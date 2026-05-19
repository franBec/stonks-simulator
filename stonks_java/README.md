# stonks_java — Spring Boot Backend

Orchestrates the stonks-simulator: exposes REST APIs, runs the market simulation loop, and bridges requests to COBOL programs via **stdin/stdout JSON over OS process execution**.

---

## Environments

Three runtime profiles control which dependencies are active:

| Profile | DB | COBOL | AI | News RSS | OTel | Use case |
|---------|----|-------|----|----------|------|----------|
| *(none)* | H2 (embedded) | Stubs (Java in-memory) | Stub | Stub | Disabled | Default for local dev & `./gradlew test` |
| `integrated` | H2 (embedded) | Real COBOL process execution | Real (OpenRouter) | Real (RSS fetch) | Disabled | All real backends, lightweight infra (no Docker) |
| `production` | PostgreSQL | Real COBOL process execution | Real (OpenRouter) | Real (RSS fetch) | Enabled | Production/staging (*PG driver not yet in build.gradle*) |

- **`./gradlew bootRun`** — starts with H2 + all stubs, no external dependencies needed.
- **`./gradlew bootRun --spring.profiles.active=integrated`** — starts with H2 + real backends (COBOL, AI, News RSS). No Docker required.
- **`./gradlew bootRun --spring.profiles.active=production`** — starts with PostgreSQL + real backends + OTel.
- **`./gradlew test`** — runs against H2 + stubs. CI-ready, zero config.

### How it works

- `application.yaml` (always loaded) provides H2 datasource + disables OTel by default.
- Stub adapters are annotated with `@Profile("!integrated & !production")` — active in any profile except `integrated` or `production`.
- Real backends (COBOL, AI, News RSS) are annotated with `@Profile({"integrated", "production"})` — active when either `integrated` or `production` is set.
- `application-production.yaml` overrides the datasource to PostgreSQL and enables OTel.

---

## Architecture: Hexagonal Architecture with Modulith approach

### Module Architecture Graph

```mermaid
graph TB
    broadcast["broadcast<br/><small>SSE streaming · paper tape · event hub</small>"]
    stock["stock<br/><small>catalog · price engine · REST · orchestration</small>"]
    trade["trade<br/><small>validation · execution · history</small>"]
    chaos["chaos<br/><small>event generation · level management · AI integration</small>"]
    news["news<br/><small>RSS headline fetching · caching</small>"]
    portfolio["portfolio<br/><small>cash · positions · P&L</small>"]
    db[("DB<br/><small>portfolio · position<br/>trade_history</small>")]

    subgraph OPEN ["Shared OPEN Modules"]
        cobol["cobol<br/><small>CobolAppPortOut · CobolProgramExecutor<br/>COBOL process bridge</small>"]
        config["config<br/><small>web filters · jackson · scheduling ·<br/>error handling · OTel tracing ·<br/>AOP logging · log masking</small>"]
        generated["generated<br/><small>OpenAPI DTOs</small>"]
        util["util<br/><small>ValuedEnum · metadata</small>"]
    end

    broadcast ---> stock
    broadcast ---> trade
    broadcast ---> chaos

    trade ---> stock
    chaos ---> news
    chaos ---> stock
    portfolio ---> stock

    portfolio -.->|reads/writes| db
    trade -.->|reads/writes| db

    style stock stroke-width:2px
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

### Where is OK to relax purity

A purist hexagonal architecture demands **one port per driven concern** and forbids any framework annotation in the core. We relax both selectively wherever purity would add ceremony without clarity:

- **Framework annotations in the core** — `@Transactional`, `@PostConstruct`, Spring scheduling annotations, and similar go on the service layer when they express a *business concern* (e.g. "this operation must be atomic", "the catalog must be loaded at startup") rather than a technical implementation detail. If the annotation describes *what* the system does, it belongs in the core; if it describes *how* (e.g. specific connection pool settings), it belongs in an adapter.

- **Framework types in the core** — Stable framework types (`Page`, `Pageable`) appear in service classes, port interfaces, or anywhere in the application layer when a hand-rolled equivalent would add zero semantic value.

- **Direct injection of stable Spring framework classes** — Well-defined, stable Spring interfaces like `ApplicationEventPublisher` may be injected directly into services without a port wrapper. If the framework interface already expresses the business intent clearly, we inject directly.

- **Consolidated ports over fine-grained ones** — Ports group related read + write operations that always change together within the same transaction boundary. This avoids the indirection of "one method per operation" ports while keeping the core decoupled from any specific persistence technology.

### What goes in the adapter layer

Every module follows the same split: the **application core** (`application/`) holds only business logic expressed as service classes that depend exclusively on domain records and port interfaces. The **adapter layer** (`adapter/in/`, `adapter/out/`) owns everything that touches infrastructure:

- **`adapter/in/`** — REST controllers, scheduled task runners, SSE publishers. These translate external protocol (HTTP requests, scheduling ticks) into core service calls and map responses back to transport DTOs.
- **`adapter/out/`** — JPA repository adapters (entity mapping, query execution), COBOL process adapters (serialization, process spawning, deserialization), and their stub counterparts used in development profiles. Each adapter implements a port interface from the core and translates between domain records and infrastructure-specific types (entities, COBOL JSON DTOs, etc.).

The core never imports a JPA entity, a MapStruct mapper, a REST DTO, or a COBOL bridge class. Those live in the adapters, swapped by Spring's profile mechanism: stubs are active by default (`!integrated & !production`), real implementations activate only when their environment is configured.

Additional considerations for the adapter layer:

- **Stub adapters may contain business logic** — Stubs approximate the real COBOL programs for local dev and CI. They necessarily encode domain rules (validation, pricing, execution math) so the system works end-to-end without external dependencies. These rules are *approximations* and may drift from the COBOL canonical logic. Treat stubs as dev-time stand-ins, not as source of truth for business rules.

- **Inline mapping vs. dedicated mappers** — Adapters may map directly between infrastructure types and domain records inline when the conversion is trivial (a handful of field assignments). A dedicated MapStruct mapper is preferred when the mapping is non-trivial, shared across multiple methods, or would clutter the adapter's readability.

- **Placeholders for values enriched later** — Adapters should avoid making domain decisions, but returning placeholder values (e.g. `BigDecimal.ZERO`) for fields the adapter cannot populate — because the data source doesn't carry them yet — is a valid workaround.

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

## Cross-Cutting Concerns

### Error Handling

- When an exception is intentionally swallowed (empty body or comment-only catch block), name the variable `ignored` to signal intent:

    ```java
    } catch (IOException ignored) {
        // Will be cleaned up on next broadcast or timeout
    }
    ```
- Global error handling is centralized in `config.web.ControllerAdvice`, a `@RestControllerAdvice` that returns RFC 9457 problem details via the OpenAPI-generated `Error` model
- Every response includes `timestamp`, `instance` (request URI), `status`, `title`, `detail`, and the OpenTelemetry `trace` ID.
- Logging level matches the HTTP status series: `ERROR` for 5xx, `WARN` for 4xx, `INFO` otherwise.

Example:

```
curl -s http://localhost:8080 | jq; curl -sw "→ HTTP %{http_code}\n" -o /dev/null http://localhost:8080
{
  "detail": "No static resource  for request '/'.",
  "instance": "/",
  "status": 404,
  "title": "Not Found",
  "timestamp": "2026-01-11T20:16:13.240960834Z",
  "trace": "d9178227-18d6-4442-8598-9a9f17f65f9c"
}
→ HTTP 404
```

```mermaid
sequenceDiagram
  participant Client
  participant DS as DispatcherServlet
  participant CA as ControllerAdvice

  Client->>DS: GET /
  DS->>DS: No resource found for "/"
  DS->>CA: throws NoResourceFoundException
  CA->>CA: @ExceptionHandler(NoResourceFoundException.class)
  CA->>CA: buildProblemDetail(e, NOT_FOUND)
  CA->>CA: log.warn("NoResourceFoundException being handled")
  CA-->>DS: ProblemDetail {status: 404, detail, timestamp, trace}
  DS-->>Client: HTTP 404 NOT FOUND + JSON body
```

### Logging

- **Logback configuration** (`logback.xml`) — single `CONSOLE` appender with a custom `MaskingPatternLayout` that redacts sensitive headers and values (`authorization`, `cookie`, `x-api-key`, `password`, `token`, `secret`, etc.) via regex before writing. The pattern includes `trace_id`, `span_id`, and `trace_flags` from MDC.
- **`LogFilter`** — a `OncePerRequestFilter` at highest precedence that logs every incoming request (method, URI, query string, headers) and outgoing response status code.
- **`LogAspect`** — an `@Aspect` targeting all `@RestController` public methods, logging method arguments before invocation and the return value after completion.
- **`OTelApiTraceSpanFilter`** — a `OncePerRequestFilter` at `LOWEST_PRECEDENCE - 1` that injects the current OpenTelemetry trace ID, span ID, and trace flags into SLF4J MDC so the logback pattern can include them.

Example:

```sh
curl -s --request GET \
  --url http://localhost:8080/api/trades/42 \
  --header 'Accept: application/json' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c' \
  --header 'Cookie: JSESSIONID=A1B2C3D4E5F6G7H8I9J0; auth_token=secret123token456' \
  --header 'Proxy-Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=' \
  --header 'User-Agent: Mozilla/5.0 (Test Client)' \
  --header 'X-API-Key: super-secret-api-key' \
  --header 'X-Auth-Token: super-secret-auth-token-12345' \
  --header 'X-CSRF-Token: csrf_abc123def456ghi789' | jq
```

```log
2026-02-18 15:28:11.600 trace_id=b8e1447340832e9b466fde0a1f172b55 span_id=a4fa1234784f7c02 trace_flags=01 INFO  http-nio-8080-exec-1 --- d.p.stonks_java.config.log.LogFilter >>>> Method: GET; URI: /api/trades/42; QueryString: null; Headers: {Host: localhost:8080, Accept: application/json, Authorization: ****, Cookie: ****, Proxy-Authorization: ****, User-Agent: Mozilla/5.0 (Test Client), X-API-Key: ****, X-Auth-Token: ****, X-CSRF-Token: ****
2026-02-18 15:28:11.619 trace_id=b8e1447340832e9b466fde0a1f172b55 span_id=a4fa1234784f7c02 trace_flags=01 INFO  http-nio-8080-exec-1 --- d.p.stonks_java.config.log.LogAspect [TradeController.getTrade(..)] Args: [42]
2026-02-18 15:28:11.620 trace_id=b8e1447340832e9b466fde0a1f172b55 span_id=a4fa1234784f7c02 trace_flags=01 INFO  http-nio-8080-exec-1 --- d.p.stonks_java.config.log.LogAspect [TradeController.getTrade(..)] Response: TradeResponse(id=42, symbol=GMEE, action=BUY, quantity=10, price=420.69, status=ACCEPTED)
2026-02-18 15:28:11.664 trace_id=b8e1447340832e9b466fde0a1f172b55 span_id=a4fa1234784f7c02 trace_flags=01 INFO  http-nio-8080-exec-1 --- d.p.stonks_java.config.log.LogFilter <<<< Response Status: 200
```

```mermaid
sequenceDiagram
participant Client
participant OTelApiTraceSpanFilter
participant LogFilter
participant LogAspect
participant TradeController

Client->>OTelApiTraceSpanFilter: HTTP Request
activate OTelApiTraceSpanFilter

OTelApiTraceSpanFilter->>OTelApiTraceSpanFilter: Set trace_id in MDC

OTelApiTraceSpanFilter->>LogFilter: doFilter(request, response)
activate LogFilter

Note over LogFilter: Log Request Details<br/>(Method, URI, QueryString, Headers)

LogFilter->>LogAspect: @Before advice
activate LogAspect
Note over LogAspect: Log method args

LogAspect->>TradeController: getTrade(42)
activate TradeController
TradeController-->>LogAspect: TradeResponse
deactivate TradeController

LogAspect-->>LogAspect: @AfterReturning advice
Note over LogAspect: Log method response

LogAspect-->>LogFilter: Return response
deactivate LogAspect

Note over LogFilter: Log Response Details<br/>(Status Code)

LogFilter-->>OTelApiTraceSpanFilter: Return response
deactivate LogFilter

OTelApiTraceSpanFilter->>OTelApiTraceSpanFilter: Clear MDC

OTelApiTraceSpanFilter-->>Client: HTTP Response
deactivate OTelApiTraceSpanFilter
```

### Mapping

Object mapping uses **MapStruct** with the Spring component model (`componentModel = SPRING`), making mappers injectable Spring beans. Mappers are organized by adapter layer and direction:

- **REST mappers** (`adapter/in/rest/mapper/`) — convert between OpenAPI-generated DTOs and domain records. Each module (trade, stock, portfolio) has its own `*RestMapper`. Custom `default` methods handle enum conversion via `EnumUtils.fromValue()` and the `ValuedEnum<V>` interface.
- **COBOL mappers** (`adapter/out/cobol/mapper/`) — convert between domain records and COBOL JSON DTOs. The `*CobolMapper` interfaces follow the same MapStruct pattern for both request serialization and response deserialization.
- **JPA mappers** (`adapter/out/jpa/mapper/`) — convert between JPA entities and domain records. `TradeExecutionEntityMapper` builds `TradeHistory` entities from domain objects; `TradeHistoryJpaMapper` maps entities back to domain `TradeHistoryItem` records.
- **Inline mapping** is used instead of a dedicated mapper when the conversion is trivial — directly in the controller or adapter method body.

### OpenAPI-First REST Development

REST endpoints follow an **OpenAPI-first** (contract-first) approach:
1. The API contract is defined in an OpenAPI specification under `src/main/resources/openapi/`.
2. DTOs and server interfaces are generated from that spec into the `generated` module (see [Module Architecture Graph](#module-architecture-graph)).
3. Generated DTOs are the canonical request/response types in the adapter layer and are never modified manually. Controllers implement the generated interfaces.

**Exceptions:** Endpoints whose primary purpose is streaming or real-time communication (e.g., `GET /stream`, `GET /api/trades/paper-tape`) are defined directly as controller methods rather than via OpenAPI, because their response semantics (`SseEmitter`, formatted text lines) do not map cleanly to the OpenAPI 3.x request/response model. Exceptions are kept to a minimum and noted inline in the controller.

---

## End to End Flows

**Diagram convention:** Cross-module calls (service in module A calling into module B) always point at a **port interface** (`StockPortIn`, `TradePortIn`, `NewsPortIn`) — the module boundary contract. Within-module outbound calls (service → its own adapter) use the **adapter implementation** name, which also distinguishes Real vs Stub scenarios. Event-driven flows (SSE via `ApplicationEventPublisher`) use the **service class** as the publisher.

### Trade Validation

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

### Get Market Stocks

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

### Price Simulation (Scheduled, Event-Driven)

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

### Trade Execution

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
    participant SS as StockPortIn
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
    participant SS as StockPortIn
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

### Get Portfolio

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
    participant SS as StockPortIn
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

### Get Trade History

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

### SSE Streaming (Real-Time Broadcast)

`GET /stream` opens a Server-Sent Events connection that pushes real-time market events to clients. The `BroadcastSseService` maintains a thread-safe list of connected emitters and listens to Spring application events from the `stock`, `trade`, and `chaos` modules.

#### Event Flow

```mermaid
sequenceDiagram
    participant Client as SSE Client
    box "broadcast: Adapter In"
    participant BC as BroadcastController
    end
    box "broadcast: Application"
    participant BS as BroadcastSseService
    end
    box "stock: Application"
    participant SS as StockService
    end
    box "trade: Application"
    participant TS as TradeService
    end
    box "chaos: Application"
    participant CS as ChaosService
    end

    Client->>BC: GET /stream
    BC->>BS: createEmitter()
    BS->>BS: new SseEmitter (300s timeout)
    BS-->>BC: SseEmitter
    BC-->>Client: event: connected

    Note over BS: @Scheduled heartbeat every 15s

    Note over SS: Price tick simulation
    SS->>SS: publishEvent(StockPriceUpdatedEvent)
    BS->>BS: @EventListener StockPriceUpdatedEvent
    BS->>BS: build PriceTickBroadcastEvent
    BS->>Client: event: PRICE_TICK<br/>data: {symbol, price, change, changePercent}

    Note over TS: Trade execution
    TS->>TS: publishEvent(TradeExecutedEvent)
    BS->>BS: @EventListener TradeExecutedEvent
    BS->>BS: build TradeExecutedBroadcastEvent
    BS->>Client: event: TRADE_EXECUTED<br/>data: {result, symbol, quantity, paperTape}

    Note over CS: Chaos event triggered
    CS->>CS: publishEvent(ChaosEventTriggered)
    BS->>BS: @EventListener ChaosEventTriggered
    BS->>BS: build ChaosBroadcastEvent
    BS->>Client: event: CHAOS_EVENT<br/>data: {headline, symbol, impact, explanation}

    Note over BS: Dead emitter cleanup<br/>onCompletion / onTimeout / onError
```

#### SSE Event Types

| Event | Source | Data Payload |
|-------|--------|--------------|
| `PRICE_TICK` | Stock module (every simulation tick) | `{symbol, name, price, change, changePercent, timestamp}` |
| `TRADE_EXECUTED` | Trade module (on accepted trade) | `{result, symbol, quantity, paperTape}` |
| `CHAOS_EVENT` | Chaos module (on triggered event) | `{headline, symbol, impact, explanation}` |

### Paper Tape

`GET /api/trades/paper-tape` returns trade history in a retro 3270-terminal-style formatted view. The `BroadcastSseService` fetches trade history from `TradePortIn`, formats each entry as a paper tape line, and returns paginated results.

#### Flow

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "broadcast: Adapter In"
    participant BC as BroadcastController
    end
    box "broadcast: Application"
    participant BS as BroadcastSseService
    end
    box "trade: Application"
    participant TI as TradePortIn
    end

    Client->>BC: GET /api/trades/paper-tape?page=0&size=20
    BC->>BS: getPaperTape(pageable)
    BS->>TI: getTradeHistory(pageable)
    TI-->>BS: TradeHistoryItem page
    loop For each trade
        BS->>BS: format: "TRADE #0042 &#124; BUY 10 GMEE @ $47.85 &#124; TOTAL: $478.50"
        BS->>BS: build PaperTapeEntry(seq, formattedLine, executedAt)
    end
    BS-->>BC: Page<PaperTapeEntry>
    BC->>BC: map(PaperTapeEntry → PaperTapeResponseData)
    BC-->>Client: 200 PaperTapeResponse
```

### Chaos Event Generation

The chaos module injects AI-generated madness into the market. Events are triggered either by the `ChaosEventScheduler` (based on the current chaos level's `aiEventIntervalMs`) or manually via `POST /api/chaos/events`. Each event transforms real-world news headlines into a memeified price impact on a target stock, adjusts the stock price, and broadcasts the event via SSE.

#### Flow (Scheduled + Manual)

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "chaos: Adapter In"
    participant CES as ChaosEventScheduler
    participant CC as ChaosController
    end
    box "chaos: Application"
    participant CS as ChaosService
    end
    box "news: Application"
    participant NI as NewsPortIn
    end
    box "stock: Application"
    participant SI as StockPortIn
    end
    box "chaos: Adapter Out"
    participant CEG as ChaosEventGenerator
    end
    box "broadcast: Application"
    participant BSS as BroadcastSseService
    end

    Note over CES: Every ${stonks.chaos.event-check-interval-ms} (default 10s)
    Note over CES: Check if enabled && elapsed >= level.aiEventIntervalMs

    alt Scheduled trigger
        CES->>CS: triggerEvent()
    else Manual trigger
        Client->>CC: POST /api/chaos/events
        CC->>CS: triggerEvent()
    end

    Note over CS: 1. Fetch context
    CS->>NI: getHeadlines()
    NI-->>CS: List<NewsHeadline> (RSS feeds, cached 60s)
    CS->>SI: getStocks()
    SI-->>CS: List<StockPrice>

    Note over CS: 2. Generate chaos event
    CS->>CEG: generate(headlines, stocks)
    Note over CEG: [default] Stub → canned random +15-35% event<br/>[integrated/production] CompositeAdapter:<br/>  1. OpenRouter AI via Spring AI ChatModel<br/>  2. On failure: Fallback catalog (15 built-in events)
    CEG-->>CS: ChaosEvent

    Note over CS: 3. Apply price impact
    CS->>SI: applyImpact(event.symbol, event.impactPercent)
    Note over SI: newPrice = currentPrice × (1 + impactPercent/100)<br/>publishEvent(StockPriceUpdatedEvent)
    SI-->>CS: void

    Note over CS: 4. Publish event & store history
    CS->>CS: publishEvent(ChaosEventTriggered)
    CS->>CS: history.add(event) [bounded to 100]
    BSS->>BSS: @EventListener ChaosEventTriggered
    BSS->>BSS: build ChaosBroadcastEvent
    BSS-->>BSS: SSE broadcast to clients
    CS-->>CC: ChaosEvent
    CC->>CC: map → ChaosEventTriggeredResponse
    CC-->>Client: 200 ChaosEventTriggeredResponse
```

#### Dev Stub Scenario (no AI)

```mermaid
sequenceDiagram
    box "chaos: Adapter In"
    participant CES as ChaosEventScheduler
    end
    box "chaos: Application"
    participant CS as ChaosService
    end
    box "chaos: Adapter Out"
    participant CEGS as ChaosEventGeneratorStub
    end

    Note over CES: Every 10s (if interval elapsed)
    CES->>CS: triggerEvent()
    CS->>CEGS: generate(headlines, stocks)
    Note over CEGS: Always returns:<br/>headline: "Meme Stonks Go Brrr!"<br/>impact: random +15% to +35%<br/>symbol: random from stock list
    CEGS-->>CS: ChaosEvent
```

### Chaos Level Management

`GET /api/chaos/level` returns the current chaos level (default `PAPER_HANDS`). `POST /api/chaos/level` changes it. The level controls how frequently chaos events are generated and modulates market volatility.

#### Chaos Levels

| Level | tickIntervalMs | volatilityMultiplier | aiEventIntervalMs |
|-------|---------------|---------------------|-------------------|
| `PAPER_HANDS` | 30,000 ms | 1.0× | 600,000 ms (10 min) |
| `MODERATE` | 15,000 ms | 2.0× | 120,000 ms (2 min) |
| `HIGH_VOLATILITY` | 5,000 ms | 5.0× | 30,000 ms (30 s) |
| `EXTREME` | 2,000 ms | 12.5× | 15,000 ms (15 s) |
| `MAXIMUM_OVERDRIVE` | 1,000 ms | 25.0× | 10,000 ms (10 s) |

#### Flow

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "chaos: Adapter In"
    participant CC as ChaosController
    end
    box "chaos: Application"
    participant CS as ChaosService
    end

    Client->>CC: GET /api/chaos/level
    CC->>CS: getCurrentLevel()
    Note over CS: AtomicReference<ChaosLevel>, defaults to PAPER_HANDS
    CS-->>CC: ChaosLevel
    CC-->>Client: 200 ChaosLevelResponse

    Client->>CC: POST /api/chaos/level (body: "EXTREME")
    CC->>CC: EnumUtils.fromValue(ChaosLevel.class, body)
    CC->>CS: setLevel(EXTREME)
    CS->>CS: currentLevel.set(EXTREME)
    CC-->>Client: 200 ChaosLevelResponse
```

### Chaos Event History

`GET /api/chaos/events` and `GET /api/chaos/history` return the in-memory history of triggered chaos events. The history is bounded to 100 entries (FIFO eviction).

#### Flow

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    box "chaos: Adapter In"
    participant CC as ChaosController
    end
    box "chaos: Application"
    participant CS as ChaosService
    end

    Client->>CC: GET /api/chaos/events
    CC->>CS: getHistory()
    CS->>CS: history.getAll() → List.copyOf (immutable snapshot)
    CS-->>CC: List<ChaosEvent>
    CC-->>Client: 200 ChaosEventsResponse
```

### News Headline Fetching

The news module is a leaf module (`@ApplicationModule(allowedDependencies = {})`) that fetches and caches headlines from RSS feeds. It exposes `NewsPortIn` to other modules — primarily consumed by `ChaosService` during event generation.

The flow is triggered when `ChaosService.triggerEvent()` calls `newsPortIn.getHeadlines()`. Headlines are cached for 60 seconds via Caffeine (`@Cacheable(value = "headlines", unless = "#result.isEmpty()")`). Subsequent calls within the TTL return cached data without a fresh fetch.

#### Real Scenario (RSS)

```mermaid
sequenceDiagram
    box "chaos: Application"
    participant CS as ChaosService
    end
    box "news: Application"
    participant NS as NewsService
    end
    box "news: Adapter Out"
    participant NRCA as NewsRssClientAdapter
    participant NSM as NewsSyndMapper
    end
    participant RSS as News RSS Feeds

    Note over CS: triggerEvent() needs headlines
    CS->>NS: getHeadlines()

    Note over NS: @Cacheable("headlines")<br/>Cache miss → fetch
    NS->>NRCA: fetchHeadlines()
    loop For each feed URL (Reuters, BBC Tech, TechCrunch)
        NRCA->>RSS: GET (RestClient)
        RSS-->>NRCA: RSS XML
        NRCA->>NRCA: parseFeed() — SyndFeedInput
        NRCA->>NSM: map(SyndEntry → NewsHeadline)
        NSM-->>NRCA: NewsHeadline
    end

    Note over NRCA: Per-feed error isolation<br/>Failed feeds return empty,<br/>others unaffected

    NRCA-->>NS: List<NewsHeadline> (merged, per-feed dedup)
    Note over NS: Deduplicate by title (case-insensitive)<br/>LinkedHashMap preserves insertion order
    Note over NS: Cache result for 60s

    NS-->>CS: List<NewsHeadline>

    Note over CS,NS: Subsequent calls within 60s TTL
    CS->>NS: getHeadlines()
    Note over NS: @Cacheable → cache hit<br/>Returns cached headlines<br/>No RSS fetch
    NS-->>CS: List<NewsHeadline> (cached)
```

#### Dev Stub Scenario (no RSS)

```mermaid
sequenceDiagram
    box "chaos: Application"
    participant CS as ChaosService
    end
    box "news: Application"
    participant NS as NewsService
    end
    box "news: Adapter Out"
    participant NCS as NewsClientStub
    end

    CS->>NS: getHeadlines()
    NS->>NCS: fetchHeadlines()
    Note over NCS: AtomicInteger counter cycles<br/>through 3 batches of 2 headlines
    Note over NCS: Batch 0: "Fed Holds..." + "Tech Stocks..."<br/>Batch 1: "Oil Prices..." + "Bitcoin $100K..."<br/>Batch 2: "Housing Market..." + "Retail Sales..."
    NCS-->>NS: List<NewsHeadline> (rotating batch)
    NS-->>CS: List<NewsHeadline>
```

### News as Chaos Context

The headlines fetched by the news module are passed as context to the chaos event generator. This flow shows how raw news headlines become part of an AI-generated or catalog-based chaos event. The details of generation differ by profile, but the headline-handoff is identical.

```mermaid
sequenceDiagram
    box "chaos: Application"
    participant CS2 as ChaosService
    end
    box "news: Application"
    participant NS2 as NewsService
    end
    box "stock: Application"
    participant SI as StockPortIn
    end
    box "chaos: Adapter Out"
    participant CEG as ChaosEventGeneratorPortOut
    end

    CS2->>NS2: getHeadlines()
    NS2-->>CS2: List<NewsHeadline> (cached or fresh)

    CS2->>SI: getStocks()
    SI-->>CS2: List<StockPrice>

    CS2->>CEG: generate(headlines, stocks)

    alt OpenRouter AI (integrated/production)
        Note over CEG: Builds LLM prompt with headline<br/>titles + sources to inspire<br/>meme-worthy chaos event
    else Fallback catalog (integrated/production)
        Note over CEG: Randomly picks headline title<br/>as sourceHeadline, selects event<br/>from 15 hardcoded meme scenarios
    else Stub (default/dev)
        Note over CEG: Randomly picks headline title<br/>as sourceHeadline for<br/>"Meme Stonks Go Brrr!" event
    end

    CEG-->>CS2: ChaosEvent (with sourceHeadline)
```

### RSS Configuration

```yaml
stonks:
  news:
    rss:
      feed-urls:
        - https://feeds.reuters.com/reuters/businessNews
        - https://feeds.bbci.co.uk/news/technology/rss.xml
        - https://techcrunch.com/feed/
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500, expireAfterWrite=60s
```

| Property | Value | Description |
|----------|-------|-------------|
| RSS Feed URLs | Reuters Business, BBC Tech, TechCrunch | Fetched in parallel via `RestClient`, per-feed error isolation |
| Cache Provider | Caffeine | In-memory, concurrent, high-performance |
| Cache TTL | 60s (`expireAfterWrite`) | Stale headlines acceptable for chaos event generation |
| Max Cache Size | 500 entries | Safety ceiling (unlikely to be hit) |
| Empty Result | Not cached (`unless = "#result.isEmpty()"`) | Avoids caching transient network failures |

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

### Acceptable Gaps

Some code paths are intentionally left untested. These fall into the following categories:

1. **Preconditions validated at a higher layer** — A branch in a stub adapter (e.g., null action or negative quantity) may be unreachable from E2E tests because the REST layer rejects the request with `@Valid` / `@Min` before it reaches the adapter. Testing these paths through the stub directly would duplicate the validation contract.

2. **Safety guards** — Branches that exist as defensive checks against programming errors. They are never expected to trigger in normal operation. Testing them would require artificially corrupting internal state.

3. **MapStruct-generated null guards** — Generated mapper implementations contain null checks on every parameter. These branches would only fire if a null value propagates past compile-time type safety, indicating a bug upstream. Testing each null guard individually adds noise without signal.

4. **Environment-dependent error paths** — Process timeout handling in `CobolProgramExecutor` relies on `Process.waitFor(timeout, unit)` and `Process.destroyForcibly()`. These JDK APIs behave correctly on standard Linux runtimes but may block on constrained environments (e.g., BusyBox on NixOS). The timeout logic is tested at the code level; the integration test for this specific path is omitted where the OS cannot reliably kill orphaned child processes.

These gaps keep the test suite focused on business logic regressions rather than infrastructure edge cases that are better caught by the JVM or static analysis.

### Documenting Test Rationale

Every non-E2E test must explain why E2E was not chosen via a class-level `//` comment. This keeps the testing strategy self-documenting as the suite grows. Common reasons:

- **Profile-gated adapter** — only active under `integrated`/`production` profiles, never loaded in default (stubs) E2E context
- **Pure algorithm** — no I/O or framework dependencies; E2E adds no value over isolated edge-case testing
- **Infrastructure/OS concern** — exercises real subprocess spawning or OS behavior that COBOL stubs bypass
- **Push-based/async behavior** — SSE, heartbeats, event listeners not exercisable through HTTP request-response
- **Structural check** — compile/verify-time architecture enforcement, not behavioral
- **Pure utility / design constraint** — zero-dependency static method or single-assertion guard

### E2E Test Data

Each test declares its data needs via `@Sql` referencing reusable SQL fixtures in `src/test/resources/sql/`:

```
sql/
├── portfolio.sql                       # baseline portfolio ($10k cash)
├── portfolio-with-position.sql         # portfolio + GMEE position (qty 10)
├── portfolio-with-limited-position.sql # portfolio + GMEE position (qty 3)
└── portfolio-with-history.sql          # portfolio + 2 trade history entries
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

### `@ApplicationModuleTest` Context Loading

`@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES)` loads **only** the module being tested and its **direct** dependencies — not transitive ones. This means:

```
broadcast → trade → stock
```

When testing `broadcast`, only `broadcast` and `trade` are loaded. `stock` (a transitive dependency) is **not** loaded, causing `NoSuchBeanDefinitionException` for any beans from `stock` that `trade` requires.

**Rule of thumb:** Use `@SpringBootTest` instead of `@ApplicationModuleTest` for modules at the top of the dependency graph that transitively depend on multiple other modules. The `ModulithVerificationTest` still validates module boundaries separately.

### Running Tests

```bash
./gradlew test                          # all tests
./gradlew test --tests TradeHistoryE2eTest  # single class
./gradlew jacocoTestCoverageVerification    # coverage check
```

---

## Integrations

### Data Storage

| Database | Purpose | Status | Connection |
|----------|---------|--------|------------|
| H2 (in-memory) | Development & Testing | Implemented | Auto-configured via `application.yaml` |
| PostgreSQL | Production | Pending | `application-production.yaml` (driver not yet in `build.gradle`) |

**Migration Tool:** Flyway — Pending, not implemented yet

**JDBC Drivers:**
- H2: `com.h2database:h2` (active)
- PostgreSQL: `org.postgresql:postgresql` (pending)

### Monitoring & Observability

| Component | Technology | Status |
|-----------|------------|--------|
| Logging | Logback via Spring Boot + custom `MaskingPatternLayout` | Implemented |
| Tracing | OpenTelemetry (trace ID in responses & MDC logs) | Implemented |
| Metrics | Micrometer with Prometheus registry | Pending |
| Visualization | Grafana Dashboards | Pending |

### External Services & APIs

| Integration | Purpose | Status |
|-------------|---------|--------|
| COBOL Programs | Trade validation, price engine, portfolio management, catalog | Implemented via `CobolProgramExecutor` (stdin/stdout JSON) |
| OpenRouter (AI) | AI-powered chaos event generation via Spring AI `ChatModel` | Implemented via `spring-ai-openai` |
| RSS News Feeds | Real-world headline fetching for chaos event context | Implemented via ROME (Reuters, BBC Tech, TechCrunch) |

### CI/CD & Deployment

**Status:** Pending

**Planned:**
- Docker image build via Gradle
- Docker Compose deployment alongside other services in this repo
- CI pipeline for automated testing and coverage verification
