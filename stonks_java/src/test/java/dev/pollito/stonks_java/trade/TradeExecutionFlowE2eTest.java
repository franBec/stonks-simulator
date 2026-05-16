package dev.pollito.stonks_java.trade;

import static dev.pollito.stonks_java.generated.model.TradeAction.BUY;
import static dev.pollito.stonks_java.generated.model.TradeAction.SELL;
import static dev.pollito.stonks_java.generated.model.TradeExecutionResult.StatusEnum.ACCEPTED;
import static dev.pollito.stonks_java.generated.model.TradeExecutionResult.StatusEnum.REJECTED;
import static dev.pollito.stonks_java.test.util.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.TradeAction;
import dev.pollito.stonks_java.generated.model.TradeExecutionRequest;
import dev.pollito.stonks_java.generated.model.TradeExecutionResponse;
import dev.pollito.stonks_java.generated.model.TradeExecutionResult.StatusEnum;
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

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class TradeExecutionFlowE2eTest {

  @Autowired private RestTestClient restTestClient;

  private static TradeExecutionRequest domainRequest(TradeAction action, String symbol, int qty) {
    return new TradeExecutionRequest().action(action).symbol(symbol).quantity(qty);
  }

  private static Stream<Arguments> executionScenarios() {
    return Stream.of(
        Arguments.of(domainRequest(BUY, "GMEE", 10), ACCEPTED, null, 10, 9500.0, 500.0),
        Arguments.of(domainRequest(BUY, "GMEE", 1000), REJECTED, "S222", 0, 0.0, 0.0),
        Arguments.of(domainRequest(BUY, "FAKE", 10), REJECTED, "S001", 0, 0.0, 0.0),
        Arguments.of(domainRequest(BUY, "GMEE", 0), REJECTED, "S224", 0, 0.0, 0.0));
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
            .uri("/api/trades")
            .body(request)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeExecutionResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades", 200);
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
            .uri("/api/trades")
            .body(domainRequest(SELL, "GMEE", 5))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeExecutionResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades", 200);
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
            .uri("/api/trades")
            .body(domainRequest(SELL, "GMEE", 10))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeExecutionResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades", 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getStatus()).isEqualTo(REJECTED);
    assertThat(data.getErrorCode()).isEqualTo("S223");
    assertThat(data.getNewQuantity()).isZero();
    assertThat(data.getNewCashBalance()).isZero();
    assertThat(data.getTotalCost()).isZero();
  }
}
