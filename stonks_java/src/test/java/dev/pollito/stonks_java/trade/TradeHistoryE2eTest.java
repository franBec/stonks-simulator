package dev.pollito.stonks_java.trade;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.TradeHistoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class TradeHistoryE2eTest {

  private static final String HISTORY_URI = "/api/trades/history";

  @Autowired private RestTestClient restTestClient;

  @Test
  @Sql("/sql/portfolio-with-history.sql")
  void paginatedHistory() {
    var result =
        restTestClient
            .get()
            .uri(HISTORY_URI + "?page=0&size=1")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeHistoryResponse.class);

    assertResponseMetadata(result.getResponseBody(), HISTORY_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getTotalElements()).isEqualTo(2);
    assertThat(data.getTotalPages()).isEqualTo(2);
    assertThat(data.getPageable().getPageNumber()).isZero();
    assertThat(data.getPageable().getPageSize()).isEqualTo(1);
    assertThat(data.getContent()).hasSize(1);

    var item = data.getContent().getFirst();
    assertThat(item.getAction()).isEqualTo("SELL");
    assertThat(item.getSymbol()).isEqualTo("GMEE");
    assertThat(item.getQuantity()).isEqualTo(5);
  }

  @Test
  @Sql(statements = {"DELETE FROM trade_history", "DELETE FROM position", "DELETE FROM portfolio"})
  void emptyHistory() {
    var result =
        restTestClient
            .get()
            .uri(HISTORY_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeHistoryResponse.class);

    assertResponseMetadata(result.getResponseBody(), HISTORY_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getContent()).isEmpty();
    assertThat(data.getTotalElements()).isZero();
    assertThat(data.getTotalPages()).isZero();
  }
}
