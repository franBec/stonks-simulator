package dev.pollito.stonks_java.trading.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;

public enum ValidationStatus implements ValuedEnum<String> {
  ACCEPTED,
  REJECTED;

  @Override
  public String getValue() {
    return name();
  }
}
