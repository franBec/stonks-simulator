package dev.pollito.stonks_java.portfolio;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PortfolioPlaceholderTest {

  @Test
  void constructorThrowsRuntimeException() {
    assertThrows(RuntimeException.class, PortfolioPlaceholder::new);
  }
}
