package dev.pollito.stonks_java.intensity.application.port.out;

import dev.pollito.stonks_java.intensity.domain.IntensityLevel;
import java.util.Optional;

public interface IntensityLevelPortOut {
  Optional<IntensityLevel> loadCurrentLevel();

  void saveLevel(IntensityLevel level);
}
