package dev.pollito.stonks_java.stock;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.stock.adapter.out.cobol.CatalogCobolAdapterStub;
import dev.pollito.stonks_java.stock.adapter.out.cobol.PriceEngineCobolAdapterStub;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.application.port.out.PriceEnginePortOut;
import dev.pollito.stonks_java.stock.application.port.out.StockPortOut;
import dev.pollito.stonks_java.stock.application.service.StockService;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
class StockPriceTickSchedulerE2eTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public PriceEnginePortOut priceEnginePortOut() {
      return new PriceEngineCobolAdapterStub();
    }

    @Bean
    @Primary
    public StockPortOut catalogPort() {
      return new CatalogCobolAdapterStub();
    }
  }

  @Autowired private StockService stockService;
  @Autowired private StockPortIn stockPortIn;

  @Test
  void simulationTickUpdatesAllPrices() {
    List<StockPrice> before = stockPortIn.getStocks();
    assertThat(before).isNotEmpty();

    stockService.simulate();

    List<StockPrice> after = stockPortIn.getStocks();
    assertThat(after).hasSameSizeAs(before);
    assertThat(after).allMatch(s -> s.price().compareTo(ZERO) > 0);
  }
}
