package dev.pollito.stonks_java.chaos.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;

public enum ChaosLevel implements ValuedEnum<String> {
  PAPER_HANDS(1.0, 600000L),
  MODERATE(2.0, 120000L),
  HIGH_VOLATILITY(5.0, 30000L),
  EXTREME(12.5, 15000L),
  MAXIMUM_OVERDRIVE(25.0, 10000L);

  private final double volatilityMultiplier;
  private final long aiEventIntervalMs;

  ChaosLevel(double volatilityMultiplier, long aiEventIntervalMs) {
    this.volatilityMultiplier = volatilityMultiplier;
    this.aiEventIntervalMs = aiEventIntervalMs;
  }

  public double getVolatilityMultiplier() {
    return volatilityMultiplier;
  }

  public long getAiEventIntervalMs() {
    return aiEventIntervalMs;
  }

  @Override
  public String getValue() {
    return name();
  }
}
