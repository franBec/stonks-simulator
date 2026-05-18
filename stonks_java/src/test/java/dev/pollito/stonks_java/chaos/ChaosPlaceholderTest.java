package dev.pollito.stonks_java.chaos;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

// Unit test (not E2E) because it verifies a compile-time/design constraint — the placeholder
// class is intentionally uninstantiable. A single assertion suffices; an E2E test would boot
// the entire Spring context just to confirm a constructor throws, adding zero value.
class ChaosPlaceholderTest {

  @Test
  void constructorThrowsRuntimeException() {
    assertThrows(RuntimeException.class, ChaosPlaceholder::new);
  }
}
