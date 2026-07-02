package dev.pollito.stonks_java.unit.real_adapter_out;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.stock.adapter.out.cobol.StockPriceEngineCobolAdapter;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolPriceEngineRequest;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolPriceEngineResult;
import dev.pollito.stonks_java.stock.domain.Trend;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockPriceEngineCobolAdapterMockTest {

  private static final String PROGRAM = "price-engine";
  @Mock private CobolAppPortOut cobolPortOut;
  @InjectMocks private StockPriceEngineCobolAdapter adapter;

  @Test
  void calculate() {
    BigDecimal currentPrice = valueOf(45.0);
    BigDecimal volatility = valueOf(0.3);
    Trend trend = Trend.BULL;
    BigDecimal newPrice = valueOf(47.5);

    when(cobolPortOut.execute(
            PROGRAM,
            new CobolPriceEngineRequest(currentPrice, volatility, trend.name()),
            CobolPriceEngineResult.class))
        .thenReturn(new CobolPriceEngineResult(newPrice));

    assertEquals(newPrice, adapter.calculate(currentPrice, volatility, trend));
    verify(cobolPortOut)
        .execute(
            PROGRAM,
            new CobolPriceEngineRequest(currentPrice, volatility, trend.name()),
            CobolPriceEngineResult.class);
  }
}
