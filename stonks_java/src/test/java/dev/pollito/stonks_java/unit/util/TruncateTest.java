package dev.pollito.stonks_java.unit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.pollito.stonks_java.util.strings.Truncate;
import org.junit.jupiter.api.Test;

class TruncateTest {

  @Test
  void valueShouldReturnNullWhenValueIsNull() {
    assertEquals(null, Truncate.value(null, 5));
  }

  @Test
  void valueShouldReturnSameValueWhenLengthIsLessThanMax() {
    assertEquals("abc", Truncate.value("abc", 5));
  }

  @Test
  void valueShouldReturnSameValueWhenLengthEqualsMax() {
    assertEquals("abcde", Truncate.value("abcde", 5));
  }

  @Test
  void valueShouldReturnTruncatedWhenLengthExceedsMax() {
    assertEquals("abcde", Truncate.value("abcdefghij", 5));
  }

  @Test
  void valueShouldReturnEmptyWhenMaxIsZero() {
    assertEquals("", Truncate.value("anything", 0));
  }
}
