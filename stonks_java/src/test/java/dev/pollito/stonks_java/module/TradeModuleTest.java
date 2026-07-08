package dev.pollito.stonks_java.module;

import static dev.pollito.stonks_java.generated.model.TradeAction.BUY;
import static dev.pollito.stonks_java.generated.model.TradeAction.SELL;
import static dev.pollito.stonks_java.generated.model.TradeExecutionResult.StatusEnum.ACCEPTED;
import static dev.pollito.stonks_java.generated.model.TradeExecutionResult.StatusEnum.REJECTED;
import static dev.pollito.stonks_java.testsupport.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.TradeAction;
import dev.pollito.stonks_java.generated.model.TradeExecutionRequest;
import dev.pollito.stonks_java.generated.model.TradeExecutionResponse;
import dev.pollito.stonks_java.generated.model.TradeExecutionResult.StatusEnum;
import dev.pollito.stonks_java.generated.model.TradeHistoryResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(
    mode = DIRECT_DEPENDENCIES,
    webEnvironment = RANDOM_PORT,
    module = "trade",
    extraIncludes = "portfolio")
@AutoConfigureRestTestClient
class TradeModuleTest {

  private static final String TRADES_URI = "/api/trades";
  private static final String HISTORY_URI = "/api/trades";
  private static final String PORTFOLIO_RESET_URI = "/api/portfolio/reset";

  @Autowired private RestTestClient restTestClient;

  private static TradeExecutionRequest domainRequest(TradeAction action, String symbol, int qty) {
    return new TradeExecutionRequest().action(action).symbol(symbol).quantity(qty);
  }

  private static Stream<Arguments> executionScenarios() {
    return Stream.of(
        Arguments.of(domainRequest(BUY, "GMEE", 10), ACCEPTED, null, 10, 9500.0, 500.0),
        Arguments.of(domainRequest(BUY, "GMEE", 1000), REJECTED, "S222", 0, 10000.0, 0.0),
        Arguments.of(domainRequest(BUY, "FAKE", 10), REJECTED, "S001", 0, 10000.0, 0.0),
        Arguments.of(domainRequest(BUY, "GMEE", 0), REJECTED, "S224", 0, 10000.0, 0.0));
  }

  @ParameterizedTest
  @MethodSource("executionScenarios")
  @Sql("/sql/portfolio.sql")
  void executeTrade(
      TradeExecutionRequest request,
      StatusEnum expectedStatus,
      String expectedErrorCode,
      Integer expectedNewQty,
      Double expectedNewCashApprox,
      Double expectedCostApprox) {
    var result =
        restTestClient
            .post()
            .uri(TRADES_URI)
            .body(request)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeExecutionResponse.class);

    assertResponseMetadata(result.getResponseBody(), TRADES_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getStatus()).isEqualTo(expectedStatus);
    assertThat(data.getErrorCode()).isEqualTo(expectedErrorCode);
    assertThat(data.getNewQuantity()).isEqualTo(expectedNewQty);
    if (expectedStatus == ACCEPTED) {
      assertThat(data.getNewCashBalance()).isCloseTo(expectedNewCashApprox, within(200.0));
      assertThat(data.getTotalCost()).isCloseTo(expectedCostApprox, within(100.0));
    } else {
      assertThat(data.getNewCashBalance()).isCloseTo(expectedNewCashApprox, within(0.01));
      assertThat(data.getTotalCost()).isCloseTo(expectedCostApprox, within(0.01));
    }
  }

  @Test
  @Sql("/sql/portfolio-with-position.sql")
  void sellValid() {
    var result =
        restTestClient
            .post()
            .uri(TRADES_URI)
            .body(domainRequest(SELL, "GMEE", 5))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeExecutionResponse.class);

    assertResponseMetadata(result.getResponseBody(), TRADES_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getStatus()).isEqualTo(ACCEPTED);
    assertThat(data.getNewQuantity()).isEqualTo(5);
    assertThat(data.getNewCashBalance()).isCloseTo(10250.0, within(200.0));
    assertThat(data.getTotalCost()).isCloseTo(250.0, within(100.0));
  }

  @Test
  @Sql("/sql/portfolio-with-limited-position.sql")
  void insufficientShares() {
    var result =
        restTestClient
            .post()
            .uri(TRADES_URI)
            .body(domainRequest(SELL, "GMEE", 10))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeExecutionResponse.class);

    assertResponseMetadata(result.getResponseBody(), TRADES_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getStatus()).isEqualTo(REJECTED);
    assertThat(data.getErrorCode()).isEqualTo("S223");
    assertThat(data.getNewQuantity()).isEqualTo(3);
    assertThat(data.getNewCashBalance()).isEqualTo(10000.0);
    assertThat(data.getTotalCost()).isZero();
  }

  @Test
  @Sql("/sql/portfolio-with-history.sql")
  void paginatedHistory() {
    var result =
        restTestClient
            .get()
            .uri(HISTORY_URI + "?page=0&size=1&sort=executedAt,desc")
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
  @Sql("/sql/portfolio-with-history.sql")
  void paginatedHistoryWithSort() {
    var result =
        restTestClient
            .get()
            .uri(HISTORY_URI + "?page=0&size=1&sort=executedAt,asc")
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
    assertThat(item.getAction()).isEqualTo("BUY");
    assertThat(item.getSymbol()).isEqualTo("GMEE");
    assertThat(item.getQuantity()).isEqualTo(10);
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

  @Test
  @Sql("/sql/portfolio-with-position.sql")
  void resetClearsPositionsAndHistory() {
    var result =
        restTestClient
            .post()
            .uri(PORTFOLIO_RESET_URI)
            .exchange()
            .expectStatus()
            .isOk();

    var history =
        restTestClient
            .get()
            .uri(HISTORY_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeHistoryResponse.class);
    assertThat(history.getResponseBody().getData().getContent()).isEmpty();
    assertThat(history.getResponseBody().getData().getTotalElements()).isZero();
  }

  @Test
  @Sql("/sql/portfolio.sql")
  void resetIsIdempotent() {
    restTestClient.post().uri(PORTFOLIO_RESET_URI).exchange().expectStatus().isOk();
    restTestClient.post().uri(PORTFOLIO_RESET_URI).exchange().expectStatus().isOk();
  }
}
