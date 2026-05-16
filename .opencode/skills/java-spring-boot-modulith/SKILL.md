---
name: java-spring-boot-modulith
description: Guidelines for developing in the stonks_java Spring Boot Modulith project. Covers hexagonal architecture, module structure, naming conventions, environment profiles, COBOL integration, testing approach, and cross-cutting concerns. Load when working on any Java/Spring Boot code in the stonks_java/ directory.
license: MIT
compatibility: opencode
---

## What I do

Provides architectural guidelines and conventions for the `stonks_java/` Spring Boot Modulith project — a Meme Stock Trading Simulator with AI-Driven Chaos that bridges REST APIs to COBOL programs via stdin/stdout JSON.

## When to use me

Load this skill whenever working on Java/Spring Boot code in the `stonks_java/` directory. This includes:

- Adding new features or modules
- Modifying existing controllers, services, ports, or adapters
- Writing or updating tests
- Refactoring code structure
- Understanding project architecture

## Instructions

### Environment Profiles

Three runtime profiles control dependencies:

| Profile | DB | COBOL | OTel | Use case |
|---------|----|-------|------|----------|
| *(none)* | H2 (embedded) | Stubs (Java in-memory) | Disabled | Default for local dev & `./gradlew test` |
| `cobol` | H2 (embedded) | Real COBOL process execution | Disabled | Manual testing with COBOL setup |
| `production` | PostgreSQL | Real COBOL process execution | Enabled | Production/staging |

- Stub adapters: `@Profile("!cobol & !production")`
- Real COBOL adapters: `@Profile({"cobol", "production"})`
- `./gradlew bootRun` — H2 + stubs
- `./gradlew bootRun --spring.profiles.active=cobol` — H2 + real COBOL
- `./gradlew test` — H2 + stubs, CI-ready

### Architecture: Hexagonal with Modulith

**Core rule:** The application core (`application/`) imports ONLY:
- Domain records (`domain/`) — plain Java, zero framework coupling
- Port interfaces (`application/port/in/`, `application/port/out/`) — contracts, not implementations

Everything else lives in the **adapter layer** (`adapter/in/`, `adapter/out/`). The core never sees infrastructure types.

#### Where to relax purity

- **Framework annotations in core** — `@Transactional`, `@PostConstruct`, Spring scheduling go on service layer when expressing business concern
- **Framework types in core** — Stable types like `Page`, `Pageable` are OK when hand-rolled equivalent adds zero value
- **Direct Spring injection** — `ApplicationEventPublisher` and similar stable interfaces may be injected directly
- **Consolidated ports** — Group related read+write operations that share transaction boundaries

#### Adapter layer split

- **`adapter/in/`** — REST controllers, scheduled task runners, SSE publishers
- **`adapter/out/`** — JPA repository adapters, COBOL process adapters, stub counterparts

### Naming Convention

Formula: `{Module}{Concept}{Layer}[Technology]`

| Part | Meaning | Examples |
|------|---------|----------|
| `Module` | Spring Modulith module | `Stock`, `Trade`, `Portfolio` |
| `Concept` | What the class does (omit when unambiguous) | `Catalog`, `PriceEngine`, `Validator`, `History` |
| `Layer` | Hexagonal role | `PortIn`, `PortOut`, `Controller`, `Service`, `Adapter`, `Mapper`, `Repository` |
| `[Technology]` | Implementation detail (optional) | `Cobol`, `Jpa`, `Rest` |

**Ports** — NEVER use `[Technology]` suffix. Technology-agnostic contracts:
`StockPortIn`, `StockPriceEnginePortOut`, `TradeValidationPortOut`, `PortfolioPortOut`

**Adapters** — Technology-specific implementations:
`StockCatalogCobolAdapter`, `StockPriceEngineCobolAdapterStub`, `TradeHistoryJpaAdapter`

**Repositories & Mappers:**
`PortfolioPositionJpaRepository`, `TradeValidatorCobolMapper`

**Controllers & Services:**
`StockController`, `TradeService`, `PortfolioService`

### Cross-Cutting Concerns

#### Error Handling

- Global error handling in `config.web.ControllerAdvice` (`@RestControllerAdvice`)
- Returns RFC 9457 problem details via OpenAPI-generated `Error` model
- Every response includes: `timestamp`, `instance`, `status`, `title`, `detail`, `trace` ID
- Logging: ERROR for 5xx, WARN for 4xx, INFO otherwise

#### Logging

- `logback.xml` — custom `MaskingPatternLayout` redacts sensitive headers/values
- `LogFilter` — `OncePerRequestFilter` logs every request/response
- `LogAspect` — `@Aspect` logging all `@RestController` method args and responses
- `OTelApiTraceSpanFilter` — injects trace_id, span_id, trace_flags into MDC

#### Mapping

MapStruct with Spring component model (`componentModel = SPRING`):

- **REST mappers** (`adapter/in/rest/mapper/`) — OpenAPI DTOs ↔ domain records
- **COBOL mappers** (`adapter/out/cobol/mapper/`) — domain records ↔ COBOL JSON DTOs
- **JPA mappers** (`adapter/out/jpa/mapper/`) — JPA entities ↔ domain records
- **Inline mapping** for trivial conversions

### Testing Approach

Tests run against H2 with COBOL stubs active by default.

| Layer | Tool | Purpose |
|-------|------|---------|
| E2E | `@ApplicationModuleTest` + `RestTestClient` | Full HTTP flow through module boundaries |
| Unit | `@ExtendWith(MockitoExtension.class)` | Stubbed adapter logic, unreachable paths |
| Integration | Plain JUnit | COBOL executor, process spawning |
| Architecture | `ApplicationModules.verify()` | Modulith boundary enforcement |

**E2E tests are the default.** If a scenario can be tested end-to-end, it should be.

**Unit tests fill gaps** for classes not initialized when stubs are active (e.g., real COBOL adapters). MapStruct mappers in COBOL adapter tests use `@Spy` with generated `Impl` class.

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

### Module Structure

Current modules:
- **stock** — catalog, price engine, REST, orchestration
- **trade** — validation, execution, history
- **portfolio** — cash, positions, P&L
- **broadcast** — (placeholder)
- **chaos** — (placeholder)

Shared OPEN modules:
- **cobol** — CobolAppPortOut, CobolProgramExecutor, COBOL process bridge
- **config** — web filters, jackson, scheduling, error handling, OTel tracing, AOP logging, log masking
- **generated** — OpenAPI DTOs
- **util** — ValuedEnum, metadata

### Key Flows

1. **Trade Validation** — `POST /api/trades/validate` validates against business rules, delegates to COBOL `trade-validator` or stub
2. **Get Market Stocks** — `GET /api/market/stocks` returns prices from in-memory map populated at startup
3. **Price Simulation** — Scheduled tick via `StockPriceTickScheduler`, publishes `StockPriceUpdatedEvent`
4. **Trade Execution** — `POST /api/trades` executes BUY/SELL atomically with portfolio state management
5. **Get Portfolio** — `GET /api/portfolio` reads DB + current prices, computes unrealized P&L
6. **Get Trade History** — `GET /api/trades/history` returns paginated history from DB

### Integrations Status

| Integration | Status |
|-------------|--------|
| H2 (in-memory) | Implemented |
| PostgreSQL | Pending (driver not in build.gradle) |
| Flyway | Pending |
| OpenTelemetry/Grafana Tempo | Pending |
| Micrometer/Prometheus | Pending |
| Docker/CI-CD | Pending |
