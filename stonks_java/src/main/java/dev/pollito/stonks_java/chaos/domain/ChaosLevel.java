package dev.pollito.stonks_java.chaos.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;

public enum ChaosLevel implements ValuedEnum<String> {
  PAPER_HANDS(30000L, 1.0, 600000L),
  MODERATE(15000L, 2.0, 120000L),
  HIGH_VOLATILITY(5000L, 5.0, 30000L),
  EXTREME(2000L, 12.5, 15000L),
  MAXIMUM_OVERDRIVE(1000L, 25.0, 10000L);

  private final long tickIntervalMs;
  private final double volatilityMultiplier;
  private final long aiEventIntervalMs;

  ChaosLevel(long tickIntervalMs, double volatilityMultiplier, long aiEventIntervalMs) {
    this.tickIntervalMs = tickIntervalMs;
    this.volatilityMultiplier = volatilityMultiplier;
    this.aiEventIntervalMs = aiEventIntervalMs;
  }

  public long getTickIntervalMs() {
    return tickIntervalMs;
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
