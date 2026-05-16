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

## Hexagonal Architecture: Pragmatic Modulith Approach

### Module Architecture Graph

```mermaid
graph TB
    broadcast["broadcast<br/><small>(placeholder)</small>"]
    stock["stock<br/><small>catalog · price engine · REST · orchestration</small>"]
    trade["trade<br/><small>validation · execution · history</small>"]
    chaos["chaos<br/><small>(placeholder)</small>"]
    portfolio["portfolio<br/><small>cash · positions · P&L</small>"]
    db[("H2<br/><small>portfolio · position<br/>trade_history</small>")]

    subgraph OPEN ["Shared OPEN Modules"]
        cobol["cobol<br/><small>CobolAppPortOut · CobolProgramExecutor<br/>COBOL process bridge</small>"]
        config["config<br/><small>web filters · jackson · scheduling ·<br/>error handling · OTel tracing ·<br/>AOP logging · log masking</small>"]
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
    style chaos stroke-dasharray: 5 5
    style cobol fill:#e6f3ff,stroke:#4a9eff
    style config fill:#e6f3ff,stroke:#4a9eff
    style generated fill:#e6f3ff,stroke:#4a9eff
    style util fill:#e6f3ff,stroke:#4a9eff
```

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

### Where is ok to relax purity

A purist hexagonal architecture demands **one port per driven concern** and forbids any framework annotation in the core. We relax both selectively wherever purity would add ceremony without clarity:

- **Framework annotations in the core** — `@Transactional`, `@PostConstruct`, Spring scheduling annotations, and similar go on the service layer when they express a *business concern* (e.g. "this operation must be atomic", "the catalog must be loaded at startup") rather than a technical implementation detail. The rule: if the annotation describes *what* the system does, it belongs in the core; if it describes *how* (e.g. specific connection pool settings), it belongs in an adapter.

- **Framework types in the core** — Stable framework types (`Page`, `Pageable`) appear in service classes, port interfaces, or anywhere in the application layer when a hand-rolled equivalent would add zero semantic value. The test is: would a custom wrapper tell a future reader something they wouldn't get from the original type? If no, we keep the framework type and document the dependency boundary.

- **Direct injection of stable Spring framework classes** — Well-defined, stable Spring interfaces like `ApplicationEventPublisher` may be injected directly into services without a port wrapper. The test is the same as for framework types: would a custom port interface add semantic clarity, or just add indirection? If the framework interface already expresses the business intent clearly, we inject directly and document the dependency boundary.

- **Consolidated ports over fine-grained ones** — Ports group related read + write operations that always change together within the same transaction boundary. This avoids the indirection of "one method per operation" ports while keeping the core decoupled from any specific persistence technology.

### What goes in the adapter layer

Every module follows the same split: the **application core** (`application/`) holds only business logic expressed as service classes that depend exclusively on domain records and port interfaces. The **adapter layer** (`adapter/in/`, `adapter/out/`) owns everything that touches infrastructure:

- **`adapter/in/`** — REST controllers, scheduled task runners, SSE publishers. These translate external protocol (HTTP requests, scheduling ticks) into core service calls and map responses back to transport DTOs.
- **`adapter/out/`** — JPA repository adapters (entity mapping, query execution), COBOL process adapters (serialization, process spawning, deserialization), and their stub counterparts used in development profiles. Each adapter implements a port interface from the core and translates between domain records and infrastructure-specific types (entities, COBOL JSON DTOs, etc.).

The core never imports a JPA entity, a MapStruct mapper, a REST DTO, or a COBOL bridge class. Those live in the adapters, swapped by Spring's profile mechanism: stubs are active by default (`!cobol & !production`), real implementations activate only when their environment is configured.

Additional considerations for the adapter layer:

- **Stub adapters may contain business logic** — Stubs approximate the real COBOL programs for local dev and CI. They necessarily encode domain rules (validation, pricing, execution math) so the system works end-to-end without external dependencies. These rules are *approximations* and may drift from the COBOL canonical logic. Treat stubs as dev-time stand-ins, not as source of truth for business rules.

- **Inline mapping vs. dedicated mappers** — Adapters may map directly between infrastructure types and domain records inline when the conversion is trivial (a handful of field assignments). A dedicated MapStruct mapper is preferred when the mapping is non-trivial, shared across multiple methods, or would clutter the adapter's readability. Consistency within a module matters more than consistency across modules.

- **Placeholders for values enriched later** — Adapters should avoid making domain decisions, but returning placeholder values (e.g. `BigDecimal.ZERO`) for fields the adapter cannot populate — because the data source doesn't carry them yet — is a valid workaround. The service layer enriches these values before they reach the caller. The rule: an adapter may return an incomplete record when it *genuinely doesn't have the data*, not when it's *choosing a domain default*.

### Naming Convention

All classes follow the formula: `{Module}{Concept}{Layer}[Technology]`

| Part | Meaning | Examples |
|------|---------|----------|
| `Module` | Spring Modulith module the class belongs to | `Stock`, `Trade`, `Portfolio` |
| `Concept` | What the class does (omit when unambiguous) | `Catalog`, `PriceEngine`, `Validator`, `History` |
| `Layer` | Hexagonal/architectural role | `PortIn`, `PortOut`, `Controller`, `Service`, `Adapter`, `Mapper`, `Repository` |
| `[Technology]` | Implementation detail (optional) | `Cobol`, `Jpa`, `Rest` |

**Ports** — interfaces defining module boundaries. The `[Technology]` suffix is **never** used on ports — ports are technology-agnostic contracts and should not expose which adapter implements them:
`StockPortIn`, `StockPriceEnginePortOut`, `TradeValidationPortOut`, `TradeExecutionPortOut`, `TradeHistoryPortOut`, `PortfolioPortOut`

**Adapters** — technology-specific implementations of ports:
`StockCatalogCobolAdapter`, `StockPriceEngineCobolAdapterStub`, `TradeHistoryJpaAdapter`, `PortfolioJpaAdapter`

**Repositories & Mappers** — persistence and mapping layer:
`PortfolioPositionJpaRepository`, `TradePortfolioJpaRepository`, `TradeValidatorCobolMapper`

**Controllers & Services** — REST endpoints and application logic:
`StockController`, `TradeService`, `PortfolioService`

---

## End to End Flows

### 1. Trade Validation

`POST /api/trades/validate` validates a trade request (symbol, action, quantity) against business rules — checking symbol existence, action validity, and fund sufficiency. In production, the `TradeService` delegates to `TradeValidationPortOut` which calls the COBOL `trade-validator` program. In dev, the stub adapter performs the same checks in-memory.

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
    TS->>TVS: validateTrade(trade)
    Note over TVS: In-memory symbol lookup,<br/>funds check, validation logic
    TVS-->>TS: TradeValidation
    TS-->>TC: TradeValidation
    TC->>TC: map(TradeValidation → TradeValidationResult)
    TC-->>Client: 200 TradeValidationResponse
```

---

### 2. Get Market Stocks

`GET /api/market/stocks` returns current stock prices. At startup, `StockService` loads the full stock catalog from the `StockPortOut` adapter (COBOL `catalog` program or hardcoded stub) and populates an in-memory price map. Subsequent requests read from this map — no COBOL call per request.

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
    participant CCA as StockCatalogCobolAdapter
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
    participant CPS as StockCatalogCobolAdapterStub
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

The `StockService` (in `stock`) orchestrates each tick: it reads the stock catalog, delegates to `StockPriceEnginePortOut` (implemented by `StockPriceEngineCobolAdapter`), and publishes `StockPriceUpdatedEvent`. Price tracking is handled in-memory within `StockService`.

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
    participant PEA as StockPriceEngineCobolAdapter
    end
    box "cobol: Adapter Out"
    participant CPE as CobolProgramExecutor
    end
    participant COBOL as price-engine (COBOL)

    Note over Sched: Every ${stonks.market.simulation.interval-ms} (default 2s)
    Sched->>SS: simulate()
    Note over SS: read stock catalog from<br/>StockCatalogCobolAdapter, then<br/>for each stock...
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
    participant PES as StockPriceEngineCobolAdapterStub
    end

    Note over Sched: Every ${stonks.market.simulation.interval-ms} (default 2s)
    Sched->>SS: simulate()
    Note over SS: read stock catalog from<br/>StockCatalogCobolAdapterStub, then<br/>for each stock...
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
2. Service reads portfolio state (cash balance + position) from DB via `TradePortfolioStatePortOut`
3. Service builds a `TradeExecutionInput` and delegates to `TradeExecutionPortOut` (COBOL or stub)
4. If the result is `ACCEPTED`, service persists updated portfolio via `TradePortfolioStatePortOut.applyExecution()` and records history via `TradeHistoryPortOut.recordExecution()`
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
    box "trade: Adapter Out (JPA)"
    participant TPSJA as TradePortfolioStateJpaAdapter
    participant THJA as TradeHistoryJpaAdapter
    end
    box "trade: Adapter Out"
    participant TPMCA as TradePortfolioMgrCobolAdapter
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

    Note over TS: Read portfolio state from DB
    TS->>TPSJA: getState(portfolioId, symbol)
    TPSJA->>DB: SELECT cash_balance, position quantity
    DB-->>TPSJA: portfolio row + position
    TPSJA-->>TS: TradePortfolioState(cashBalance, holdingQty)

    Note over TS: Build enriched input, delegate to COBOL
    TS->>TS: TradeExecutionInput(action, symbol, qty, price, cashBalance, holdingQty)
    TS->>TPMCA: executeTrade(input)
    TPMCA->>TPMCA: map(TradeExecutionInput → CobolPortfolioMgrRequest)
    TPMCA->>CPE: execute("portfolio-mgr", req, CobolPortfolioMgrResult.class)
    CPE->>COBOL: spawn process, write JSON to stdin
    COBOL-->>CPE: stdout JSON
    CPE-->>TPMCA: CobolPortfolioMgrResult
    TPMCA->>TPMCA: map(CobolPortfolioMgrResult → TradeExecutionResult)
    TPMCA-->>TS: TradeExecutionResult

    alt ACCEPTED
        TS->>TPSJA: applyExecution(portfolioId, symbol, newCashBalance, newQuantity)
        TPSJA->>DB: UPDATE cash_balance, MERGE position
        DB-->>TPSJA: updated
        TPSJA-->>TS: void
        TS->>THJA: recordExecution(trade, result, portfolioId)
        THJA->>THJA: map(trade, result → TradeHistory entity)
        THJA->>DB: INSERT trade_history
        DB-->>THJA: inserted
        THJA-->>TS: void
    end

    TS-->>TC: TradeExecutionResult
    TC->>TC: map(TradeExecutionResult → TradeExecutionResponse)
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
    box "trade: Adapter Out (JPA)"
    participant TPSJA as TradePortfolioStateJpaAdapter
    participant THJA as TradeHistoryJpaAdapter
    end
    box "trade: Adapter Out"
    participant TPMCAS as TradePortfolioMgrCobolAdapterStub
    end
    participant DB as H2

    Client->>TC: POST /api/trades (action, symbol, qty)
    TC->>TC: map(TradeExecutionRequest → Trade)
    TC->>TS: executeTrade(trade)

    Note over TS: Enrich with market price
    TS->>SS: getStocks()
    SS-->>TS: stock prices
    TS->>TS: Trade(action, symbol, qty, currentPrice, 0)

    Note over TS: Read portfolio state from DB
    TS->>TPSJA: getState(portfolioId, symbol)
    TPSJA->>DB: SELECT cash_balance, position quantity
    DB-->>TPSJA: portfolio row + position
    TPSJA-->>TS: TradePortfolioState(cashBalance, holdingQty)

    Note over TS: Build enriched input, delegate to stub
    TS->>TS: TradeExecutionInput(action, symbol, qty, price, cashBalance, holdingQty)
    TS->>TPMCAS: executeTrade(input)
    Note over TPMCAS: Pure Java validation + computation<br/>Validates S001, S222-S226<br/>Computes newCashBalance,<br/>newQuantity, totalCost
    TPMCAS-->>TS: TradeExecutionResult

    alt ACCEPTED
        TS->>TPSJA: applyExecution(portfolioId, symbol, newCashBalance, newQuantity)
        TPSJA->>DB: UPDATE cash_balance, MERGE position
        DB-->>TPSJA: updated
        TPSJA-->>TS: void
        TS->>THJA: recordExecution(trade, result, portfolioId)
        THJA->>DB: INSERT trade_history
        DB-->>THJA: inserted
        THJA-->>TS: void
    end

    TS-->>TC: TradeExecutionResult
    TC->>TC: map(TradeExecutionResult → TradeExecutionResponse)
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

`GET /api/trades/history` returns paginated trade history from the DB via `TradeHistoryJpaAdapter`.

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "trade: Adapter In"
    participant TC as TradeController
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "trade: Adapter Out (JPA)"
    participant THJA as TradeHistoryJpaAdapter
    participant THR as TradeHistoryJpaRepository
    end
    participant DB as H2

    Client->>TC: GET /api/trades/history?page=0&size=20
    TC->>TS: getTradeHistory(page, size)
    TS->>THJA: getTradeHistory(pageable)
    THJA->>THR: findByPortfolioIdOrderByExecutedAtDesc(1L, Pageable)
    THR->>DB: SELECT ... ORDER BY executed_at DESC
    DB-->>THR: trade history entities
    THR-->>THJA: entities
    THJA->>THJA: map entities → domain records
    THJA-->>TS: trade history page
    TS-->>TC: trade history page
    TC->>TC: map domain → response
    TC-->>Client: 200 TradeHistoryResponse
```

---

## Testing Approach

Tests run against H2 with COBOL stubs active by default — zero external dependencies.

### Strategy

| Layer | Tool | Purpose |
|-------|------|---------|
| E2E | `@ApplicationModuleTest` + `RestTestClient` | Full HTTP flow through module boundaries |
| Unit | `@ExtendWith(MockitoExtension.class)` | Stubbed adapter logic, edge cases not reachable via E2E |
| Integration | Plain JUnit | COBOL executor, process spawning |
| Architecture | `ApplicationModules.verify()` | Modulith boundary enforcement |

### Test Hierarchy

**E2E tests are the default.** They run against H2 with COBOL stubs active, exercising the full request-to-response path through module boundaries. If a scenario can be tested end-to-end, it should be.

**Unit tests fill gaps.** Some classes (typically real COBOL adapters) are never initialized when stubs are active, so E2E cannot reach them. Unit tests with mocked ports cover those unreachable paths and complex edge-case logic that would be awkward to assert through HTTP. MapStruct mappers in COBOL adapter tests use `@Spy` with the generated `Impl` class rather than `@Mock`, so the real mapping logic is exercised — only the `CobolAppPortOut` call is mocked.

**Integration tests are minimal.** There is exactly one test for `CobolProgramExecutor` — verifying that process spawning, stdin/stdout JSON, and timeout handling work. No additional integration tests are planned; the COBOL bridge is a stable concern.

### Coverage

Coverage thresholds are a suggestion, not a hard rule. Adjust them up or down as the codebase evolves. The goal is to catch regressions, not to chase a number.

### E2E Test Data

Each test declares its data needs via `@Sql` referencing reusable SQL fixtures in `src/test/resources/sql/`:

```
sql/
├── portfolio.sql                    # baseline portfolio ($10k cash)
├── portfolio-with-position.sql      # portfolio + GMEE position (qty 10)
├── portfolio-with-limited-position.sql  # portfolio + GMEE position (qty 3)
└── portfolio-with-history.sql       # portfolio + 2 trade history entries
```

Tests that need an empty DB use inline cleanup statements:
```java
@Sql(statements = {"DELETE FROM trade_history", "DELETE FROM position", "DELETE FROM portfolio"})
```

### What to Avoid

- **`@DirtiesContext`** — SQL fixtures + per-test isolation make context rebuilds unnecessary
- **`@TestPropertySource`** — `src/test/resources/application.yaml` overrides `spring.sql.init.mode=never` globally for tests
- **`@ActiveProfiles`** — default profile (stubs + H2) is what tests need
- **Repository autowiring for setup** — data is declarative, not constructed in test methods

### Running Tests

```bash
./gradlew test                          # all tests
./gradlew test --tests TradeHistoryE2eTest  # single class
./gradlew jacocoTestCoverageVerification    # coverage check
```
