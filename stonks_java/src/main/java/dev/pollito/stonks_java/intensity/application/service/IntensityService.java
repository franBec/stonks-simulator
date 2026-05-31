package dev.pollito.stonks_java.intensity.application.service;

import static java.math.BigDecimal.valueOf;

import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import dev.pollito.stonks_java.intensity.application.port.out.IntensityLevelPortOut;
import dev.pollito.stonks_java.intensity.domain.IntensityLevel;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IntensityService implements IntensityPortIn {

  private final StockPortIn stockPortIn;
  private final IntensityLevelPortOut intensityLevelPortOut;

  private final AtomicReference<IntensityLevel> currentLevel =
      new AtomicReference<>(IntensityLevel.PAPER_HANDS);

  @PostConstruct
  public void initialize() {
    intensityLevelPortOut
        .loadCurrentLevel()
        .ifPresent(
            level -> {
              currentLevel.set(level);
              stockPortIn.setVolatilityMultiplier(valueOf(level.getVolatilityMultiplier()));
            });
  }

  @Override
  public IntensityLevel getCurrentLevel() {
    return currentLevel.get();
  }

  @Override
  @Transactional
  public void setLevel(IntensityLevel level) {
    currentLevel.set(level);
    stockPortIn.setVolatilityMultiplier(valueOf(level.getVolatilityMultiplier()));
    intensityLevelPortOut.saveLevel(level);
  }
}
