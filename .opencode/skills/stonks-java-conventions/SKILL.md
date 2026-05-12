---
name: stonks-java-conventions
description: >
  Guides agents to write idiomatic Java code for the stonks_java project: Spring Modulith +
  hexagonal architecture, OpenAPI-first code gen, MapStruct mappers, ValuedEnum pattern,
  response metadata envelope, and the Netflix-Lite testing strategy
  (e2e slices with RestTestClient, no Mockito, no TestContainers).
license: MIT
compatibility: opencode
metadata:
  audience: developers
  project: stonks_java
---

# stonks-java-conventions

## Project Context

- **Tech stack**: Java 25, Spring Boot 4.0.6, Spring Modulith 2.0.6, Gradle, SQLite, GnuCOBOL
- **Frontend**: React 19 + Tailwind CSS v4 (separate `stonks_vite_app/` module)
- **Architecture**: Hexagonal (Ports & Adapters) inside Spring Modulith modules
- **API contract**: OpenAPI 3.0.3 spec in `src/main/resources/openapi.yaml`
- **Code generation**: OpenAPI Generator 7.20.0 generates interfaces and DTOs into `build/generated/sources/openapi`

## Module Structure

Each business capability is a top-level package under `dev.pollito.stonks_java`:

```
trading/
├── package-info.java              // @ApplicationModule(allowedDependencies = {"market"})
├── adapter/
│   ├── in/rest/
│   │   ├── TradesController.java  // implements generated TradesApi
│   │   └── mapper/
│   │       └── TradeRestMapper.java
│   └── out/cobol/
│       ├── CobolTradeValidatorAdapter.java  // implements port
│       ├── TradeValidatorStub.java
│       ├── dto/
│       │   └── CobolTradeValidationRequest.java
│       │   └── CobolTradeValidationResult.java
│       └── mapper/
│           └── TradeCobolMapper.java
├── application/
│   ├── port/
│   │   ├── in/ValidateTradeUseCase.java
│   │   └── out/TradeValidatorPort.java
│   └── service/
│       └── TradeServiceImpl.java
└── domain/
    ├── Trade.java
    ├── TradeAction.java
    ├── TradeValidation.java
    └── ValidationStatus.java
```

Shared infrastructure lives in `config/`, `util/`, `cobol/`, and `generated/` modules.

## Coding Standards

### Formatting
- **Google Java Format** enforced by Spotless (`./gradlew spotlessApply`)
- **2-space indentation** (configured in `greclipse.properties`)
- Build fails if not formatted: `build` depends on `spotlessApply`

### Imports & Style
- Prefer `var` for local variables
- Use static imports for constants and enums
- Use `org.jspecify.annotations.NonNull` for nullability annotations
- Use Lombok `@RequiredArgsConstructor` for dependency injection (no `@Autowired` on fields)

### Naming
- **Controllers**: `XxxController` implementing generated `XxxApi`
- **Services**: `XxxServiceImpl` implementing `XxxUseCase`
- **Adapters**: `XxxAdapter` implementing `XxxPort`
- **Mappers**: `XxxMapper` as MapStruct interface
- **Domain enums**: implement `ValuedEnum<T>`

## Hexagonal Architecture Rules

1. **Inbound adapters** (REST controllers) depend only on:
   - Generated API interfaces
   - Application use-case interfaces (`port.in`)
   - MapStruct mappers

2. **Application services** depend only on:
   - Domain models
   - Inbound port interfaces (they implement these)
   - Outbound port interfaces (they use these)

3. **Outbound adapters** depend only on:
   - Outbound port interfaces (they implement these)
   - External systems (COBOL, DB, etc.)
   - MapStruct mappers for DTO translation

4. **Domain** has zero dependencies on frameworks (plain records/enums).

## OpenAPI-First Workflow

1. Edit `src/main/resources/openapi.yaml` to define or change the API contract
2. Run `./gradlew openApiGenerate` (happens automatically before `compileJava`)
3. Generated code lands in `build/generated/sources/openapi` under `dev.pollito.stonks_java.generated.api|model`
4. Controller implements the generated API interface
5. Use MapStruct to map between generated models and domain models

## Response Envelope Pattern

Every API response wraps data in a metadata envelope. In OpenAPI:

```yaml
schemas:
  ResponseMetadata:
    properties:
      instance: string   # request URI
      status: integer    # HTTP status
      timestamp: string  # ISO-8601 date-time
      trace: string      # OpenTelemetry trace ID
```

Success responses (`XxxResponse`) extend `ResponseMetadata` and add a `data` property.
Error responses (`Error`) extend `ResponseMetadata` and add `title` and `detail`.

Controllers must populate metadata using:
- `request.getRequestURI()` for `instance`
- `java.time.OffsetDateTime.now()` for `timestamp`
- `io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId()` for `trace`

## ValuedEnum Pattern

Domain enums implement `dev.pollito.stonks_java.util.enums.ValuedEnum<T>`:

```java
public enum TradeAction implements ValuedEnum<String> {
  BUY, SELL;

  @Override
  public String getValue() { return name(); }
}
```

Map generated OpenAPI enums to domain enums using `EnumUtils.fromValue()`:

```java
default TradeAction map(dev.pollito.stonks_java.generated.model.TradeAction action) {
  return action == null ? null : EnumUtils.fromValue(TradeAction.class, action.getValue());
}
```

## MapStruct Conventions

- Component model: `MappingConstants.ComponentModel.SPRING`
- Default methods for enum mapping when names differ or when using `ValuedEnum`
- Mappers are in `adapter/in/rest/mapper/` or `adapter/out/cobol/mapper/`

## Testing Strategy (Netflix-Lite)

### Module Slice Tests (Preferred)
Use `@ApplicationModuleTest` with `RestTestClient`:

```java
@ApplicationModuleTest(
    mode = DIRECT_DEPENDENCIES,
    webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class TradingFlowE2eTest {

  @TestConfiguration
  static class TradingTestConfig {
    @Bean
    @Primary
    public TradeValidatorPort tradeValidatorPort() {
      return new TradeValidatorStub();
    }
  }

  @Autowired private RestTestClient restTestClient;

  @Test
  void someScenario() {
    var result = restTestClient
        .post()
        .uri("/api/trades/validate")
        .body(request)
        .exchange()
        .expectStatus().isOk()
        .returnResult(TradeValidationResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades/validate", 200);
    assertThat(result.getResponseBody().getData().getStatus()).isEqualTo(ACCEPTED);
  }
}
```

Key rules:
- Use `@TestConfiguration` + `@Bean @Primary` to inject stubs for outbound adapters
- Use `RestTestClient` for HTTP assertions
- Use `assertResponseMetadata()` from `dev.pollito.stonks_java.test.util.RestTestClientAssertions`
- Use AssertJ (`assertThat`) for all assertions
- Use JUnit 5 parameterized tests for scenario tables
- No TestContainers. No `@MockBean`.
- Mockito is **not** used in e2e/modulith tests.

### Unit Tests for Uncovered Adapters

Outbound adapters replaced by stubs in e2e tests have zero coverage. Write focused unit tests
for them with Mockito: mock the external dependency, spy on the real MapStruct mapper.

```java
@ExtendWith(MockitoExtension.class)
class CobolTradeValidatorAdapterTest {

  @Mock private CobolProgramExecutor cobolProgramExecutor;

  @Spy
  private TradeCobolMapper tradeCobolMapper = new TradeCobolMapperImpl();

  @InjectMocks private CobolTradeValidatorAdapter adapter;

  @Test
  void validate() {
    Trade trade = new Trade(TradeAction.BUY, "GMEE", 10, 45.0, 10000.0);
    CobolTradeValidationResult result =
        new CobolTradeValidationResult("ACCEPTED", null, "Valid", 450.0, 9550.0);

    when(cobolProgramExecutor.execute(
            "trade-validator",
            new CobolTradeValidationRequest("BUY", "GMEE", 10, 45.0, 10000.0),
            CobolTradeValidationResult.class))
        .thenReturn(result);

    TradeValidation actual = adapter.validate(trade);

    assertEquals(
        new TradeValidation(ValidationStatus.ACCEPTED, null, "Valid", 450.0, 9550.0), actual);
    verify(tradeCobolMapper).map(trade);
    verify(tradeCobolMapper).map(result);
    verify(cobolProgramExecutor)
        .execute(
            "trade-validator",
            new CobolTradeValidationRequest("BUY", "GMEE", 10, 45.0, 10000.0),
            CobolTradeValidationResult.class);
  }
}
```

Key rules:
- Mock only the external dependency (`CobolProgramExecutor`), not the mapper
- Spy on the real MapStruct `Impl` class so actual mapping logic is exercised
- Verify mapper interactions and the mock call
- These tests are supplementary to e2e tests, not a replacement

### Modulith Verification
Every module must pass:

```java
class ModulithVerificationTest {
  @Test
  void verifyModuleStructure() {
    ApplicationModules.of(StonksJavaApplication.class).verify();
  }
}
```

Declare allowed cross-module dependencies in `package-info.java`:

```java
@ApplicationModule(allowedDependencies = {"market"})
package dev.pollito.stonks_java.trading;
```

## Coverage Requirements

JaCoCo enforces:
- **Line coverage**: minimum 60%
- **Branch coverage**: minimum 50%

Excluded from coverage:
- `generated/`, `openapitools/`
- `*Application*`
- `domain/` (records/POJOs)
- `dto/`
- `*Exception*`
- `log/`

## Dependency Injection

- Constructor injection only via Lombok `@RequiredArgsConstructor`
- No field `@Autowired`
- Configuration properties use `@Data` + `@ConfigurationProperties` with `@ConfigurationPropertiesScan` on main class

## Observability

- OpenTelemetry is auto-configured via `spring-boot-starter-opentelemetry`
- Include trace ID in all responses via `Span.current().getSpanContext().getTraceId()`
- `LogAspect` logs all controller entry/exit automatically
- Use `OTelApiTraceSpanFilter` for trace/span filtering

## COBOL Integration

- Outbound adapters call `CobolProgramExecutor.execute(programName, request, responseType)`
- COBOL programs are configured in `application.yaml` under `stonks.cobol.programs`
- Programs communicate via stdin/stdout JSON
- Stub implementations (`TradeValidatorStub`) are provided for testing

## Adding a New Feature

1. Define the OpenAPI contract in `openapi.yaml`
2. Run `./gradlew openApiGenerate` (or just build)
3. Create the module package with `package-info.java` and `@ApplicationModule`
4. Create domain records/enums
5. Create inbound port (use case) interface
6. Create outbound port interface
7. Implement service in `application/service/`
8. Implement controller in `adapter/in/rest/` implementing generated API
9. Implement outbound adapter in `adapter/out/`
10. Write MapStruct mappers
11. Write `@ApplicationModuleTest` slice tests with stubbed outbound adapters
12. Run `./gradlew check` (includes tests, coverage, Spotless, and modulith verification)

## Prohibited Patterns

- Do NOT use `@MockBean`
- Do NOT use TestContainers
- Mockito is allowed only in unit tests for uncovered adapters (see section above), never in e2e/modulith tests
- Do NOT use field injection (`@Autowired` on fields)
- Do NOT put business logic in controllers
- Do NOT reference generated models from domain or application layers
- Do NOT create circular module dependencies
