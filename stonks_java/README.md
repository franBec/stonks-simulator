# stonks_java — Spring Boot Backend

Orchestrates the stonks-simulator: exposes REST APIs, runs the market simulation loop, and bridges requests to COBOL programs via **stdin/stdout JSON over OS process execution**.

## Environments

Three runtime profiles control which dependencies are active:

| Profile | DB | COBOL | OTel | Use case |
|---------|----|-------|------|----------|
| *(none)* | H2 (embedded) | Stubs (Java in-memory) | Disabled | Default for local dev & `./gradlew test` |
| `cobol` | H2 (embedded) | Real COBOL process execution | Disabled | Manual testing with COBOL setup |
| `production` | PostgreSQL | Real COBOL process execution | Enabled | Production/staging (*PG driver not yet in build.gradle*) |

- **`./gradlew bootRun`** — starts with H2 + stubs, no external dependencies needed.
- **`./gradlew bootRun --spring.profiles.active=cobol`** — starts with H2 + real COBOL binaries.
- **`./gradlew test`** — runs against H2 + stubs. CI-ready, zero config.
- **VS Code** — three launch configs in `.vscode/launch.json`: `[local]`, `[cobol]`, `[production]`.

### How it works

- `application.yaml` (always loaded) provides H2 datasource + disables OTel by default.
- COBOL stub adapters are annotated with `@Profile("!cobol & !production")` — active in any profile except `cobol` or `production`.
- Real COBOL adapters are annotated with `@Profile({"cobol", "production"})` — only active when one of those profiles is set.
- `application-production.yaml` overrides the datasource to PostgreSQL and enables OTel.

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

## Naming Convention

All classes follow the formula: `{Module}{Concept}{Layer}[Technology]`

| Part | Meaning | Examples |
|------|---------|----------|
| `Module` | Spring Modulith module the class belongs to | `Stock`, `Trade`, `Portfolio` |
| `Concept` | What the class does (omit when unambiguous) | `Catalog`, `PriceEngine`, `Validator`, `History` |
| `Layer` | Hexagonal/architectural role | `PortIn`, `PortOut`, `Controller`, `Service`, `Adapter`, `Mapper`, `Repository` |
| `[Technology]` | Implementation detail (optional) | `Cobol`, `Jpa`, `Rest` |

**Ports** — interfaces defining module boundaries:
`StockPortIn`, `StockPriceEnginePortOut`, `TradeValidatorPortOutCobol`, `PortfolioPortOut`

**Adapters** — technology-specific implementations of ports:
`StockCatalogCobolAdapter`, `StockPriceEngineCobolAdapterStub`, `TradeHistoryJpaAdapter`, `PortfolioJpaAdapter`

**Repositories & Mappers** — persistence and mapping layer:
`PortfolioPositionJpaRepository`, `TradePortfolioJpaRepository`, `TradeValidatorCobolMapper`

**Controllers & Services** — REST endpoints and application logic:
`StockController`, `TradeService`, `PortfolioService`

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

`POST /api/trades` executes a BUY/SELL trade atomically:
1. Service enriches the request with the current market price via `StockPortIn`
2. Adapter reads portfolio cash balance and position holding qty from the DB
3. Adapter calls COBOL `PORTFOLIO-MGR` (which validates and computes new state)
4. If `ACCEPTED`, adapter persists updated cash balance, position, and trade history
5. Returns `TradeExecutionResult` with the new portfolio state

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
    box "stock: Application"
    participant SS as StockService
    end
    box "trade: Adapter Out"
    participant PMA as PortfolioMgrCobolAdapter
    end
    box "trade: Adapter Out (JPA)"
    participant TEPR as TradeExecutionPortfolioJpaRepository
    participant TEPR2 as TradeExecutionPositionJpaRepository
    participant THR as TradeHistoryJpaRepository
    end
    box "cobol: Adapter Out"
    participant CPE as CobolProgramExecutor
    end
    participant COBOL as portfolio-mgr (COBOL)
    participant DB as H2

    Client->>TC: POST /api/trades (action, symbol, qty)
    TC->>TC: map(TradeExecutionRequest → Trade)
    TC->>TS: executeTrade(trade)

    Note over TS: Enrich with market price
    TS->>SS: getStocks()
    SS-->>TS: stock prices
    TS->>TS: Trade(action, symbol, qty, currentPrice, 0)

    Note over TS: Single delegate call
    TS->>PMA: executeTrade(trade)

    Note over PMA: Read state from DB
    PMA->>TEPR: findById(1L)
    TEPR->>DB: SELECT cash_balance
    DB-->>TEPR: portfolio row
    TEPR-->>PMA: cashBalance

    PMA->>TEPR2: findByPortfolioIdAndSymbol(1L, symbol)
    TEPR2->>DB: SELECT quantity
    DB-->>TEPR2: position (or empty)
    TEPR2-->>PMA: holdingQty (0 if none)

    Note over PMA: Call PORTFOLIO-MGR
    PMA->>PMA: CobolPortfolioMgrRequest(action, symbol, qty, price, cashBalance, holdingQty)
    PMA->>CPE: execute("portfolio-mgr", req, CobolPortfolioMgrResult.class)
    CPE->>COBOL: spawn, write JSON to stdin
    COBOL-->>CPE: stdout JSON
    CPE-->>PMA: CobolPortfolioMgrResult
    PMA->>PMA: map → TradeExecutionResult

    alt ACCEPTED
        PMA->>TEPR: save(cashBalance = newCashBalance)
        TEPR->>DB: UPDATE portfolio
        PMA->>TEPR2: save(quantity = newQuantity)
        TEPR2->>DB: MERGE position
        PMA->>THR: save(trade_history record)
        THR->>DB: INSERT
    end

    PMA-->>TS: TradeExecutionResult
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
    box "stock: Application"
    participant SS as StockService
    end
    box "trade: Adapter Out"
    participant PMS as PortfolioMgrCobolAdapterStub
    end
    box "trade: Adapter Out (JPA)"
    participant TEPR as TradeExecutionPortfolioJpaRepository
    participant TEPR2 as TradeExecutionPositionJpaRepository
    participant THR as TradeHistoryJpaRepository
    end
    participant DB as H2

    Client->>TC: POST /api/trades (action, symbol, qty)
    TC->>TC: map(TradeExecutionRequest → Trade)
    TC->>TS: executeTrade(trade)

    Note over TS: Enrich with market price
    TS->>SS: getStocks()
    SS-->>TS: stock prices
    TS->>TS: Trade(action, symbol, qty, currentPrice, 0)

    Note over TS: Single delegate call
    TS->>PMS: executeTrade(trade)

    Note over PMS: Read state from DB
    PMS->>TEPR: findById(1L)
    TEPR->>DB: SELECT cash_balance
    DB-->>TEPR: portfolio row
    TEPR-->>PMS: cashBalance

    PMS->>TEPR2: findByPortfolioIdAndSymbol(1L, symbol)
    TEPR2->>DB: SELECT quantity
    DB-->>TEPR2: position (or empty)
    TEPR2-->>PMS: holdingQty (0 if none)

    Note over PMS: Pure Java validation + computation
    Note over PMS: Validates S001, S222-S226
    Note over PMS: Computes newCashBalance,<br/>newQuantity, totalCost

    alt ACCEPTED
        PMS->>TEPR: save(cashBalance = newCashBalance)
        TEPR->>DB: UPDATE portfolio
        PMS->>TEPR2: save(quantity = newQuantity)
        TEPR2->>DB: MERGE position
        PMS->>THR: save(trade_history record)
        THR->>DB: INSERT
    end

    PMS-->>TS: TradeExecutionResult
    TS-->>TC: TradeExecutionResult
    TC->>TC: map → TradeExecutionResult
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

`GET /api/trades/history` returns paginated trade history from the DB via the outbound adapter.

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
    participant PA as PortfolioMgrCobolAdapter
    end
    box "trade: Adapter Out (JPA)"
    participant THR as TradeHistoryJpaRepository
    end
    participant DB as H2

    Client->>TC: GET /api/trades/history?page=0&size=20
    TC->>TS: getTradeHistory(page, size)
    TS->>PA: getTradeHistory(page, size)
    PA->>THR: findByPortfolioIdOrderByExecutedAtDesc(1L, Pageable)
    THR->>DB: SELECT ... ORDER BY executed_at DESC
    DB-->>THR: page of trade history
    THR-->>PA: Page of trade history
    PA->>PA: map(Page → TradeHistoryPage)
    PA-->>TS: TradeHistoryPage
    TS-->>TC: TradeHistoryPage
    TC->>TC: map(TradeHistoryPage → TradeHistoryResponse)
    TC-->>Client: 200 TradeHistoryResponse
```

---

## Hexagonal Architecture: Pragmatic Modulith Approach

### Core rule

The application core (`application/`) imports only:
- **Domain records** (`domain/`) — plain Java, zero framework coupling
- **Port interfaces** (`application/port/in/`, `application/port/out/`) — contracts, not implementations

Everything else (JPA entities, repositories, REST serialization, COBOL process bridges, MapStruct mappers) lives in the **adapter layer** (`adapter/in/`, `adapter/out/`). The core never sees infrastructure types.

### Why this shape

This is a **modulith** — a single deployable with strict module boundaries. Not microservices. The architecture optimizes for:

| Concern | Choice | Why |
|---------|--------|-----|
| Transaction boundary | `@Transactional` on the **service** (not the adapter) | The unit of work is a business operation (update portfolio + position + history atomically), not an infrastructure detail. Adapters participate via propagation. |
| Port granularity | Consolidate related CRUD behind one port | Avoid "one port per table" syndrome. `TradePortfolioStatePortOut` covers read + write of portfolio + position because they always change together. |
| Entity relationships | Adapters own entity lifecycle internally | The `TradeHistory` → `Portfolio` FK is resolved inside the adapter, not the core. Hibernate's first-level cache prevents redundant queries within the same transaction. |
| Profile segregation | Stub adapters active by default (`!cobol & !production`) | Local dev and CI need zero external dependencies. Real adapters activate only when the environment provides them. |

### Where we relaxed purity

A purist hexagonal architecture demands **one port per driven concern** and forbids any framework annotation in the core. We relaxed both in measured ways:

1. **`@Transactional` on the service** — purists would push this into an adapter or use a decorator. We keep it on the service because it marks a *business transaction boundary*, not a technical one. Every adapter call inside that method joins the same transaction via Spring's `TransactionManager` propagation.

2. **`Page`/`Pageable` in ports** — Spring Data's pagination types leak into the core. A purist would define a custom pagination domain object. We accepted the leak because:
   - Replacing `Page<TradeHistoryItem>` with a custom `Page<TradeHistoryItem>` adds zero semantic value
   - Every REST adapter would immediately map back to `Page` anyway
   - The dependency is on the **interface** (`org.springframework.data.domain.Page`), not on a specific implementation or data access technology

3. **Consolidated ports instead of fine-grained ones** — `TradePortfolioStatePortOut` combines read (`getState`) and write (`applyExecution`) for two entities (portfolio + position). A purist might split into four ports (read portfolio, write portfolio, read position, write position). We consolidated because:
   - These four operations always happen together in this module
   - The transaction boundary is the same
   - Fewer ports = less indirection = easier to reason about the modulith

### What ended up in the adapter layer

```
┌──────────────────────────────────────────────────────────┐
│         APPLICATION CORE (zero infra imports)            │
│                                                          │
│  TradeService                                           │
│    • TradePortIn                       (self)           │
│    • TradeValidatorPortOutCobol        (port)           │
│    • TradeExecutorPortOutCobol         (port)           │
│    • TradePortfolioStatePortOut        (port)           │
│    • TradeHistoryPortOutJpa            (port)           │
│    • StockPortIn                       (port)           │
│    • domain records only                                │
└──────────────────────┬───────────────────────────────────┘
                       │ depends on interfaces, never on classes
                       ▼
┌──────────────────────────────────────────────────────────┐
│  ADAPTERS (out) — own all infrastructure dependencies   │
│                                                          │
│  TradePortfolioStateJpaAdapter                           │
│    • TradePortfolioJpaRepository   (.generated.entity)   │
│    • TradePositionJpaRepository    (.generated.entity)   │
│                                                          │
│  TradeHistoryJpaAdapter                                  │
│    • TradeHistoryJpaRepository     (.generated.entity)   │
│    • TradeExecutionEntityMapper    (MapStruct)           │
│    • TradeHistoryJpaMapper          (MapStruct)          │
│                                                          │
│  TradeValidatorCobolAdapter / Stub                       │
│  TradePortfolioMgrCobolAdapter / Stub                    │
│    • CobolPortOut                  (.cobol module)       │
│    • Cobol DTOs + Cobol mappers                          │
└──────────────────────────────────────────────────────────┘
```
