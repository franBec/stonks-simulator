package dev.pollito.stonks_java.trade;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static dev.pollito.stonks_java.generated.model.TradeAction.BUY;
import static dev.pollito.stonks_java.generated.model.TradeAction.SELL;
import static dev.pollito.stonks_java.generated.model.TradeValidationResult.StatusEnum.ACCEPTED;
import static dev.pollito.stonks_java.generated.model.TradeValidationResult.StatusEnum.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.Error;
import dev.pollito.stonks_java.generated.model.TradeAction;
import dev.pollito.stonks_java.generated.model.TradeValidationRequest;
import dev.pollito.stonks_java.generated.model.TradeValidationResponse;
import dev.pollito.stonks_java.generated.model.TradeValidationResult.StatusEnum;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class TradeValidationE2eTest {

  private static final String VALIDATE_URI = "/api/trades/validate";

  @Autowired private RestTestClient restTestClient;

  private static TradeValidationRequest request(
      TradeAction action, String symbol, int qty, double price, double cash) {
    return new TradeValidationRequest()
        .action(action)
        .symbol(symbol)
        .quantity(qty)
        .price(price)
        .cashBalance(cash);
  }

  private static Stream<Arguments> validationScenarios() {
    return Stream.of(
        Arguments.of(request(BUY, "GMEE", 10, 45.0, 10000.0), ACCEPTED, 450.0, 9550.0),
        Arguments.of(request(BUY, "GMEE", 1000, 45.0, 100.0), REJECTED, 0.0, 0.0),
        Arguments.of(request(BUY, "FAKE", 10, 45.0, 10000.0), REJECTED, 0.0, 0.0),
        Arguments.of(request(BUY, "GMEE", 0, 45.0, 10000.0), REJECTED, 0.0, 0.0),
        Arguments.of(request(BUY, "GMEE", 10, 0.0, 10000.0), REJECTED, 0.0, 0.0),
        Arguments.of(request(SELL, "GMEE", 10, 45.0, 1000.0), ACCEPTED, 450.0, 550.0));
  }

  @ParameterizedTest
  @MethodSource("validationScenarios")
  void validateTrade(
      TradeValidationRequest request,
      StatusEnum expectedStatus,
      Double expectedCost,
      Double expectedRemaining) {
    var result =
        restTestClient
            .post()
            .uri(VALIDATE_URI)
            .body(request)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeValidationResponse.class);

    assertResponseMetadata(result.getResponseBody(), VALIDATE_URI, 200);
    assertThat(result.getResponseBody().getData()).isNotNull();
    assertThat(result.getResponseBody().getData().getStatus()).isEqualTo(expectedStatus);
    assertThat(result.getResponseBody().getData().getTotalCost()).isEqualTo(expectedCost);
    assertThat(result.getResponseBody().getData().getRemainingCash()).isEqualTo(expectedRemaining);
  }

  @Test
  void badRequestOnNullAction() {
    var request =
        new TradeValidationRequest()
            .action(null)
            .symbol("GMEE")
            .quantity(10)
            .price(45.0)
            .cashBalance(10000.0);

    var result =
        restTestClient
            .post()
            .uri(VALIDATE_URI)
            .body(request)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .returnResult(Error.class);

    assertResponseMetadata(result.getResponseBody(), VALIDATE_URI, 400);
    assertThat(result.getResponseBody().getTitle()).isEqualTo("Bad Request");
  }

  @Test
  void notFoundOnUnknownPath() {
    var result =
        restTestClient
            .get()
            .uri("/api/trades/nonexistent")
            .exchange()
            .expectStatus()
            .isNotFound()
            .returnResult(Error.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades/nonexistent", 404);
    assertThat(result.getResponseBody().getTitle()).isEqualTo("Not Found");
  }
}
