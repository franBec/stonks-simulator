package dev.pollito.stonks_java.stock.adapter.out.cobol;

import static java.util.Arrays.stream;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.stock.adapter.out.cobol.dto.CobolCatalogStock;
import dev.pollito.stonks_java.stock.adapter.out.cobol.mapper.StockCobolMapper;
import dev.pollito.stonks_java.stock.application.port.out.StockPortOut;
import dev.pollito.stonks_java.stock.domain.Stock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "cobol", havingValue = "real")
@RequiredArgsConstructor
public class StockCatalogCobolAdapter implements StockPortOut {
  private static final String PROGRAM_NAME = "catalog";

  private final CobolAppPortOut cobolPortOut;
  private final StockCobolMapper mapper;

  @Override
  public List<Stock> getStocks() {
    return stream(cobolPortOut.execute(PROGRAM_NAME, null, CobolCatalogStock[].class))
        .map(mapper::map)
        .toList();
  }
}
