---
name: java-spring-boot-modulith
description: Guidelines for developing in the stonks_java Spring Boot Modulith project. Covers hexagonal architecture, module structure, naming conventions, environment profiles, COBOL integration, SSE streaming, chaos/event generation, news RSS, testing approach, OpenAPI-first REST development, and cross-cutting concerns. Load when working on any Java/Spring Boot code in the stonks_java/ directory.
license: MIT
compatibility: opencode
---

## What I do

Provides architectural guidelines and conventions for the `stonks_java/` Spring Boot Modulith project — a Meme Stock Trading Simulator with AI-Driven Chaos that bridges REST APIs to COBOL programs via stdin/stdout JSON, streams real-time events via SSE, and enriches chaos with RSS news headlines and AI-generated events.

## When to use me

Load this skill whenever working on Java/Spring Boot code in the `stonks_java/` directory. This includes:

- Adding new features or modules
- Modifying existing controllers, services, ports, or adapters
- Writing or updating tests
- Refactoring code structure
- Understanding project architecture

## Instructions

### Environment Configuration

Each external dependency is toggled independently via `stonks.adapters.*` properties:

| Property | Values | Default | Description |
|----------|--------|---------|-------------|
| `stonks.adapters.db` | `h2`, `postgresql` | `h2` | Database backend |
| `stonks.adapters.cobol` | `stub`, `real` | `stub` | COBOL process execution |
| `stonks.adapters.ai` | `stub`, `real` | `stub` | AI chaos event generation (OpenRouter) |
| `stonks.adapters.news` | `stub`, `real` | `stub` | RSS news headline fetching |
| `stonks.adapters.otel` | `true`, `false` | `false` | OpenTelemetry metrics export |

- Stub adapters: `@ConditionalOnProperty(prefix = "stonks.adapters", name = "...", havingValue = "stub", matchIfMissing = true)`
- Real backends: `@ConditionalOnProperty(prefix = "stonks.adapters", name = "...", havingValue = "real")`
- `./gradlew bootRun` — H2 + stubs
- `./gradlew bootRun --args='--stonks.adapters.cobol=real'` — H2 + real COBOL only
- `./gradlew bootRun --args='--stonks.adapters.db=postgresql --stonks.adapters.cobol=real --stonks.adapters.ai=real --stonks.adapters.news=real --stonks.adapters.otel=true'` — PG + all real backends + OTel
- `./gradlew test` — H2 + stubs, CI-ready

### Architecture: Hexagonal with Modulith

**Core rule:** The application core (`application/`) imports ONLY:
- Domain records (`domain/`) — plain Java, zero framework coupling
- Port interfaces (`application/port/in/`, `application/port/out/`) — contracts, not implementations

Everything else lives in the **adapter layer** (`adapter/in/`, `adapter/out/`). The core never sees infrastructure types — no JPA entities, MapStruct mappers, REST DTOs, or COBOL bridge classes.

#### Where to relax purity

- **Framework annotations in core** — `@Transactional`, `@PostConstruct`, Spring scheduling go on service layer when they express a *business concern* (e.g., "this operation must be atomic", "the catalog must be loaded at startup"). If they describe *how* (e.g., connection pool settings), they belong in an adapter.
- **Framework types in core** — Stable types like `Page`, `Pageable` are OK when a hand-rolled equivalent adds zero semantic value.
- **Direct Spring injection** — `ApplicationEventPublisher` and similar stable interfaces may be injected directly without a port wrapper if the framework interface already expresses business intent.
- **Consolidated ports** — Group related read+write operations that share transaction boundaries. Avoid "one method per operation" ports that add indirection without clarity.

#### Adapter layer

- **`adapter/in/`** — REST controllers, scheduled task runners, SSE publishers. Translates external protocol into core service calls and maps responses back to transport DTOs.
- **`adapter/out/`** — JPA repository adapters, COBOL process adapters, RSS client adapters, and their stub counterparts. Each implements a port interface from the core and translates between domain records and infrastructure-specific types.

**Adapter considerations:**
- **Stubs may contain business logic** — Stub adapters approximate real COBOL programs for local dev/CI. They encode domain rules (validation, pricing, execution math) but are *approximations*, not canonical business logic.
- **Inline mapping vs. dedicated mappers** — Map directly between infrastructure types and domain records inline when conversion is trivial. Use a dedicated MapStruct mapper when mapping is non-trivial, shared across methods, or would clutter readability.
- **Placeholder values** — Adapters may return placeholder values (e.g., `BigDecimal.ZERO`) for fields the data source cannot yet populate, but should avoid making domain decisions.

### Inter-Module Communication Rules

When writing code that crosses module boundaries, follow these patterns:

- **Cross-module calls** — Always go through a **port interface** (`StockPortIn`, `TradePortIn`, `NewsPortIn`). Never inject another module's service or adapter directly.
- **Event-driven flows** — Use `ApplicationEventPublisher` for cross-module events (`StockPriceUpdatedEvent`, `TradeExecutedEvent`, `ChaosEventTriggered`). Pub/sub via Spring events, not direct method calls.
- **Module dependency direction** — Modules can only depend on modules they declare as dependencies.
- **`@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES)`** only loads the module under test and its direct dependencies. For modules at the top of the dependency graph (e.g., `broadcast`), use `@SpringBootTest` instead. `ModulithVerificationTest` separately validates module boundaries.

### Naming Convention

Formula: `{Module}{Concept}{Layer}[Technology]`

| Part | Meaning | Examples |
|------|---------|----------|
| `Module` | Spring Modulith module | `Stock`, `Trade`, `Portfolio`, `Chaos`, `Broadcast`, `News` |
| `Concept` | What the class does (omit when unambiguous) | `Catalog`, `PriceEngine`, `Validator`, `History`, `Event`, `Sse` |
| `Layer` | Hexagonal role | `PortIn`, `PortOut`, `Controller`, `Service`, `Adapter`, `Mapper`, `Repository` |
| `[Technology]` | Implementation detail (optional) | `Cobol`, `Jpa`, `Rest` |

**Ports** — NEVER use `[Technology]` suffix. Technology-agnostic contracts:
`StockPortIn`, `StockPriceEnginePortOut`, `TradePortIn`, `TradeValidationPortOut`, `TradeExecutionPortOut`, `TradeHistoryPortOut`, `TradePortfolioStatePortOut`, `PortfolioPortOut`, `NewsPortIn`

**Adapters** — Technology-specific implementations:
`StockCatalogCobolAdapter`, `StockPriceEngineCobolAdapterStub`, `TradeValidatorCobolAdapter`, `TradePortfolioMgrCobolAdapter`, `TradePortfolioMgrCobolAdapterStub`, `TradeHistoryJpaAdapter`, `TradePortfolioStateJpaAdapter`, `PortfolioJpaAdapter`

**Repositories & Mappers:**
`PortfolioPositionJpaRepository`, `TradePortfolioJpaRepository`, `TradeValidatorCobolMapper`, `TradeExecutionEntityMapper`, `TradeHistoryJpaMapper`

**Controllers & Services:**
`StockController`, `TradeController`, `PortfolioController`, `ChaosController`, `BroadcastController`, `StockService`, `TradeService`, `PortfolioService`, `ChaosService`, `BroadcastSseService`, `NewsService`

### Module Boundaries

| Module | Exposes (PortIn) | Depends on | Key responsibilities |
|--------|-------------------|------------|-----------------------|
| stock | `StockPortIn` | cobol, config, generated | Catalog, price engine, simulation orchestration |
| trade | `TradePortIn` | stock | Validation, execution, history |
| portfolio | `PortfolioPortIn` | stock | Cash, positions, P&L |
| broadcast | — | stock, trade, chaos | SSE streaming, paper tape |
| chaos | — | news, stock | Event generation, level management |
| news | `NewsPortIn` | *(none — leaf module)* | RSS headline fetching, caching |

Shared OPEN modules: **cobol** (process bridge), **config** (web filters, Jackson, scheduling, error handling, OTel, AOP logging, log masking), **generated** (OpenAPI DTOs), **util** (ValuedEnum, metadata)

**Concurrency constraints within `stock`:** `StockService.simulate()` and `StockService.applyImpact()` share a `ReentrantLock`. `StockPriceTickScheduler` uses `AtomicBoolean` guard against overlapping ticks. Per-stock failures are skipped — remaining stocks retain previous price.

### Scheduling Constraints

These parameters have inter-dependencies — changing one may require adjusting others:

| Parameter | Default | Constraint |
|-----------|---------|------------|
| `stonks.market.simulation.interval-ms` | 5,000 ms | ≥ max tick duration (11 COBOL spawns × per-program timeout). |
| `stonks.chaos.event-check-interval-ms` | 30,000 ms | ≤ smallest `aiEventIntervalMs` across chaos levels |
| `stonks.broadcast.sse-timeout-ms` | 300,000 ms | > `heartbeat-rate-ms` |
| `stonks.broadcast.heartbeat-rate-ms` | 15,000 ms | < `sse-timeout-ms` |
| News cache TTL | 60s | ≤ `aiEventIntervalMs` at aggressive chaos levels |
| COBOL program timeout | 5s per program | < `simulation.interval-ms`; total per-tick is 1+N programs × 5s |

### Cross-Cutting Concerns

#### Error Handling

- When an exception is intentionally swallowed (empty body or comment-only catch block), name the variable `ignored`:
  ```java
  } catch (IOException ignored) {
      // Will be cleaned up on next broadcast or timeout
  }
  ```
- Global error handling in `config.web.ControllerAdvice` (`@RestControllerAdvice`)
- Returns RFC 9457 problem details via OpenAPI-generated `Error` model
- Every response includes: `timestamp`, `instance`, `status`, `title`, `detail`, `trace` ID
- Logging: ERROR for 5xx, WARN for 4xx, INFO otherwise

#### Logging

- `logback.xml` — custom `MaskingPatternLayout` redacts sensitive headers/values (`authorization`, `cookie`, `x-api-key`, `password`, `token`, `secret`). Pattern includes `trace_id`, `span_id`, `trace_flags` from MDC.
- `LogFilter` — `OncePerRequestFilter` at highest precedence logs every request (method, URI, query string, headers) and response status
- `LogAspect` — `@Aspect` logging all `@RestController` method args and responses
- `OTelApiTraceSpanFilter` — `OncePerRequestFilter` at `LOWEST_PRECEDENCE - 1` injects trace_id, span_id, trace_flags into MDC

#### Mapping

MapStruct with Spring component model (`componentModel = SPRING`):

- **REST mappers** (`adapter/in/rest/mapper/`) — OpenAPI DTOs ↔ domain records. Custom `default` methods handle enum conversion via `EnumUtils.fromValue()` and `ValuedEnum<V>`.
- **COBOL mappers** (`adapter/out/cobol/mapper/`) — domain records ↔ COBOL JSON DTOs
- **JPA mappers** (`adapter/out/jpa/mapper/`) — JPA entities ↔ domain records
- **Inline mapping** for trivial conversions — directly in controller or adapter method body

#### OpenAPI-First REST Development

- REST endpoints follow an **OpenAPI-first** (contract-first) approach:
  1. Define the API contract in an OpenAPI spec under `src/main/resources/openapi/`.
  2. Generate DTOs and server interfaces into the `generated` module.
  3. Generated DTOs are the canonical request/response types — never modify them manually.
  4. Controllers implement the generated server interfaces.
- **Exceptions:** SSE/streaming endpoints (`GET /stream`, `GET /api/trades/paper-tape`) whose response semantics (`SseEmitter`, formatted text lines) don't map to OpenAPI 3.x are defined directly as controller methods. Keep exceptions minimal and document inline.

### Testing Approach

Tests run against H2 with COBOL stubs active by default.

| Layer | Tool | Purpose |
|-------|------|---------|
| E2E | `@ApplicationModuleTest` + `RestTestClient` | Full HTTP flow through module boundaries |
| Unit | `@ExtendWith(MockitoExtension.class)` | Stubbed adapter logic, edge cases not reachable via E2E |
| Integration | Plain JUnit | COBOL executor, process spawning |
| Architecture | `ApplicationModules.verify()` | Modulith boundary enforcement — single `ModulithVerificationTest`; do not create additional modulith verification tests |

**E2E tests are the default.** If a scenario can be tested end-to-end, it should be.

**Unit tests fill gaps** for classes not initialized when stubs are active (e.g., real COBOL adapters). Prefer `@InjectMocks` + `@Mock` fields over manual `setUp()` with `new`. MapStruct mappers in adapter tests use `@Spy` with generated `Impl` class.

**Integration tests are minimal** — exactly one test for `CobolProgramExecutor`. No additional integration tests planned.

**Coverage thresholds are suggestions**, not hard rules. Adjust up or down as the codebase evolves.

**Acceptable testing gaps:**
1. **Preconditions validated at a higher layer** — e.g., null action or negative quantity unreachable from E2E because `@Valid`/`@Min` rejects first.
2. **Safety guards** — Defensive checks against programming errors that shouldn't trigger in normal operation.
3. **MapStruct-generated null guards** — Generated null checks; would only fire if null propagates past compile-time type safety.
4. **Environment-dependent error paths** — e.g., `Process.destroyForcibly()` on BusyBox/NixOS.

**Red flags (may indicate over-testing):**
- Mockito `reset()` — usually means shared mutable state or testing multiple scenarios in one method
- Reflection to bypass `private` — consider testing through public contract instead

**Document test rationale:** Every non-E2E test must explain why E2E was not chosen via a class-level `//` comment. Common reasons:
- **Profile-gated adapter** — only active under `integrated`/`production`
- **Pure algorithm** — no I/O or framework dependencies
- **Infrastructure/OS concern** — real subprocess spawning
- **Push-based/async behavior** — SSE, heartbeats, event listeners
- **Structural check** — compile/verify-time architecture enforcement
- **Pure utility / design constraint** — zero-dependency static method

**Test data:** Use `@Sql` with reusable fixtures in `src/test/resources/sql/`:
- `portfolio.sql` — baseline portfolio ($10k cash)
- `portfolio-with-position.sql` — portfolio + GMEE position (qty 10)
- `portfolio-with-limited-position.sql` — portfolio + GMEE position (qty 3)
- `portfolio-with-history.sql` — portfolio + 2 trade history entries

Tests needing empty DB use inline cleanup:
```java
@Sql(statements = {"DELETE FROM trade_history", "DELETE FROM position", "DELETE FROM portfolio"})
```

**What to avoid in tests:**
- `@DirtiesContext`
- `@TestPropertySource`
- `@ActiveProfiles`
- Repository autowiring for setup

**Running tests:**
```bash
./gradlew test                          # all tests
./gradlew test --tests TradeHistoryE2eTest  # single class
./gradlew jacocoTestCoverageVerification    # coverage check
```

### Integrations

| Integration | Status | Notes |
|-------------|--------|-------|
| H2 | Implemented | Auto-configured via `application.yaml` |
| PostgreSQL | Pending | Driver not yet in `build.gradle` |
| Flyway | Pending | Not yet implemented |
| COBOL Programs | Implemented | `CobolProgramExecutor` — stdin/stdout JSON over OS process |
| OpenRouter (AI) | Implemented | `spring-ai-openai` — chaos event generation |
| RSS News Feeds | Implemented | ROME — Reuters, BBC Tech, TechCrunch |
| Caffeine Cache | Implemented | News headline cache (`unless = "#result.isEmpty()"`) |
| OpenTelemetry | Implemented | Trace ID in responses & MDC logs |
| Micrometer/Prometheus | Pending | Not yet implemented |
| Grafana Dashboards | Pending | Not yet implemented |
| Docker/CI-CD | Pending | Not yet implemented |