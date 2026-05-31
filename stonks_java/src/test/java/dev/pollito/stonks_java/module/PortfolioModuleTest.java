package dev.pollito.stonks_java.module;

import static dev.pollito.stonks_java.testsupport.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.PortfolioResponse;
import dev.pollito.stonks_java.generated.model.Position;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(
    mode = DIRECT_DEPENDENCIES,
    webEnvironment = RANDOM_PORT,
    module = "portfolio")
@AutoConfigureRestTestClient
class PortfolioModuleTest {

  private static final String PORTFOLIO_URI = "/api/portfolio";

  @Autowired private RestTestClient restTestClient;

  @Test
  @Sql("/sql/portfolio.sql")
  void initialPortfolio() {
    var result =
        restTestClient
            .get()
            .uri(PORTFOLIO_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(PortfolioResponse.class);

    assertResponseMetadata(result.getResponseBody(), PORTFOLIO_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getCashBalance()).isEqualTo(10000.0);
    assertThat(data.getPositions()).isEmpty();
    assertThat(data.getUnrealizedPnl()).isEqualTo(0.0);
  }

  @Test
  @Sql("/sql/portfolio-with-multiple-positions.sql")
  void portfolioWithMultiplePositionsComputesPnl() {
    var result =
        restTestClient
            .get()
            .uri(PORTFOLIO_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(PortfolioResponse.class);

    assertResponseMetadata(result.getResponseBody(), PORTFOLIO_URI, 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getCashBalance()).isEqualTo(8500.0);

    List<Position> positions = data.getPositions();
    assertThat(positions).hasSize(2);

    Position gmee =
        positions.stream().filter(p -> "GMEE".equals(p.getSymbol())).findFirst().orElseThrow();
    Position doge =
        positions.stream().filter(p -> "DOGE".equals(p.getSymbol())).findFirst().orElseThrow();

    assertThat(gmee.getQuantity()).isEqualTo(10);
    assertThat(doge.getQuantity()).isEqualTo(50);

    assertThat(gmee.getCurrentPrice()).isGreaterThan(0);
    assertThat(doge.getCurrentPrice()).isGreaterThan(0);

    Double gmeePnl = gmee.getUnrealizedPnl();
    Double dogePnl = doge.getUnrealizedPnl();
    Double totalPnl = data.getUnrealizedPnl();

    assertThat(gmeePnl).isNotNull();
    assertThat(dogePnl).isNotNull();
    assertThat(totalPnl).isCloseTo(gmeePnl + dogePnl, within(0.01));
  }
}
