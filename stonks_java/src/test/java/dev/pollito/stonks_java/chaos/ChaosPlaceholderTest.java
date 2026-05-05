package dev.pollito.stonks_java.chaos;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ChaosPlaceholderTest {

  @Test
  void constructorThrowsRuntimeException() {
    assertThrows(RuntimeException.class, ChaosPlaceholder::new);
  }
}
