package dev.pollito.stonks_java.broadcast;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import dev.pollito.stonks_java.generated.model.PaperTapeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class PaperTapeE2eTest {

  private static final String PAPER_TAPE_URI = "/api/trades/paper-tape";

  @Autowired private RestTestClient restTestClient;

  @Test
  @Sql("/sql/portfolio-with-history.sql")
  void getPaperTapeReturnsFormattedEntries() {
    var result =
        restTestClient
            .get()
            .uri(PAPER_TAPE_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(PaperTapeResponse.class);

    assertResponseMetadata(result.getResponseBody(), PAPER_TAPE_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getTotalElements()).isEqualTo(2);
    assertThat(data.getContent()).hasSize(2);

    var firstEntry = data.getContent().getFirst();
    assertThat(firstEntry.getSequenceNumber()).isEqualTo(2L);
    assertThat(firstEntry.getFormattedLine()).contains("TRADE #0002");
    assertThat(firstEntry.getFormattedLine()).contains("SELL");
    assertThat(firstEntry.getFormattedLine()).contains("GMEE");
  }

  @Test
  @Sql("/sql/portfolio-with-history.sql")
  void getPaperTapeIsPaginated() {
    var result =
        restTestClient
            .get()
            .uri(PAPER_TAPE_URI + "?page=0&size=1")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(PaperTapeResponse.class);

    assertResponseMetadata(result.getResponseBody(), PAPER_TAPE_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getTotalElements()).isEqualTo(2);
    assertThat(data.getTotalPages()).isEqualTo(2);
    assertThat(data.getContent()).hasSize(1);
  }

  @Test
  @Sql(statements = {"DELETE FROM trade_history", "DELETE FROM position", "DELETE FROM portfolio"})
  void getPaperTapeEmptyWhenNoTrades() {
    var result =
        restTestClient
            .get()
            .uri(PAPER_TAPE_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(PaperTapeResponse.class);

    assertResponseMetadata(result.getResponseBody(), PAPER_TAPE_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getContent()).isEmpty();
    assertThat(data.getTotalElements()).isZero();
  }
}
