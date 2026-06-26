# stonks_java — Spring Boot Backend

Orchestrates the stonks-simulator: exposes REST APIs, runs the market simulation loop, and bridges requests to COBOL programs via **stdin/stdout JSON over OS process execution**.

---

## Environments

Each external dependency is toggled independently via `stonks.adapters.*` properties. Run with no flags for the default dev experience (H2 + all stubs), or opt into individual real backends as needed.

| Property | Values | Default | Description |
|----------|--------|---------|-------------|
| `stonks.adapters.db` | `h2`, `postgresql` | `h2` | Database backend |
| `stonks.adapters.cobol` | `stub`, `real` | `stub` | COBOL process execution |
| `stonks.adapters.ai` | `stub`, `real` | `stub` | AI chaotic event generation (OpenRouter) |
| `stonks.adapters.news` | `stub`, `real` | `stub` | RSS news headline fetching |
| `stonks.adapters.otel` | `true`, `false` | `false` | OpenTelemetry metrics export |

- **`./gradlew bootRun`** — starts with H2 + all stubs, no external dependencies needed.
- **`./gradlew bootRun --args='--stonks.adapters.cobol=real'`** — starts with H2 + real COBOL only.
- **`./gradlew bootRun --args='--stonks.adapters.cobol=real --stonks.adapters.ai=real --stonks.adapters.news=real'`** — starts with H2 + all real backends.
- **`./gradlew bootRun --args='--stonks.adapters.db=postgresql --stonks.adapters.cobol=real --stonks.adapters.ai=real --stonks.adapters.news=real --stonks.adapters.otel=true'`** — starts with PostgreSQL + all real backends + OTel.
- **`./gradlew test`** — runs against H2 + stubs. CI-ready, zero config.

### How it works

- `application.yaml` declares `stonks.adapters.*` defaults (all stubs, H2, OTel off).
- An `EnvironmentPostProcessor` (`StonksAdapterEnvironmentPostProcessor`) maps `stonks.adapters.db` to the correct `spring.datasource.*` and `spring.jpa.*` properties before Spring Boot auto-configuration runs.
- Stub adapters use `@ConditionalOnProperty(prefix = "stonks.adapters", name = "...", havingValue = "stub", matchIfMissing = true)`.
- Real backends use `@ConditionalOnProperty(prefix = "stonks.adapters", name = "...", havingValue = "real")`.
- OTel export is controlled by the placeholder `${stonks.adapters.otel:false}` in `management.otlp.metrics.export.enabled`.

---

## Architecture: Hexagonal Architecture with Modulith approach

### Module Architecture Graph

```mermaid
graph TB
    broadcast["broadcast<br/><small>SSE streaming · paper tape · event hub</small>"]
    stock["stock<br/><small>catalog · price engine · REST · orchestration</small>"]
    trade["trade<br/><small>execution · history</small>"]
    chaosevent["chaosevent<br/><small>event generation · AI · incident log</small>"]
    intensity["intensity<br/><small>level management · volatility control</small>"]
    news["news<br/><small>RSS headline fetching · caching</small>"]
    portfolio["portfolio<br/><small>cash · positions · P&L</small>"]
    db[("DB<br/><small>portfolio · position<br/>trade_history · stock_price<br/>intensity_level · chaosevent_incident_log</small>")]

    subgraph OPEN ["Shared OPEN Modules"]
        cobol["cobol<br/><small>CobolAppPortOut · CobolProgramExecutor<br/>COBOL process bridge</small>"]
        generated["generated<br/><small>OpenAPI DTOs</small>"]
        util["util<br/><small>ValuedEnum · metadata</small>"]
    end

    config["config<br/><small>web filters · jackson · scheduling ·<br/>error handling · OTel tracing ·<br/>AOP logging · log masking</small>"]

    broadcast ---> stock
    broadcast ---> trade
    broadcast ---> chaosevent
    broadcast ---> portfolio

    trade ---> stock
    chaosevent ---> news
    chaosevent ---> stock
    chaosevent ---> intensity
    intensity ---> stock
    portfolio ---> stock

    portfolio -.->|reads/writes| db
    trade -.->|reads/writes| db
    intensity -.->|reads/writes| db
    chaosevent -.->|reads/writes| db

    style stock stroke-width:2px
    style cobol fill:#e6f3ff,stroke:#4a9eff
    style generated fill:#e6f3ff,stroke:#4a9eff
    style util fill:#e6f3ff,stroke:#4a9eff
```

> **Note:** `config` is a cross-cutting shared package (not annotated `@ApplicationModule`) containing web filters, Jackson config, scheduling, error handling, OTel tracing, AOP logging, and log masking. Every module may depend on it implicitly.

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
- **`adapter/out/`** — JPA repository adapters (entity mapping, query execution), COBOL process adapters (serialization, process spawning, deserialization), and their stub counterparts used in development mode. Each adapter implements a port interface from the core and translates between domain records and infrastructure-specific types (entities, COBOL JSON DTOs, etc.).

The core never imports a JPA entity, a MapStruct mapper, a REST DTO, or a COBOL bridge class. Those live in the adapters, swapped by `@ConditionalOnProperty` on `stonks.adapters.*` properties: stubs are active by default, real implementations activate only when explicitly configured.

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
`StockPortIn`, `StockCatalogPortOut`, `StockPricePortOut`, `StockPriceEnginePortOut`, `TradeExecutionPortOut`, `TradeHistoryPortOut`, `PortfolioPortOut`

**Adapters** — technology-specific implementations of ports:
`StockCatalogCobolAdapter`, `StockPriceEngineCobolAdapterStub`, `StockPriceJpaAdapter`, `TradeHistoryJpaAdapter`, `PortfolioJpaAdapter`

**Repositories & Mappers** — persistence and mapping layer:
`PortfolioPositionJpaRepository`, `TradePortfolioJpaRepository`

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
1. The API contract is defined in an OpenAPI specification at `src/main/resources/openapi.yaml`.
2. DTOs and server interfaces are generated from that spec into the `generated` module (see [Module Architecture Graph](#module-architecture-graph)).
3. Generated DTOs are the canonical request/response types in the adapter layer and are never modified manually. Controllers implement the generated interfaces.

**Exceptions:** Endpoints whose primary purpose is streaming or real-time communication (e.g., `GET /stream`) are defined directly as controller methods rather than via OpenAPI, because their response semantics (`SseEmitter`) do not map cleanly to the OpenAPI 3.x request/response model. Exceptions are kept to a minimum and noted inline in the controller.

---

## End to End Flows

This section presents a comprehensive Graphviz diagram covering all request/response flows, module boundaries, adapter selection (real vs. stub), and external integrations in a single view. Render it with a Graphviz viewer (e.g., [`dot -Tpng -o flows.png request-flows-paths-monochrome.dot`](docs/request-flows-paths-monochrome.dot)) or paste into an online renderer.

```dot
digraph stonks_request_flows {
  rankdir=LR;
  splines=ortho;
  compound=true;
  label="stonks_java — Request/Response Flow Paths";
  labelloc=t;
  labeljust=l;
  fontname="Consolas";
  fontcolor="#c9d1d9";
  bgcolor="#0d1117";
  pad=0.5;
  nodesep=0.5;
  ranksep=1.0;
  newrank=true;

  // ── Color palette ──
  // Neutral=#30363d  Text=#8b949e      White=#c9d1d9
  // Gold=#bc8c00     Green=#1a7f37     Purple=#6e40c9   Red=#bd2c00

  node [fontname="Consolas", fontcolor="#c9d1d9"];

  // ═══════════════════════════════════════════════
  //  ACTORS / EXTERNAL
  // ═══════════════════════════════════════════════
  node [shape=box, style="filled,rounded", penwidth=2, fontsize=10, fillcolor="#1c2128", color="#c9d1d9"];
  client [label="HTTP Client\n(Browser / curl)"];

  node [shape=cylinder, style=filled, penwidth=2, fontsize=10, fillcolor="#161b22", color="#30363d"];
  db [label="H2 / PostgreSQL"];

  node [shape=box, style="filled,rounded", penwidth=2, fontsize=10, fillcolor="#161b22", color="#bd2c00"];
  cobol_backend [label="COBOL Backend\n(z/OS)"];

  node [shape=box, style="filled,rounded", penwidth=2, fontsize=10, fillcolor="#161b22", color="#6e40c9"];
  ai_api [label="OpenRouter AI\n(LLM API)"];

  node [shape=box, style="filled,rounded", penwidth=2, fontsize=10, fillcolor="#161b22", color="#1a7f37"];
  rss_feed [label="RSS News\nFeed(s)"];

  // ═══════════════════════════════════════════════
  //  STOCK MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_stock {
    label="stock module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=9, fillcolor="#0d4194", color="#30363d", margin="0.15,0.05"];
    stock_ctrl [label="StockController\nGET /api/stocks"];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    stock_port_in [label="StockPortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    stock_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>StockService</B></FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">simulate() · getStocks()</FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">← ApplyStockImpact (event)</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=7, fillcolor="#0d1117", color="#30363d", margin="0.08,0.03"];
    stock_cat_port [label="StockCatalog\nPortOut"];
    stock_eng_port [label="StockPriceEngine\nPortOut"];
    stock_price_port [label="StockPrice\nPortOut"];

    // Real adapters (cobol=real) → connect to CobolAppPortOut
    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    stock_cat_real [label="StockCatalogCobolAdapter\n→ CobolAppPortOut.execute()"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    stock_eng_real [label="StockPriceEngineCobolAdapter\n→ CobolAppPortOut.execute()"];

    // Stub adapters (cobol=stub) → standalone, no COBOL call
    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#bc8c00", margin="0.1,0.04"];
    stock_cat_stub [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="1" CELLPADDING="2">
        <TR><TD>StockCatalogCobolAdapterStub</TD></TR>
        <TR><TD><FONT POINT-SIZE="7" COLOR="#bc8c00">returns hardcoded stocks</FONT></TD></TR>
      </TABLE>
    >];

    stock_eng_stub [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="1" CELLPADDING="2">
        <TR><TD>StockPriceEngineCobolAdapterStub</TD></TR>
        <TR><TD><FONT POINT-SIZE="7" COLOR="#bc8c00">random walk simulation</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    stock_jpa [label="StockPriceJpaAdapter"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    stock_sched [label="StockPriceTickScheduler\n(scheduled: 5s)"];
  }

  // ═══════════════════════════════════════════════
  //  NEWS MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_news {
    label="news module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    news_port_in [label="NewsPortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    news_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>NewsService</B></FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">getHeadlines() [cached 60s]</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=7, fillcolor="#0d1117", color="#30363d", margin="0.08,0.03"];
    news_client_port [label="NewsClient\nPortOut"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    news_rss [label="NewsRssClientAdapter\n→ RSS feed HTTP"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#bc8c00", margin="0.1,0.04"];
    news_stub [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="1" CELLPADDING="2">
        <TR><TD>NewsClientStub</TD></TR>
        <TR><TD><FONT POINT-SIZE="7" COLOR="#bc8c00">4 canned headlines</FONT></TD></TR>
      </TABLE>
    >];
  }

  // ═══════════════════════════════════════════════
  //  INTENSITY MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_intensity {
    label="intensity module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=9, fillcolor="#0d4194", color="#30363d", margin="0.15,0.05"];
    intensity_ctrl [label="IntensityController\nGET+POST /api/intensity-level"];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    intensity_port_in [label="IntensityPortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    intensity_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>IntensityService</B></FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=7, fillcolor="#0d1117", color="#30363d", margin="0.08,0.03"];
    intensity_level_port [label="IntensityLevel\nPortOut"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    intensity_jpa [label="IntensityLevelJpaAdapter"];
  }

  // ═══════════════════════════════════════════════
  //  PORTFOLIO MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_portfolio {
    label="portfolio module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=9, fillcolor="#0d4194", color="#30363d", margin="0.15,0.05"];
    portfolio_ctrl [label="PortfolioController\nGET /api/portfolio"];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    portfolio_port_in [label="PortfolioPortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    portfolio_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>PortfolioService</B></FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=7, fillcolor="#0d1117", color="#30363d", margin="0.08,0.03"];
    portfolio_port_out [label="Portfolio\nPortOut"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    portfolio_jpa [label="PortfolioJpaAdapter"];
  }

  // ═══════════════════════════════════════════════
  //  TRADE MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_trade {
    label="trade module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=9, fillcolor="#0d4194", color="#30363d", margin="0.15,0.05"];
    trade_ctrl [label="TradeController\nGET+POST /api/trades"];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    trade_port_in [label="TradePortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    trade_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>TradeService</B></FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">executeTrade()</FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">getTradeHistory()</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=7, fillcolor="#0d1117", color="#30363d", margin="0.08,0.03"];
    trade_exec_port [label="TradeExecution\nPortOut"];
    trade_hist_port [label="TradeHistory\nPortOut"];
    trade_state_port [label="TradePortfolioState\nPortOut"];

    // Real adapters
    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    trade_cobol_real [label="TradePortfolioMgrCobolAdapter\n→ CobolAppPortOut.execute()"];

    // Stub adapters
    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#bc8c00", margin="0.1,0.04"];
    trade_cobol_stub [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="1" CELLPADDING="2">
        <TR><TD>TradePortfolioMgrCobolAdapterStub</TD></TR>
        <TR><TD><FONT POINT-SIZE="7" COLOR="#bc8c00">in-memory validation</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    trade_hist_jpa [label="TradeHistoryJpaAdapter"];
    trade_state_jpa [label="TradePortfolioStateJpaAdapter"];
  }

  // ═══════════════════════════════════════════════
  //  CHAOSEVENT MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_chaos {
    label="chaosevent module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=9, fillcolor="#0d4194", color="#30363d", margin="0.15,0.05"];
    chaos_ctrl [label="ChaoseventController\nGET+POST /api/chaotic-events"];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    chaos_port_in [label="ChaoseventPortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    chaos_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>ChaoseventService</B></FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">triggerEvent()</FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">getHistory()</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=7, fillcolor="#0d1117", color="#30363d", margin="0.08,0.03"];
    chaos_gen_port [label="ChaoticEventGenerator\nPortOut"];
    chaos_inc_port [label="ChaoticIncident\nPortOut"];

    // Real path: Composite → OpenRouter (+ Fallback safety net)
    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    chaos_composite [label="ChaoticEventGenerator\nCompositeAdapter\n(@Primary, ai=real)"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    chaos_openrouter [label="OpenRouterAdapter\n→ OpenRouter LLM API"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    chaos_fallback [label="FallbackAdapter\n(template fallback)"];

    // Stub path: standalone, no AI call
    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#bc8c00", margin="0.1,0.04"];
    chaos_stub [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="1" CELLPADDING="2">
        <TR><TD>ChaoticEventGeneratorStub</TD></TR>
        <TR><TD><FONT POINT-SIZE="7" COLOR="#bc8c00">template-based events</FONT></TD></TR>
      </TABLE>
    >];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    chaos_jpa [label="ChaoticIncidentJpaAdapter"];

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    chaos_sched [label="ChaoseventScheduler\n(scheduled: 30s)"];
  }

  // ═══════════════════════════════════════════════
  //  BROADCAST MODULE
  // ═══════════════════════════════════════════════
  subgraph cluster_broadcast {
    label="broadcast module";
    fontcolor="#8b949e";
    fontsize=11;
    style=filled;
    color="#30363d";
    fillcolor="#0d1117";
    penwidth=2;
    margin=20;

    node [shape=box, style="filled,rounded", penwidth=2, fontsize=9, fillcolor="#0d4194", color="#30363d", margin="0.15,0.05"];
    broadcast_ctrl [label="BroadcastController\nGET /api/stream (SSE)"];

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d1117", color="#30363d", margin="0.1,0.04"];
    broadcast_port_in [label="BroadcastPortIn"];

    node [shape=box, style="filled,rounded", penwidth=4, fontsize=9, fillcolor="#0d1117", color="#30363d", margin="0.2,0.1"];
    broadcast_svc [label=<
      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="2" CELLPADDING="2">
        <TR><TD><FONT POINT-SIZE="11" COLOR="#8b949e"><B>BroadcastSseService</B></FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">createEmitter()</FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">← StockPriceUpdatedEvent</FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">← TradeExecutedEvent</FONT></TD></TR>
        <TR><TD><FONT POINT-SIZE="8" COLOR="#8b949e">← ChaoticEventTriggered</FONT></TD></TR>
      </TABLE>
    >];
  }

  // ═══════════════════════════════════════════════
  //  SHARED / OPEN MODULES
  // ═══════════════════════════════════════════════
  subgraph cluster_cobol {
    label="cobol (shared open module)";
    fontcolor="#8b949e";
    fontsize=10;
    style=dashed;
    color="#30363d";
    bgcolor="#0d1117";

    node [shape=box, style="filled,rounded", penwidth=1, fontsize=8, fillcolor="#0d4194", color="#30363d", margin="0.1,0.04"];
    cobol_port_out [label="CobolAppPortOut"];
    cobol_exec [label="CobolProgramExecutor"];
  }

  //  ── EDGE STYLE REFERENCE ──
  //  HTTP request   → solid, thick, white
  //  Internal       → solid, thin, gray (all delegation, port out, adapter, JPA)
  //  Stub adapter   → solid, thin, gold
  //  Domain event   → dashed, green
  //  SSE            → dotted, purple
  //  DB query       → dotted, gray
  //  COBOL          → solid, thin, red
  //  AI LLM API     → dashed, purple

  // ═══════════════════════════════════════════════
  //  FLOW 1: GET /api/stocks
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=2.5, color="#c9d1d9", fontsize=8, fontcolor="#c9d1d9"];
  client -> stock_ctrl [label="1. GET /api/stocks", weight=5];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  stock_ctrl -> stock_port_in;
  stock_port_in -> stock_svc;

  // Service → PortOuts
  edge [style=dashed, penwidth=1, color="#8b949e"];
  stock_svc -> stock_cat_port [label="@PostConstruct"];
  stock_svc -> stock_eng_port [label="simulate()"];

  // StockCatalogPortOut fans out to real + stub
  edge [style=solid, penwidth=1.5, color="#8b949e"];
  stock_cat_port -> stock_cat_real [label="cobol=real"];
  edge [style=solid, penwidth=1.5, color="#bc8c00"];
  stock_cat_port -> stock_cat_stub [label="cobol=stub"];

  // Real path: adapter → CobolAppPortOut.execute() → CobolProgramExecutor → COBOL
  edge [style=solid, penwidth=1.5, color="#bd2c00"];
  stock_cat_real -> cobol_port_out [label="execute('catalog')"];

  // StockPriceEnginePortOut fans out
  edge [style=solid, penwidth=1.5, color="#8b949e"];
  stock_eng_port -> stock_eng_real [label="cobol=real"];
  edge [style=solid, penwidth=1.5, color="#bc8c00"];
  stock_eng_port -> stock_eng_stub [label="cobol=stub"];

  edge [style=solid, penwidth=1.5, color="#bd2c00"];
  stock_eng_real -> cobol_port_out [label="execute('price-engine')"];

  // StockPricePortOut → JPA (only one impl, no stub)
  edge [style=dashed, penwidth=1.5, color="#8b949e"];
  stock_svc -> stock_price_port [label="load/save"];
  stock_price_port -> stock_jpa;

  edge [style=dotted, penwidth=1.5, color="#8b949e"];
  stock_jpa -> db;

  // ═══════════════════════════════════════════════
  //  FLOW 2: Stock price tick (scheduled)
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=1.5, color="#8b949e", fontsize=8, fontcolor="#8b949e"];
  stock_sched -> stock_port_in [label="2. simulate()", weight=5];

  edge [style=dashed, penwidth=1.5, color="#1a7f37"];
  stock_svc -> broadcast_svc [label="StockPriceUpdatedEvent", constraint=false];

  // ═══════════════════════════════════════════════
  //  FLOW 3: POST /api/trades
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=2.5, color="#c9d1d9", fontsize=8, fontcolor="#c9d1d9"];
  client -> trade_ctrl [label="3. POST /api/trades", weight=5];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  trade_ctrl -> trade_port_in;
  trade_port_in -> trade_svc;

  edge [style=dashed, penwidth=1, color="#8b949e"];
  trade_svc -> trade_exec_port;
  trade_svc -> trade_hist_port;
  trade_svc -> trade_state_port;

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  trade_svc -> stock_port_in [label="getStocks()", constraint=false];

  // TradeExecutionPortOut fans out to real + stub
  edge [style=solid, penwidth=1.5, color="#8b949e"];
  trade_exec_port -> trade_cobol_real [label="cobol=real"];
  edge [style=solid, penwidth=1.5, color="#bc8c00"];
  trade_exec_port -> trade_cobol_stub [label="cobol=stub"];

  edge [style=solid, penwidth=1.5, color="#bd2c00"];
  trade_cobol_real -> cobol_port_out [label="execute('portfolio-mgr')"];

  edge [style=dashed, penwidth=1.5, color="#8b949e"];
  trade_state_port -> trade_state_jpa;
  trade_hist_port -> trade_hist_jpa;

  edge [style=dotted, penwidth=1.5, color="#8b949e"];
  trade_hist_jpa -> db;
  trade_state_jpa -> db;

  edge [style=dashed, penwidth=1.5, color="#1a7f37"];
  trade_svc -> broadcast_svc [label="TradeExecutedEvent", constraint=false];

  // ═══════════════════════════════════════════════
  //  FLOW 4: POST /api/chaotic-events
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=2.5, color="#c9d1d9", fontsize=8, fontcolor="#c9d1d9"];
  client -> chaos_ctrl [label="4. POST /api/chaotic-events", weight=5];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  chaos_ctrl -> chaos_port_in;
  chaos_port_in -> chaos_svc;

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  chaos_svc -> stock_port_in [label="getStocks()", constraint=false];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  chaos_svc -> news_port_in [label="getHeadlines()", constraint=false];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  news_port_in -> news_svc;

  edge [style=dashed, penwidth=1, color="#8b949e"];
  news_svc -> news_client_port;

  // NewsClientPortOut fans out
  edge [style=solid, penwidth=1.5, color="#8b949e"];
  news_client_port -> news_rss [label="news=real"];
  edge [style=solid, penwidth=1.5, color="#bc8c00"];
  news_client_port -> news_stub [label="news=stub"];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  news_rss -> rss_feed [style=dashed, color="#1a7f37"];

  edge [style=dashed, penwidth=1, color="#8b949e"];
  chaos_svc -> chaos_gen_port;
  chaos_svc -> chaos_inc_port;

  // ChaoticEventGeneratorPortOut fans out
  edge [style=solid, penwidth=1.5, color="#8b949e"];
  chaos_gen_port -> chaos_composite [label="ai=real"];
  edge [style=solid, penwidth=1.5, color="#bc8c00"];
  chaos_gen_port -> chaos_stub [label="ai=stub"];

  edge [style=solid, penwidth=1, color="#8b949e"];
  chaos_composite -> chaos_openrouter;
  chaos_composite -> chaos_fallback;

  chaos_openrouter -> ai_api [label="LLM call", style=dashed, color="#6e40c9"];

  edge [style=dashed, penwidth=1.5, color="#8b949e"];
  chaos_inc_port -> chaos_jpa;

  edge [style=dotted, penwidth=1.5, color="#8b949e"];
  chaos_jpa -> db;

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  chaos_sched -> chaos_port_in [label="trigger()"];

  edge [style=dashed, penwidth=1.5, color="#1a7f37"];
  chaos_svc -> stock_svc [label="ApplyStockImpact", constraint=false];

  edge [style=dashed, penwidth=1.5, color="#1a7f37"];
  chaos_svc -> broadcast_svc [label="ChaoticEventTriggered", constraint=false];

  // ═══════════════════════════════════════════════
  //  FLOW 5: GET /api/portfolio
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=2.5, color="#c9d1d9", fontsize=8, fontcolor="#c9d1d9"];
  client -> portfolio_ctrl [label="5. GET /api/portfolio", weight=5];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  portfolio_ctrl -> portfolio_port_in;
  portfolio_port_in -> portfolio_svc;

  edge [style=dashed, penwidth=1, color="#8b949e"];
  portfolio_svc -> portfolio_port_out;

  edge [style=dashed, penwidth=1.5, color="#8b949e"];
  portfolio_port_out -> portfolio_jpa;

  edge [style=dotted, penwidth=1.5, color="#8b949e"];
  portfolio_jpa -> db;

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  portfolio_svc -> stock_port_in [label="getStocks()", constraint=false];

  // ═══════════════════════════════════════════════
  //  FLOW 6: GET /api/intensity-level
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=2.5, color="#c9d1d9", fontsize=8, fontcolor="#c9d1d9"];
  client -> intensity_ctrl [label="6. GET intensity", weight=5];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  intensity_ctrl -> intensity_port_in;
  intensity_port_in -> intensity_svc;

  edge [style=dashed, penwidth=1, color="#8b949e"];
  intensity_svc -> intensity_level_port;

  edge [style=dashed, penwidth=1.5, color="#8b949e"];
  intensity_level_port -> intensity_jpa;

  edge [style=dotted, penwidth=1.5, color="#8b949e"];
  intensity_jpa -> db;

  // ═══════════════════════════════════════════════
  //  FLOW 7: SSE stream
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=2.5, color="#c9d1d9", fontsize=8, fontcolor="#c9d1d9"];
  client -> broadcast_ctrl [label="7. GET /api/stream (SSE)", weight=5];

  edge [style=solid, penwidth=1.5, color="#8b949e"];
  broadcast_ctrl -> broadcast_port_in;
  broadcast_port_in -> broadcast_svc;

  edge [style=dotted, penwidth=2, color="#6e40c9"];
  broadcast_svc -> client [label="SSE: price ticks, trades, chaos"];

  // ═══════════════════════════════════════════════
  //  CobolAppPortOut → CobolProgramExecutor → COBOL Backend
  // ═══════════════════════════════════════════════
  edge [style=solid, penwidth=1, color="#bd2c00"];
  cobol_port_out -> cobol_exec [label="implements"];
  cobol_exec -> cobol_backend [label="IPC / socket call"];

  // ═══════════════════════════════════════════════
  //  LEGEND
  // ═══════════════════════════════════════════════
  subgraph cluster_legend {
    label="Legend";
    fontcolor="#8b949e";
    fontsize=10;
    style=dashed;
    color="#30363d";
    bgcolor="#0d1117";

    node [shape=plaintext, fontsize=8, fontcolor="#c9d1d9", fillcolor="transparent"];
    legend_http [label="White thick   → HTTP request/response"];
    legend_gray [label="Gray           → All internal calls"];
    legend_gold [label="Gold           → Stub adapter (active when property=stub)"];
    legend_db  [label="Dotted         → Database query"];
    legend_evt [label="Green dashed   → Domain event (ApplicationEventPublisher)"];
    legend_sse [label="Purple dotted  → SSE stream"];
    legend_cobol [label="Red            → COBOL backend"];
    legend_ai   [label="Purple dashed  → AI / external API"];
    legend_note [label="All module borders and nodes are neutral gray.\nColor is reserved for entry/exit, stubs, events, SSE, and externals."];
  }

  // Layout constraints
  { rank=same; client; }
  { rank=same; stock_ctrl; trade_ctrl; chaos_ctrl; portfolio_ctrl; intensity_ctrl; broadcast_ctrl; }
  { rank=same; stock_svc; trade_svc; chaos_svc; portfolio_svc; intensity_svc; broadcast_svc; news_svc; }
  { rank=same; stock_jpa; trade_hist_jpa; trade_state_jpa; portfolio_jpa; intensity_jpa; chaos_jpa; }
  { rank=same; db; cobol_backend; ai_api; rss_feed; }
}
```

---

## Testing Approach

Tests run against H2 with COBOL stubs active by default — zero external dependencies.

### Folder Structure (test-type-first)

Tests are organized by **type**, not by production package depth. The folder name signals intent so individual comments can stay focused on behavior.

```
src/test/java/dev/pollito/stonks_java/
├── architecture/   # Structural / smoke tests
├── e2e/            # Full application (@SpringBootTest + HTTP)
├── module/         # Module-isolated (@ApplicationModuleTest + HTTP or port-in)
├── integration/    # Partial context, real DB / subprocess
├── adapter_contract/  # Mocked contract tests for @ConditionalOnProperty adapters
├── unit/           # Pure JUnit / Mockito — zero Spring context
├── edge_case/      # Scenarios hard to trigger through HTTP
└── testsupport/    # Shared helpers (e.g. RestTestClientAssertions)
```

### Naming Conventions

| Suffix | Meaning | Example |
|--------|---------|---------|
| `*E2eTest` | Full app `@SpringBootTest` + HTTP | `BroadcastE2eTest` |
| `*ModuleTest` | `@ApplicationModuleTest` + HTTP or port-in | `PortfolioModuleTest` |
| `*IntegrationTest` | Partial context, real DB/subprocess | `CobolProgramExecutorIntegrationTest` |
| `*MockTest` | Mocked contract for disabled adapters | `TradePortfolioMgrCobolAdapterMockTest` |
| `*Test` | Pure unit, utility, or edge case | `EnumUtilsTest`, `ControllerAdviceTest` |

### `@ApplicationModuleTest` vs `@SpringBootTest`

Use `@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES)` when the module under test only needs its own beans plus directly-injected dependencies. Use `@SpringBootTest` only when the test needs the **full application context** — e.g. `broadcast` listens to cross-module Spring events from `stock` and `chaosevent`, which are not direct bean dependencies and won't be loaded by `DIRECT_DEPENDENCIES`.

When `@ApplicationModuleTest` is used from outside the module's package (e.g. flat `module/` folder), declare the target module explicitly:

```java
@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT, module = "trade")
```

### Test Hierarchy

**Module and E2E tests are the default.** They exercise the full request-to-response path. If a scenario can be tested through HTTP, it should be.

**`adapter_contract/` tests are second-class.** They verify adapter wiring and mapping for beans gated by `@ConditionalOnProperty` (e.g. `stonks.adapters.cobol=real`) that are never loaded in the default test profile. These are isolated mock tests — not first-class behavioral tests. They exist because the real adapters can't be reached from E2E, not because they're preferable to it.

**`unit/` and `edge_case/` tests fill genuine gaps.** Some logic is unreachable from E2E because stubs don't exercise certain branches (e.g. deduplication, edge-case exception handlers) or because the behavior is push-based (SSE) rather than request-response. Class-level comments explain **what behavior is under test** and **why that behavior can't be reached from a higher layer** — they never repeat "this is not an E2E test."

**MapStruct mappers in adapter tests use `@Spy` with the generated `*Impl` class** rather than `@Mock`, so real mapping logic is exercised even in mocked adapter tests.

**Integration tests are minimal.** There is exactly one test for `CobolProgramExecutor` — verifying process spawning, stdin/stdout JSON, and timeout handling. No additional integration tests are planned; the COBOL bridge is a stable concern.

### Coverage

Coverage thresholds are a suggestion, not a hard rule. Adjust them up or down as the codebase evolves. The goal is to catch regressions, not to chase a number.

### Acceptable Gaps

Some code paths are intentionally left untested:

1. **Preconditions validated at a higher layer** — A branch in a stub adapter (e.g. null action) may be unreachable from E2E because the REST layer rejects it with `@Valid` before it reaches the adapter. Testing these paths through the stub directly would duplicate the validation contract.

2. **Safety guards** — Branches that exist as defensive checks against programming errors. They are never expected to trigger in normal operation. Testing them would require artificially corrupting internal state.

3. **MapStruct-generated null guards** — Generated mapper implementations contain null checks on every parameter. These branches would only fire if a null value propagates past compile-time type safety, indicating a bug upstream. Testing each null guard individually adds noise without signal.

4. **Environment-dependent error paths** — Process timeout handling in `CobolProgramExecutor` relies on `Process.waitFor(timeout, unit)` and `Process.destroyForcibly()`. These JDK APIs behave correctly on standard Linux runtimes but may block on constrained environments (e.g. BusyBox on NixOS). The timeout logic is tested at the code level; the integration test for this specific path is omitted where the OS cannot reliably kill orphaned child processes.

These gaps keep the test suite focused on business logic regressions rather than infrastructure edge cases that are better caught by the JVM or static analysis.

### Red Flags — You May Be Testing Too Much

- **Mockito `reset()`** — Resetting a mock between tests usually means the test is sharing mutable state or testing multiple scenarios in a single method.
- **Reflection to bypass `private`** — Using `Field.setAccessible(true)` or `ReflectionTestUtils` to reach private fields/methods often indicates the class under test has too many internal responsibilities, or the test is coupling to implementation details. If you need to reach inside a class, consider whether the behavior can be exercised through its public contract instead.
- **Pure unit tests for trivial behavior** — If a test mocks every dependency and only verifies that `A.f()` calls `B.g()`, it's probably not adding value over an integration or module test.
- **Tests for scenarios that can never happen** — If a code path is unreachable from any real execution (not just "hard to trigger" but actually impossible), the test is documenting dead code, not verifying behavior.

These aren't hard bans — there are legitimate edge cases — but they should prompt a second look at the test and the design it's testing.

### E2E Test Data

Each test declares its data needs via `@Sql` referencing reusable SQL fixtures in `src/test/resources/sql/`:

```
sql/
├── portfolio.sql                       # baseline portfolio ($10k cash)
├── portfolio-with-position.sql         # portfolio + GMEE position (qty 10)
├── portfolio-with-multiple-positions.sql  # portfolio + GMEE (10) + DOGE (50)
├── portfolio-with-limited-position.sql # portfolio + GMEE position (qty 3)
├── portfolio-with-history.sql          # portfolio + 2 trade history entries
├── intensity-level.sql                 # intensity_level table
└── chaosevent-incident-log.sql         # chaosevent_incident_log table
```

Tests that need an empty DB use inline cleanup statements:
```java
@Sql(statements = {"DELETE FROM trade_history", "DELETE FROM position", "DELETE FROM portfolio"})
```

### What to Avoid

- **`@DirtiesContext`** — SQL fixtures + per-test isolation make context rebuilds unnecessary
- **`@TestPropertySource`** — `src/test/resources/application.yaml` overrides `spring.sql.init.mode=never` globally for tests
- **`@ActiveProfiles`** — not needed; default properties (stubs + H2) are what tests need
- **Repository autowiring for setup** — data is declarative, not constructed in test methods

### Running Tests

```bash
./gradlew test                              # all tests
./gradlew test --tests TradeModuleTest      # single class
./gradlew jacocoTestCoverageVerification    # coverage check
```

### Karate Smoke Tests (Manual / Live Server)

In addition to the automated JUnit suite, there is a separate **Karate** smoke test pack for manual sanity checks against a **live** running server (`localhost:8080` by default). These tests live in `src/karate/` and are **completely isolated** from `./gradlew test` — they do not affect JaCoCo coverage, are excluded from the `check` lifecycle, and never fail the build.

They are structured as reusable helpers (`features/helpers/*.feature`) that individual scenarios `call read`, plus a master `smoke-flow.feature` that chains all manual-test steps end-to-end.

```bash
# 1. Start the app in another terminal
./gradlew bootRun

# 2. Run the Karate smoke tests
./gradlew karateTest

# 3. View the pretty HTML report
open build/karate-reports/karate-summary.html
```

To target a different environment:
```bash
./gradlew karateTest -Dkarate.env=staging
```

The `karate.env` property switches `baseUrl` in `karate-config.js` (default: `http://localhost:8080`).

---

## Integrations

### Data Storage

| Database | Purpose | Status | Connection |
|----------|---------|--------|------------|
| H2 (in-memory) | Development & Testing | Implemented | Auto-configured via `application.yaml` |
| PostgreSQL | Production | Pending | `StonksAdapterEnvironmentPostProcessor` sets PG datasource when `stonks.adapters.db=postgresql` (driver not yet in `build.gradle`) |

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
| COBOL Programs | Price engine, portfolio management, catalog | Implemented via `CobolProgramExecutor` (stdin/stdout JSON) |
| OpenRouter (AI) | AI-powered chaotic event generation via Spring AI `ChatModel` | Implemented via `spring-ai-openai` |
| RSS News Feeds | Real-world headline fetching for chaotic event context | Implemented via ROME (BBC Tech, TechCrunch, Guardian Business) |

### CI/CD & Deployment

**Status:** Pending

**Planned:**
- Docker image build via Gradle
- Docker Compose deployment alongside other services in this repo
- CI pipeline for automated testing and coverage verification
