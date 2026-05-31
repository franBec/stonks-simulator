package dev.pollito.stonks_java.intensity.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;

public enum IntensityLevel implements ValuedEnum<String> {
  PAPER_HANDS(1.0, 900000L),
  MODERATE(2.0, 300000L),
  HIGH_VOLATILITY(5.0, 120000L),
  EXTREME(12.5, 60000L),
  MAXIMUM_OVERDRIVE(25.0, 30000L);

  private final double volatilityMultiplier;
  private final long aiEventIntervalMs;

  IntensityLevel(double volatilityMultiplier, long aiEventIntervalMs) {
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
