# stonks_java — Spring Boot Backend

Orchestrates the stonks-simulator: exposes REST APIs, runs the market simulation loop, and bridges requests to COBOL programs via **stdin/stdout JSON over OS process execution**.

---

## Module Architecture

```mermaid
graph TB
    broadcast["broadcast<br/><small>(placeholder)</small>"]
    stocks["stocks<br/><small>catalog · projection · REST · orchestration</small>"]
    simulation["simulation<br/><small>PriceEnginePort<br/>COBOL adapter · dev stub</small>"]
    trading["trading<br/><small>trade validation</small>"]
    chaos["chaos<br/><small>(placeholder)</small>"]
    portfolio["portfolio<br/><small>(placeholder)</small>"]

    broadcast ---> stocks
    broadcast ---> simulation
    broadcast ---> trading
    broadcast ---> chaos
    broadcast ---> portfolio

    stocks -.->|"::price-engine-port"| simulation
    trading ---> stocks
    chaos ---> stocks
    portfolio ---> stocks

    style simulation stroke-width:2px
    style stocks stroke-width:2px
    style broadcast stroke-dasharray: 5 5
```

---

## Happy Paths

### 1. Trade Validation

#### Real Scenario (COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trading: Adapter In"
    participant TC as TradesController
    participant TRM as TradeRestMapper
    end
    box "trading: Application"
    participant TS as TradeServiceImpl
    end
    box "trading: Adapter Out"
    participant CTA as CobolTradeValidatorAdapter
    participant TCM as TradeCobolMapper
    end
    box "cobol: Application"
    participant CPE as CobolProgramExecutorService
    end
    participant COBOL as trade-validator (COBOL)

    Client->>TC: POST /api/trades/validate
    TC->>TRM: map(request → Trade)
    TRM-->>TC: Trade
    TC->>TS: validateTrade(trade)
    TS->>CTA: validate(trade)
    CTA->>TCM: map(Trade → CobolTradeValidationRequest)
    TCM-->>CTA: CobolTradeValidationRequest
    CTA->>CPE: execute("trade-validator", req, CobolTradeValidationResult.class)
    CPE->>COBOL: spawn process, write JSON to stdin
    COBOL-->>CPE: stdout JSON
    CPE-->>CTA: CobolTradeValidationResult
    CTA->>TCM: map(CobolTradeValidationResult → TradeValidation)
    TCM-->>CTA: TradeValidation
    CTA-->>TS: TradeValidation
    TS-->>TC: TradeValidation
    TC->>TRM: map(TradeValidation → TradeValidationResult)
    TRM-->>TC: TradeValidationResult
    TC-->>Client: 200 TradeValidationResponse
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trading: Adapter In"
    participant TC as TradesController
    participant TRM as TradeRestMapper
    end
    box "trading: Application"
    participant TS as TradeServiceImpl
    end
    box "trading: Adapter Out"
    participant TVS as TradeValidatorStub
    end

    Client->>TC: POST /api/trades/validate
    TC->>TRM: map(request → Trade)
    TRM-->>TC: Trade
    TC->>TS: validateTrade(trade)
    TS->>TVS: validate(trade)
    Note over TVS: In-memory symbol lookup,<br/>funds check, validation logic
    TVS-->>TS: TradeValidation
    TS-->>TC: TradeValidation
    TC->>TRM: map(TradeValidation → TradeValidationResult)
    TRM-->>TC: TradeValidationResult
    TC-->>Client: 200 TradeValidationResponse
```

---

### 2. Get Market Stocks

#### Real Scenario (COBOL catalog load at startup, then projection-based reads)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "stocks: Adapter In"
    participant SC as StocksController
    participant SRM as StockRestMapper
    end
    box "stocks: Application"
    participant SPP as StockPriceProjector
    end
    box "stocks: Adapter Out"
    participant CCA as CobolCatalogAdapter
    end
    box "cobol: Application"
    participant CPE as CobolProgramExecutorService
    end
    participant COBOL as catalog (COBOL)

    Note over SPP: @PostConstruct init()
    SPP->>CCA: getStocks()
    CCA->>CPE: execute("catalog", null, CobolCatalogStock[].class)
    CPE->>COBOL: spawn process
    COBOL-->>CPE: stdout JSON array
    CPE-->>CCA: CobolCatalogStock[]
    CCA-->>SPP: stock list
    Note over SPP: populate projection map

    Note over Client,SC: Later request
    Client->>SC: GET /api/market/stocks
    SC->>SPP: getStocks()
    Note over SPP: read projection snapshot
    SPP-->>SC: stock prices
    SC->>SRM: map(StockPrice → StockPrice)
    SRM-->>SC: StockPrice
    SC-->>Client: 200 MarketStocksResponse
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "stocks: Adapter In"
    participant SC as StocksController
    participant SRM as StockRestMapper
    end
    box "stocks: Application"
    participant SPP as StockPriceProjector
    end
    box "stocks: Adapter Out"
    participant CPS as CatalogPortStub
    end

    Note over SPP: @PostConstruct init()
    SPP->>CPS: getStocks()
    CPS-->>SPP: stock list (10 hardcoded meme stocks)
    Note over SPP: populate projection map

    Note over Client,SC: Later request
    Client->>SC: GET /api/market/stocks
    SC->>SPP: getStocks()
    Note over SPP: read projection snapshot
    SPP-->>SC: stock prices
    SC->>SRM: map(StockPrice → StockPrice)
    SRM-->>SC: StockPrice
    SC-->>Client: 200 MarketStocksResponse
```

---

### 3. Price Simulation (Scheduled, Event-Driven)

The `StockPriceTickService` (in `stocks`) orchestrates each tick: it reads current prices and the stock catalog, delegates to `PriceEnginePort` (implemented by `simulation`), and publishes `StockPriceUpdatedEvent`. The `StockPriceProjector` (also in `stocks`) listens for those events and updates its projection.

#### Real Scenario (COBOL)

```mermaid
sequenceDiagram
    box "stocks: Adapter In"
    participant Sched as StockPriceTickScheduler
    end
    box "stocks: Application"
    participant SC as StockPriceTickService
    participant SPP as StockPriceProjector
    end
    box "simulation: Adapter Out"
    participant CPEA as CobolPriceEngineAdapter
    end
    box "cobol: Application"
    participant CPE as CobolProgramExecutorService
    end
    participant COBOL as price-engine (COBOL)

    Note over Sched: Every ${stonks.market.simulation.interval-ms} (default 2s)
    Sched->>SC: simulate()
    SC->>SPP: getStocks() (current prices)
    SPP-->>SC: current prices
    loop For each stock
        SC->>CPEA: calculate(currentPrice, volatility, trend)
        CPEA->>CPE: execute("price-engine", request, CobolPriceEngineResult.class)
        CPE->>COBOL: spawn process, write JSON to stdin
        COBOL-->>CPE: stdout JSON {newPrice}
        CPE-->>CPEA: CobolPriceEngineResult
        CPEA-->>SC: newPrice (BigDecimal)
        SC-->>SPP: publishEvent(StockPriceUpdatedEvent)
        Note over SPP: @ApplicationModuleListener<br/>updates projection map
    end
```

#### Dev Stub Scenario (no COBOL)

```mermaid
sequenceDiagram
    box "stocks: Adapter In"
    participant Sched as StockPriceTickScheduler
    end
    box "stocks: Application"
    participant SC as StockPriceTickService
    participant SPP as StockPriceProjector
    end
    box "simulation: Adapter Out"
    participant PES as PriceEngineStub
    end

    Note over Sched: Every ${stonks.market.simulation.interval-ms} (default 2s)
    Sched->>SC: simulate()
    SC->>SPP: getStocks() (current prices)
    SPP-->>SC: current prices
    loop For each stock
        SC->>PES: calculate(currentPrice, volatility, trend)
        Note over PES: Random walk with trend bias,<br/>circuit breaker, price bounds
        PES-->>SC: newPrice (BigDecimal)
        SC-->>SPP: publishEvent(StockPriceUpdatedEvent)
        Note over SPP: @ApplicationModuleListener<br/>updates projection map
    end
```
