# stonks_java — Spring Boot Backend

Orchestrates the stonks-simulator: exposes REST APIs, runs the market simulation loop, and bridges requests to COBOL programs via **stdin/stdout JSON over OS process execution**.

---

## Module Architecture

```mermaid
graph TB
    broadcast["broadcast<br/><small>(placeholder)</small>"]
    stock["stock<br/><small>catalog · price engine · REST · orchestration</small>"]
    trade["trade<br/><small>validation · execution · history</small>"]
    chaos["chaos<br/><small>(placeholder)</small>"]
    portfolio["portfolio<br/><small>cash · positions · P&L</small>"]
    db[("H2<br/><small>portfolio · position<br/>trade_history</small>")]

    subgraph OPEN ["Shared OPEN Modules"]
        cobol["cobol<br/><small>CobolPortOut · CobolProgramExecutor<br/>COBOL process bridge</small>"]
        config["config<br/><small>web filters · jackson · scheduling</small>"]
        generated["generated<br/><small>OpenAPI DTOs</small>"]
        util["util<br/><small>ValuedEnum · metadata</small>"]
    end

    broadcast ---> stock
    broadcast ---> trade
    broadcast ---> chaos
    broadcast ---> portfolio

    trade ---> stock
    trade ---> portfolio
    chaos ---> stock
    portfolio ---> stock

    portfolio -.->|reads/writes| db
    trade -.->|reads/writes| db

    style stock stroke-width:2px
    style broadcast stroke-dasharray: 5 5
    style cobol fill:#e6f3ff,stroke:#4a9eff
    style config fill:#e6f3ff,stroke:#4a9eff
    style generated fill:#e6f3ff,stroke:#4a9eff
    style util fill:#e6f3ff,stroke:#4a9eff
```

---

## Happy Paths

### 1. Trade Validation

#### Real Scenario (COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trade: Adapter In"
    participant TC as TradeController
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "trade: Adapter Out"
    participant CTA as TradeValidatorCobolAdapter
    end
    box "cobol: Adapter Out"
    participant CPE as CobolProgramExecutor
    end
    participant COBOL as trade-validator (COBOL)

    Client->>TC: POST /api/trades/validate
    TC->>TC: map(request → Trade)
    TC->>TS: validateTrade(trade)
    TS->>CTA: validate(trade)
    CTA->>CTA: map(Trade → CobolTradeValidationRequest)
    CTA->>CPE: execute("trade-validator", req, CobolTradeValidationResult.class)
    CPE->>COBOL: spawn process, write JSON to stdin
    COBOL-->>CPE: stdout JSON
    CPE-->>CTA: CobolTradeValidationResult
    CTA->>CTA: map(CobolTradeValidationResult → TradeValidation)
    CTA-->>TS: TradeValidation
    TS-->>TC: TradeValidation
    TC->>TC: map(TradeValidation → TradeValidationResult)
    TC-->>Client: 200 TradeValidationResponse
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trade: Adapter In"
    participant TC as TradeController
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "trade: Adapter Out"
    participant TVS as TradeValidatorCobolAdapterStub
    end

    Client->>TC: POST /api/trades/validate
    TC->>TC: map(request → Trade)
    TC->>TS: validateTrade(trade)
    TS->>TVS: validate(trade)
    Note over TVS: In-memory symbol lookup,<br/>funds check, validation logic
    TVS-->>TS: TradeValidation
    TS-->>TC: TradeValidation
    TC->>TC: map(TradeValidation → TradeValidationResult)
    TC-->>Client: 200 TradeValidationResponse
```

---

### 2. Get Market Stocks

#### Real Scenario (COBOL catalog load at startup, then projection-based reads)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "stock: Adapter In"
    participant SC as StockController
    end
    box "stock: Application"
    participant SS as StockService
    end
    box "stock: Adapter Out"
    participant CCA as CatalogCobolAdapter
    end
    box "cobol: Adapter Out"
    participant CPE as CobolProgramExecutor
    end
    participant COBOL as catalog (COBOL)

    Note over SS: @PostConstruct init()
    SS->>CCA: getStocks()
    CCA->>CPE: execute("catalog", null, CobolCatalogStock[].class)
    CPE->>COBOL: spawn process
    COBOL-->>CPE: stdout JSON array
    CPE-->>CCA: CobolCatalogStock[]
    CCA->>CCA: map(CobolCatalogStock → Stock)
    CCA-->>SS: stock list
    Note over SS: populate price map

    Note over Client,SC: Later request
    Client->>SC: GET /api/market/stocks
    SC->>SS: getStocks()
    Note over SS: read price snapshot
    SS-->>SC: stock prices
    SC->>SC: map(StockPrice → StockPrice)
    SC-->>Client: 200 StocksResponse
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "stock: Adapter In"
    participant SC as StockController
    end
    box "stock: Application"
    participant SS as StockService
    end
    box "stock: Adapter Out"
    participant CPS as CatalogCobolAdapterStub
    end

    Note over SS: @PostConstruct init()
    SS->>CPS: getStocks()
    CPS-->>SS: stock list (10 hardcoded meme stocks)
    Note over SS: populate price map

    Note over Client,SC: Later request
    Client->>SC: GET /api/market/stocks
    SC->>SS: getStocks()
    Note over SS: read price snapshot
    SS-->>SC: stock prices
    SC->>SC: map(StockPrice → StockPrice)
    SC-->>Client: 200 StocksResponse
```

---

### 3. Price Simulation (Scheduled, Event-Driven)

The `StockService` (in `stock`) orchestrates each tick: it reads the stock catalog, delegates to `PriceEnginePortOut` (implemented by `PriceEngineCobolAdapter`), and publishes `StockPriceUpdatedEvent`. Price tracking is handled in-memory within `StockService`.

#### Real Scenario (COBOL)

```mermaid
sequenceDiagram
    box "stock: Adapter In"
    participant Sched as StockPriceTickScheduler
    end
    box "stock: Application"
    participant SS as StockService
    end
    box "stock: Adapter Out"
    participant PEA as PriceEngineCobolAdapter
    end
    box "cobol: Adapter Out"
    participant CPE as CobolProgramExecutor
    end
    participant COBOL as price-engine (COBOL)

    Note over Sched: Every ${stonks.market.simulation.interval-ms} (default 2s)
    Sched->>SS: simulate()
    Note over SS: read stock catalog from<br/>CatalogCobolAdapter, then<br/>for each stock...
    loop For each stock
        SS->>PEA: calculate(currentPrice, volatility, trend)
        PEA->>CPE: execute("price-engine", request, CobolPriceEngineResult.class)
        CPE->>COBOL: spawn process, write JSON to stdin
        COBOL-->>CPE: stdout JSON {newPrice}
        CPE-->>PEA: CobolPriceEngineResult
        PEA-->>SS: newPrice (BigDecimal)
        SS->>SS: publishEvent(StockPriceUpdatedEvent)
    end
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    box "stock: Adapter In"
    participant Sched as StockPriceTickScheduler
    end
    box "stock: Application"
    participant SS as StockService
    end
    box "stock: Adapter Out"
    participant PES as PriceEngineCobolAdapterStub
    end

    Note over Sched: Every ${stonks.market.simulation.interval-ms} (default 2s)
    Sched->>SS: simulate()
    Note over SS: read stock catalog from<br/>CatalogCobolAdapterStub, then<br/>for each stock...
    loop For each stock
        SS->>PES: calculate(currentPrice, volatility, trend)
        Note over PES: Random walk with trend bias,<br/>circuit breaker, price bounds
        PES-->>SS: newPrice (BigDecimal)
        SS->>SS: publishEvent(StockPriceUpdatedEvent)
    end
```

---

### 4. Trade Execution

`POST /api/trades` validates the trade (reuses the validation flow), loads the current portfolio from the DB, delegates portfolio mutation to `PORTFOLIO-MGR` (COBOL), then persists the updated state inside a DB transaction.

#### Real Scenario (COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trade: Adapter In"
    participant TC as TradeController
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "trade: Adapter Out"
    participant TVA as TradeValidatorCobolAdapter
    participant PMA as PortfolioMgrCobolAdapter
    end
    box "portfolio: Adapter Out"
    participant PJA as PortfolioJpaAdapter
    end
    box "cobol: Adapter Out"
    participant CPE as CobolProgramExecutor
    end
    participant TV as trade-validator (COBOL)
    participant PM as portfolio-mgr (COBOL)
    participant DB as H2

    Client->>TC: POST /api/trades
    TC->>TC: map(request → Trade)
    TC->>TS: executeTrade(trade)

    Note over TS: Step 1: Validate
    TS->>TVA: validate(trade)
    TVA->>CPE: execute("trade-validator", req, CobolTradeValidationResult.class)
    CPE->>TV: spawn, write to stdin
    TV-->>CPE: stdout JSON
    CPE-->>TVA: CobolTradeValidationResult
    TVA-->>TS: TradeValidation
    alt REJECTED
        TS-->>TC: execution failed
        TC-->>Client: 200 TradeExecutionResult (REJECTED)
    end

    Note over TS: Step 2: Load portfolio
    TS->>PJA: getPortfolio()
    PJA->>DB: SELECT portfolio + positions
    DB-->>PJA: rows
    PJA-->>TS: PortfolioSummary

    Note over TS: Step 3: Execute via COBOL
    TS->>PMA: execute(trade, currentHolding)
    PMA->>PMA: map(Trade + holding → CobolPortfolioMgrRequest)
    PMA->>CPE: execute("portfolio-mgr", req, CobolPortfolioMgrResult.class)
    CPE->>PM: spawn, write to stdin
    PM-->>CPE: stdout JSON
    CPE-->>PMA: CobolPortfolioMgrResult
    PMA-->>TS: TradeExecutionResult

    Note over TS: Step 4: Persist & return
    TS->>PJA: updateCash(cashBalance)
    TS->>PJA: upsertPosition(symbol, newQty)
    TS->>PJA: insertTradeHistory(...)
    PJA->>DB: UPDATE / INSERT / INSERT
    DB-->>PJA: success

    TS-->>TC: TradeExecutionResult
    TC->>TC: map → TradeExecutionResult
    TC-->>Client: 200 TradeExecutionResponse
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trade: Adapter In"
    participant TC as TradeController
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "trade: Adapter Out"
    participant TVS as TradeValidatorCobolAdapterStub
    participant PMS as PortfolioMgrCobolAdapterStub
    end
    box "portfolio: Adapter Out"
    participant PJA as PortfolioJpaAdapter
    end
    participant DB as H2

    Client->>TC: POST /api/trades
    TC->>TC: map(request → Trade)
    TC->>TS: executeTrade(trade)

    Note over TS: Step 1: Validate (Java stub)
    TS->>TVS: validate(trade)
    Note over TVS: In-memory symbol lookup,<br/>funds check
    TVS-->>TS: TradeValidation
    alt REJECTED
        TS-->>TC: execution failed
        TC-->>Client: 200 TradeExecutionResult (REJECTED)
    end

    Note over TS: Step 2: Load portfolio
    TS->>PJA: getPortfolio()
    PJA->>DB: SELECT
    DB-->>PJA: rows
    PJA-->>TS: PortfolioSummary

    Note over TS: Step 3: Execute (Java stub)
    TS->>PMS: execute(trade, currentHolding)
    Note over PMS: Pure Java: cash check,<br/>share check, new state
    PMS-->>TS: TradeExecutionResult

    Note over TS: Step 4: Persist & return
    TS->>PJA: updateCash / upsertPosition / insertHistory
    PJA->>DB: UPDATE / INSERT
    DB-->>PJA: success

    TS-->>TC: TradeExecutionResult
    TC-->>Client: 200 TradeExecutionResponse
```

---

### 5. Get Portfolio

`GET /api/portfolio` reads the portfolio + positions from the DB, fetches current stock prices from the `stock` module, and computes unrealized P&L per position and total.

#### Real & Dev Stub (no COBOL involved — pure DB + stock module)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "portfolio: Adapter In"
    participant PC as PortfolioController
    end
    box "portfolio: Application"
    participant PS as PortfolioService
    end
    box "portfolio: Adapter Out"
    participant PJA as PortfolioJpaAdapter
    end
    box "stock: Application"
    participant SS as StockService
    end
    participant DB as H2

    Client->>PC: GET /api/portfolio
    PC->>PS: getPortfolio()
    PS->>PJA: getPortfolio()
    PJA->>DB: SELECT portfolio
    DB-->>PJA: cashBalance
    PJA->>DB: SELECT positions
    DB-->>PJA: positions list
    PJA-->>PS: PortfolioSummary(cash, positions)

    Note over PS: For each position,<br/>look up current market price
    PS->>SS: getStocks()
    SS-->>PS: stock prices map

    Note over PS: Compute per-position:<br/>marketValue = qty * currentPrice<br/>unrealizedPnl = marketValue - costBasis

    PS-->>PC: PortfolioSummary with P&L
    PC->>PC: map → PortfolioResponse
    PC-->>Client: 200 PortfolioResponse
```

---

### 6. Get Trade History

`GET /api/trades/history` returns paginated trade history from the DB.

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trade: Adapter In"
    participant TC as TradeController
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "trade: Adapter Out"
    participant THR as TradeHistoryJpaRepository
    end
    participant DB as H2

    Client->>TC: GET /api/trades/history?page=0&size=20
    TC->>TS: getTradeHistory(page, size)
    TS->>THR: findAll(Pageable)
    THR->>DB: SELECT ... LIMIT/OFFSET
    DB-->>THR: Page<TradeHistoryEntity>
    THR-->>TS: Page<TradeHistoryItem>
    TS-->>TC: Page<TradeHistoryItem>
    TC->>TC: map → Page schema
    TC-->>Client: 200 TradeHistoryResponse
```
