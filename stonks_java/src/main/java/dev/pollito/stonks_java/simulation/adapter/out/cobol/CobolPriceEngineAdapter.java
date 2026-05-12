package dev.pollito.stonks_java.simulation.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.CobolProgramExecutor;
import dev.pollito.stonks_java.simulation.adapter.out.cobol.dto.CobolPriceEngineRequest;
import dev.pollito.stonks_java.simulation.adapter.out.cobol.dto.CobolPriceEngineResult;
import dev.pollito.stonks_java.simulation.application.port.out.PriceEnginePort;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CobolPriceEngineAdapter implements PriceEnginePort {
  private static final String PROGRAM_NAME = "price-engine";

  private final CobolProgramExecutor executor;

  @Override
  public BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, String trend) {
    return executor
        .execute(
            PROGRAM_NAME,
            new CobolPriceEngineRequest(currentPrice, volatility, trend),
            CobolPriceEngineResult.class)
        .newPrice();
  }
}
