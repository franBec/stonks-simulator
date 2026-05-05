package dev.pollito.stonks_java.market;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MarketPlaceholderTest {

  @Test
  void constructorThrowsRuntimeException() {
    assertThrows(RuntimeException.class, MarketPlaceholder::new);
  }
}
