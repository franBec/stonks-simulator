package dev.pollito.stonks_java.stock;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.application.service.StockService;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
class StockPriceTickSchedulerE2eTest {

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
