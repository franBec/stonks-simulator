package dev.pollito.stonks_java.stock.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolPriceEngineRequest;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolPriceEngineResult;
import dev.pollito.stonks_java.stock.application.port.out.PriceEnginePortOut;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"cobol", "production"})
@RequiredArgsConstructor
public class PriceEngineCobolAdapter implements PriceEnginePortOut {
  private static final String PROGRAM_NAME = "price-engine";

  private final CobolAppPortOut cobolPortOut;

  @Override
  public BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, String trend) {
    return cobolPortOut
        .execute(
            PROGRAM_NAME,
            new CobolPriceEngineRequest(currentPrice, volatility, trend),
            CobolPriceEngineResult.class)
        .newPrice();
  }
}
