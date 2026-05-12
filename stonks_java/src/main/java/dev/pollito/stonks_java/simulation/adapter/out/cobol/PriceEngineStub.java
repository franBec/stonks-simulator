package dev.pollito.stonks_java.simulation.adapter.out.cobol;

import static java.math.BigDecimal.ONE;

import dev.pollito.stonks_java.simulation.application.port.out.PriceEnginePort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@Primary
@Slf4j
public class PriceEngineStub implements PriceEnginePort {

  private static final BigDecimal MAX_PRICE = new BigDecimal("500.00");
  private static final BigDecimal MIN_PRICE = new BigDecimal("0.10");
  private static final BigDecimal MAX_STEP_CHANGE = new BigDecimal("0.15");
  private final SecureRandom random = new SecureRandom();

  @Override
  public BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, String trend) {
    log.warn("Using dev stub for PriceEnginePort — no real COBOL engine is running");

    BigDecimal trendBias =
        switch (trend) {
          case "BULL" -> new BigDecimal("0.003");
          case "BEAR" -> new BigDecimal("-0.003");
          case "MOON" -> new BigDecimal("0.01");
          case "CHAOS" -> BigDecimal.valueOf((random.nextDouble() - 0.5) * 0.04);
          case "CRASH" -> new BigDecimal("-0.05");
          default -> BigDecimal.ZERO;
        };

    BigDecimal randomShock =
        BigDecimal.valueOf((random.nextDouble() - 0.5) * 2.0).multiply(volatility);

    BigDecimal totalFactor = ONE.add(trendBias).add(randomShock);
    BigDecimal newPrice = currentPrice.multiply(totalFactor).setScale(2, RoundingMode.HALF_UP);

    BigDecimal stepChange =
        newPrice.subtract(currentPrice).divide(currentPrice, 10, RoundingMode.HALF_UP);
    if (stepChange.compareTo(MAX_STEP_CHANGE) > 0) {
      newPrice = currentPrice.multiply(ONE.add(MAX_STEP_CHANGE)).setScale(2, RoundingMode.HALF_UP);
    } else if (stepChange.compareTo(MAX_STEP_CHANGE.negate()) < 0) {
      newPrice =
          currentPrice.multiply(ONE.subtract(MAX_STEP_CHANGE)).setScale(2, RoundingMode.HALF_UP);
    }

    if (newPrice.compareTo(MAX_PRICE) > 0) {
      newPrice = MAX_PRICE;
    }
    if (newPrice.compareTo(MIN_PRICE) < 0) {
      newPrice = MIN_PRICE;
    }

    return newPrice;
  }
}
