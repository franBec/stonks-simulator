package dev.pollito.stonks_java.util.enums;

import org.jspecify.annotations.NonNull;

public final class EnumUtils {

  private EnumUtils() {}

  public static <E extends Enum<E> & ValuedEnum<V>, V> @NonNull E fromValue(
      @NonNull Class<E> enumClass, V value) {
    for (E constant : enumClass.getEnumConstants()) {
      if (constant.getValue().equals(value)) {
        return constant;
      }
    }
    throw new IllegalArgumentException("Unknown " + enumClass.getSimpleName() + " value: " + value);
  }
}
