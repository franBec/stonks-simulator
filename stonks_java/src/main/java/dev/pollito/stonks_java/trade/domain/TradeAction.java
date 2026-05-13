package dev.pollito.stonks_java.trade.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;

public enum TradeAction implements ValuedEnum<String> {
  BUY,
  SELL;

  @Override
  public String getValue() {
    return name();
  }
}
