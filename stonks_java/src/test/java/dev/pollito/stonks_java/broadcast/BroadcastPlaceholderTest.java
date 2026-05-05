package dev.pollito.stonks_java.broadcast;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BroadcastPlaceholderTest {

  @Test
  void constructorThrowsRuntimeException() {
    assertThrows(RuntimeException.class, BroadcastPlaceholder::new);
  }
}
