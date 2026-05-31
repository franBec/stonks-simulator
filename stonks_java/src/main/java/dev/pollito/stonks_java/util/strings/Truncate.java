package dev.pollito.stonks_java.util.strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Truncate {

  public static String value(String value, int max) {
    return value == null || value.length() <= max ? value : value.substring(0, max);
  }
}
