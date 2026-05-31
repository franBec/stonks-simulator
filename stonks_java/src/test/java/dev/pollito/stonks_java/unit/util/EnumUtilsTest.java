package dev.pollito.stonks_java.unit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.pollito.stonks_java.util.enums.EnumUtils;
import dev.pollito.stonks_java.util.enums.ValuedEnum;
import org.junit.jupiter.api.Test;

class EnumUtilsTest {

  private enum TestEnum implements ValuedEnum<String> {
    FIRST("one"),
    SECOND("two");

    private final String value;

    TestEnum(String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value;
    }
  }

  @Test
  void fromValueReturnsFirstMatch() {
    TestEnum result = EnumUtils.fromValue(TestEnum.class, "one");
    assertEquals(TestEnum.FIRST, result);
  }

  @Test
  void fromValueReturnsSubsequentMatch() {
    TestEnum result = EnumUtils.fromValue(TestEnum.class, "two");
    assertEquals(TestEnum.SECOND, result);
  }

  @Test
  void fromValueThrowsWhenNotFound() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> EnumUtils.fromValue(TestEnum.class, "unknown"));
    assertEquals("Unknown TestEnum value: unknown", exception.getMessage());
  }
}
