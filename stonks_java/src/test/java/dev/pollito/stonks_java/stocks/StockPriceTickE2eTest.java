package dev.pollito.stonks_java.stocks;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.simulation.adapter.out.cobol.PriceEngineStub;
import dev.pollito.stonks_java.simulation.application.port.out.PriceEnginePort;
import dev.pollito.stonks_java.stocks.adapter.out.cobol.CatalogPortStub;
import dev.pollito.stonks_java.stocks.application.port.in.GetStocksUseCase;
import dev.pollito.stonks_java.stocks.application.port.out.CatalogPort;
import dev.pollito.stonks_java.stocks.application.service.StockPriceTickService;
import dev.pollito.stonks_java.stocks.domain.StockPrice;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
class StockPriceTickE2eTest {

  @TestConfiguration
  static class StockPriceTickTestConfig {
    @Bean
    @Primary
    public PriceEnginePort priceEnginePort() {
      return new PriceEngineStub();
    }

    @Bean
    @Primary
    public CatalogPort catalogPort() {
      return new CatalogPortStub();
    }
  }

  @Autowired private StockPriceTickService stockPriceTickService;
  @Autowired private GetStocksUseCase getStocksUseCase;

  @Test
  void simulationTickUpdatesAllPrices() {
    List<StockPrice> before = getStocksUseCase.getStocks();
    assertThat(before).isNotEmpty();

    stockPriceTickService.simulate();

    List<StockPrice> after = getStocksUseCase.getStocks();
    assertThat(after).hasSameSizeAs(before);
    assertThat(after).allMatch(s -> s.price().compareTo(ZERO) > 0);
  }
}
