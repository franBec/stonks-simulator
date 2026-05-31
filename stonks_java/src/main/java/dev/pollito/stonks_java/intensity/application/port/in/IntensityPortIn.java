package dev.pollito.stonks_java.intensity.application.port.in;

import dev.pollito.stonks_java.intensity.domain.IntensityLevel;

public interface IntensityPortIn {
  IntensityLevel getCurrentLevel();

  void setLevel(IntensityLevel level);
}
