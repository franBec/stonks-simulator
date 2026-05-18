package dev.pollito.stonks_java;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Smoke test (not E2E) — quick sanity check that the full Spring context boots without
// configuration errors. Provides fast feedback on basic wiring issues without running
// any real assertions. Complementary to the more focused per-module E2E tests.
@SpringBootTest
class StonksJavaApplicationTests {

  @Test
  void contextLoads() {}
}
