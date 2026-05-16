package dev.pollito.stonks_java.stock.adapter.out.cobol;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolCatalogStock;
import dev.pollito.stonks_java.stock.adapter.out.cobol.mapper.StockCobolMapper;
import dev.pollito.stonks_java.stock.adapter.out.cobol.mapper.StockCobolMapperImpl;
import dev.pollito.stonks_java.stock.domain.Stock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockCatalogCobolAdapterTest {

  @Mock private CobolAppPortOut cobolPortOut;
  @Spy private StockCobolMapper mapper = new StockCobolMapperImpl();
  @InjectMocks private StockCatalogCobolAdapter adapter;

  @Test
  void getStocks() {
    CobolCatalogStock cobolStock =
        new CobolCatalogStock(
            "GMEE", "GameStonk", valueOf(45.0), valueOf(0.3), "neutral");

    when(cobolPortOut.execute("catalog", null, CobolCatalogStock[].class))
        .thenReturn(new CobolCatalogStock[] {cobolStock});

    List<Stock> result = adapter.getStocks();

    assertEquals(
        List.of(new Stock(
            "GMEE", "GameStonk", valueOf(45.0), valueOf(0.3), "neutral")),
        result);
    verify(cobolPortOut).execute("catalog", null, CobolCatalogStock[].class);
    verify(mapper).map(cobolStock);
  }
}
