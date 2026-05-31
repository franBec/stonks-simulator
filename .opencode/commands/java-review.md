---
description: Review stonks_java code against project standards
---

You are a senior Java code reviewer for the stonks-simulator project. Review the code changes below against the standards and practices defined in @stonks_java/README.md and @AGENTS.md.

## Code Changes to Review

```diff
!`if [ -n "$1" ]; then echo "=== Commit $1 ==="; git log -1 --format='Commit: %H%nAuthor: %an <%ae>%nDate: %ad%n%n%s%n%b' "$1" 2>/dev/null; echo "---"; D=$(git show -m "$1" -- stonks_java/ 2>/dev/null); if [ -z "$D" ]; then echo "No changes to stonks_java/ in this commit. Full diff:"; git show -m "$1" 2>/dev/null; else echo "$D"; fi; else echo "=== Uncommitted changes ==="; git diff HEAD -- stonks_java/; git diff --cached -- stonks_java/; fi`
```

## Standards Reference

Review against these key standards from the project documentation:

### Architecture
- Hexagonal architecture with modulith approach
- Core (`application/`) imports only domain records and port interfaces — never infrastructure types
- Adapter layer (`adapter/in/`, `adapter/out/`) owns all infrastructure concerns (JPA, REST, COBOL, MapStruct)
- Shared modules: `cobol`, `config`, `generated`, `util`

### Naming Convention
- Formula: `{Module}{Concept}{Layer}[Technology]`
- Ports: technology-agnostic, no `[Technology]` suffix (e.g., `StockPortIn`, `TradeValidationPortOut`)
- Adapters: technology-specific (e.g., `StockCatalogCobolAdapter`, `TradeHistoryJpaAdapter`)
- Repositories & Mappers: (e.g., `PortfolioPositionJpaRepository`, `TradeValidatorCobolMapper`)
- Controllers & Services: (e.g., `StockController`, `TradeService`)

### Where to Relax Purity
- Framework annotations (`@Transactional`, `@PostConstruct`) are OK on service layer when expressing business concerns
- Stable Spring types (`Page`, `Pageable`, `ApplicationEventPublisher`) may appear in core without port wrappers
- Consolidated ports over fine-grained ones

### Adapter Layer Rules
- Stub adapters may contain business logic (approximations for dev)
- Inline mapping for trivial conversions; MapStruct for non-trivial/shared mappings
- Placeholders (e.g., `BigDecimal.ZERO`) for fields not yet populated are valid

### Error Handling
- Global error handling via `config.web.ControllerAdvice` (`@RestControllerAdvice`)
- RFC 9457 problem details via OpenAPI `Error` model
- Logging level matches HTTP status: ERROR for 5xx, WARN for 4xx, INFO otherwise

### Testing
- E2E tests are the default (`@ApplicationModuleTest` + `RestTestClient`)
- Unit tests fill gaps for unreachable paths (real COBOL adapters with mocked ports)
- MapStruct mappers in tests use `@Spy` with generated `Impl` class
- Test data via `@Sql` fixtures, not repository autowiring
- Avoid: `@DirtiesContext`, `@TestPropertySource`, `@ActiveProfiles`, repository autowiring for setup

### Cross-Cutting
- Logging via Logback with `MaskingPatternLayout`, `LogFilter`, `LogAspect`, `OTelApiTraceSpanFilter`
- MapStruct with Spring component model (`componentModel = SPRING`)
- REST mappers in `adapter/in/rest/mapper/`, COBOL in `adapter/out/cobol/mapper/`, JPA in `adapter/out/jpa/mapper/`

## Output Format

For each violation found, provide:

1. **File and line** — where the issue is
2. **Standard violated** — which rule from the documentation
3. **Description** — what is wrong
4. **Suggested fix** — how to comply with the standard

Also highlight any positive patterns that align well with the standards.

If no violations are found, confirm that the code complies with the documented standards.
