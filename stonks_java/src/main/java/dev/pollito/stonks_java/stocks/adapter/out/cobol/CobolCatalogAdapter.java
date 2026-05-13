package dev.pollito.stonks_java.stocks.adapter.out.cobol;

import static java.util.Arrays.stream;

import dev.pollito.stonks_java.cobol.application.port.out.CobolPortOut;
import dev.pollito.stonks_java.stocks.adapter.out.cobol.dto.CobolCatalogStock;
import dev.pollito.stonks_java.stocks.adapter.out.cobol.mapper.StockCobolMapper;
import dev.pollito.stonks_java.stocks.application.port.out.CatalogPort;
import dev.pollito.stonks_java.stocks.domain.Stock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CobolCatalogAdapter implements CatalogPort {
  private static final String PROGRAM_NAME = "catalog";

  private final CobolPortOut cobolPortOut;
  private final StockCobolMapper mapper;

  @Override
  public List<Stock> getStocks() {
    return stream(cobolPortOut.execute(PROGRAM_NAME, null, CobolCatalogStock[].class))
        .map(mapper::map)
        .toList();
  }
}
