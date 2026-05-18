---
description: Analyze JaCoCo coverage report, identify actionable improvements, and flag acceptable gaps
---

Analyze the JaCoCo test coverage report, identify actionable ways to improve line and branch coverage, and flag any gaps that fall under the project's acceptable gap categories — without proposing suggestions for those.

## JaCoCo Report

Check the JaCoCo report at `stonks_java/build/reports/jacoco/`

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
- **Every non-E2E test documents why** via a class-level `//` comment explaining why E2E was not chosen
- **MapStruct mappers in tests** use `@Spy` with the generated `Impl` class, not `@Mock`
- **Test data** via `@Sql` fixtures in `src/test/resources/sql/`, not repository autowiring
- **Avoid**: `@DirtiesContext`, `@TestPropertySource`, `@ActiveProfiles`, repository autowiring for setup

### Coverage Philosophy
> Coverage thresholds are a suggestion, not a hard rule. Adjust them up or down as the codebase evolves. The goal is to catch regressions, not to chase a number.

### Acceptable Gaps
Some code paths are intentionally left untested. These are documented in @stonks_java/README.md and fall into these categories:

1. **Preconditions validated at a higher layer** — e.g., a null action or negative quantity in a stub adapter that the REST layer rejects via `@Valid`/`@Min` before reaching the adapter.
2. **Safety guards** — defensive checks against programming errors never expected in normal operation.
3. **MapStruct-generated null guards** — null checks on every mapper parameter; would only fire if null propagates past compile-time type safety.
4. **Environment-dependent error paths** — e.g., process timeout cleanup in `CobolProgramExecutor` that cannot be reliably exercised on constrained runtimes like BusyBox/NixOS.

## Analysis

1. Identify packages/classes with the lowest line and branch coverage
2. For each low-coverage area, determine which test layer (E2E, Unit, Integration) is most appropriate
3. Suggest specific test scenarios that would improve coverage, aligned with the testing approach
4. Check whether the uncovered code matches an **Acceptable Gap** category — if so, **note it as acceptable and do not propose any suggestion**
5. Prioritize suggestions by impact — focus on business logic in the application core and adapter logic that is reachable with the default test profile (H2 + stubs)

## Output Format

For each **actionable** suggestion, provide:

1. **Target** — class or method to cover
2. **Current coverage** — line and branch percentages (if available)
3. **Recommended test type** — E2E, Unit, or Integration (with justification)
4. **Test scenario** — specific case to test
5. **Test data fixture** — which SQL fixture to use or if inline cleanup is needed

For **acceptable gaps**, simply list:

1. **Target** — class or method
2. **Gap category** — which Acceptable Gap category it falls under (1–4)
3. **Reason** — brief explanation of why it's acceptable
