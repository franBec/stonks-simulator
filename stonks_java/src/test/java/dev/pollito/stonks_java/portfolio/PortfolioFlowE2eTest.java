package dev.pollito.stonks_java.portfolio;

import static dev.pollito.stonks_java.test.util.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.PortfolioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql("/sql/portfolio.sql")
class PortfolioFlowE2eTest {

  @Autowired private RestTestClient restTestClient;

  @Test
  void initialPortfolio() {
    var result =
        restTestClient
            .get()
            .uri("/api/portfolio")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(PortfolioResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/portfolio", 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getCashBalance()).isEqualTo(10000.0);
    assertThat(data.getPositions()).isEmpty();
    assertThat(data.getUnrealizedPnl()).isEqualTo(0.0);
  }
}
