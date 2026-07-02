package dev.pollito.stonks_java.stock.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolPriceEngineRequest;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolPriceEngineResult;
import dev.pollito.stonks_java.stock.application.port.out.StockPriceEnginePortOut;
import dev.pollito.stonks_java.stock.domain.Trend;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "cobol", havingValue = "real")
@RequiredArgsConstructor
public class StockPriceEngineCobolAdapter implements StockPriceEnginePortOut {
  private static final String PROGRAM_NAME = "price-engine";

  private final CobolAppPortOut cobolPortOut;

  @Override
  public BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, Trend trend) {
    return cobolPortOut
        .execute(
            PROGRAM_NAME,
            new CobolPriceEngineRequest(currentPrice, volatility, trend.name()),
            CobolPriceEngineResult.class)
        .newPrice();
  }
}
