package dev.pollito.stonks_java.module;

import static dev.pollito.stonks_java.testsupport.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.IntensityLevel;
import dev.pollito.stonks_java.generated.model.IntensityLevelResponse;
import dev.pollito.stonks_java.generated.model.IntensityLevelSetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(
    mode = DIRECT_DEPENDENCIES,
    webEnvironment = RANDOM_PORT,
    module = "intensity")
@AutoConfigureRestTestClient
@Sql(scripts = {"classpath:sql/intensity-level.sql"})
class IntensityModuleTest {

  private static final String INTENSITY_LEVEL_URI = "/api/intensity-level";

  @Autowired private RestTestClient restTestClient;

  @Test
  void getIntensityLevelReturnsDefaultLevel() {
    var result =
        restTestClient
            .get()
            .uri(INTENSITY_LEVEL_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(IntensityLevelResponse.class);

    assertResponseMetadata(result.getResponseBody(), INTENSITY_LEVEL_URI, 200);
    assertThat(result.getResponseBody().getData()).isEqualTo(IntensityLevel.PAPER_HANDS);
  }

  @Test
  void setIntensityLevelChangesLevel() {
    var result =
        restTestClient
            .post()
            .uri(INTENSITY_LEVEL_URI)
            .contentType(APPLICATION_JSON)
            .body(new IntensityLevelSetRequest().level(IntensityLevel.HIGH_VOLATILITY))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(IntensityLevelResponse.class);

    assertResponseMetadata(result.getResponseBody(), INTENSITY_LEVEL_URI, 200);
    assertThat(result.getResponseBody().getData()).isEqualTo(IntensityLevel.HIGH_VOLATILITY);

    var getResult =
        restTestClient
            .get()
            .uri(INTENSITY_LEVEL_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(IntensityLevelResponse.class);

    assertThat(getResult.getResponseBody().getData()).isEqualTo(IntensityLevel.HIGH_VOLATILITY);
  }
}
