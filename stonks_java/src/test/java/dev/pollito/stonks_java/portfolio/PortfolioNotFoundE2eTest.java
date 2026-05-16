package dev.pollito.stonks_java.portfolio;

import static dev.pollito.stonks_java.test.util.RestTestClientAssertions.assertResponseMetadata;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.Error;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class PortfolioNotFoundE2eTest {

  private static final String PORTFOLIO_URI = "/api/portfolio";

  @Autowired private RestTestClient restTestClient;

  @Test
  @Sql(statements = {"DELETE FROM trade_history", "DELETE FROM position", "DELETE FROM portfolio"})
  void portfolioNotFound_returns404() {
    var result =
        restTestClient
            .get()
            .uri(PORTFOLIO_URI)
            .exchange()
            .expectStatus()
            .isNotFound()
            .returnResult(Error.class);

    assertResponseMetadata(result.getResponseBody(), PORTFOLIO_URI, NOT_FOUND.value());
    assertEquals("Not Found", result.getResponseBody().getTitle());
  }
}
