---
description: Analyze JaCoCo coverage report and suggest improvements following the project's testing approach
---

Analyze the JaCoCo test coverage report and suggest actionable ways to improve line and branch coverage, following the testing approach defined in @stonks_java/README.md.

## JaCoCo Report

Check the JaCoCo HTML report at:
`file:///home/pollito/Documents/code/franBec/stonks-simulator/stonks_java/build/reports/jacoco/test/html/index.html`

If that path doesn't exist, find the report wherever it is located under `stonks_java/build/reports/jacoco/`.

## Testing Approach Reference

Follow the project's testing strategy from @stonks_java/README.md:

### Strategy
| Layer | Tool | Purpose |
|-------|------|---------|
| E2E | `@ApplicationModuleTest` + `RestTestClient` | Full HTTP flow through module boundaries |
| Unit | `@ExtendWith(MockitoExtension.class)` | Stubbed adapter logic, edge cases not reachable via E2E |
| Integration | Plain JUnit | COBOL executor, process spawning |
| Architecture | `ApplicationModules.verify()` | Modulith boundary enforcement |

### Key Rules
- **E2E tests are the default** — if a scenario can be tested end-to-end, it should be
- **Unit tests fill gaps** — for classes never initialized when stubs are active (e.g., real COBOL adapters) and complex edge-case logic
- **MapStruct mappers in tests** use `@Spy` with the generated `Impl` class, not `@Mock`
- **Test data** via `@Sql` fixtures in `src/test/resources/sql/`, not repository autowiring
- **Avoid**: `@DirtiesContext`, `@TestPropertySource`, `@ActiveProfiles`, repository autowiring for setup

### Coverage Philosophy
> Coverage thresholds are a suggestion, not a hard rule. Adjust them up or down as the codebase evolves. The goal is to catch regressions, not to chase a number.

## Analysis

1. Identify packages/classes with the lowest line and branch coverage
2. For each low-coverage area, determine which test layer (E2E, Unit, Integration) is most appropriate
3. Suggest specific test scenarios that would improve coverage, aligned with the testing approach
4. Highlight any coverage gaps that are acceptable (e.g., real COBOL adapters that require the `cobol` or `production` profile)
5. Prioritize suggestions by impact — focus on business logic in the application core and adapter logic that is reachable with the default test profile (H2 + stubs)

## Output Format

For each suggestion, provide:

1. **Target** — class or method to cover
2. **Current coverage** — line and branch percentages (if available)
3. **Recommended test type** — E2E, Unit, or Integration (with justification)
4. **Test scenario** — specific case to test
5. **Test data fixture** — which SQL fixture to use or if inline cleanup is needed
