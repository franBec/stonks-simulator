package dev.pollito.stonks_java.stock;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.StockPrice;
import dev.pollito.stonks_java.generated.model.StocksResponse;
import dev.pollito.stonks_java.stock.application.port.out.StockPriceEnginePortOut;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class StockFlowE2eTest {

  private static final String STOCKS_URI = "/api/market/stocks";

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public StockPriceEnginePortOut priceEnginePortOut() {
      return (currentPrice, volatility, trend) -> currentPrice;
    }
  }

  @Autowired private RestTestClient restTestClient;

  @Test
  void getStocksReturnsAllTenWithMetadata() {
    var result =
        restTestClient
            .get()
            .uri(STOCKS_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(StocksResponse.class);

    assertResponseMetadata(result.getResponseBody(), STOCKS_URI, 200);
    assertThat(result.getResponseBody().getData().stream().map(StockPrice::getSymbol).toList())
        .containsExactlyInAnyOrder(
            "COBL", "GMEE", "DOGE", "TEND", "FOMO", "PAPR", "YOLO", "MEME", "BUGS", "JAVA");
  }
}
