package dev.pollito.stonks_java.chaos;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static dev.pollito.stonks_java.generated.model.ChaosLevel.HIGH_VOLATILITY;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import dev.pollito.stonks_java.generated.model.ChaosEvent;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggerRequest;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggeredResponse;
import dev.pollito.stonks_java.generated.model.ChaosEventsResponse;
import dev.pollito.stonks_java.generated.model.ChaosLevel;
import dev.pollito.stonks_java.generated.model.ChaosLevelResponse;
import dev.pollito.stonks_java.generated.model.ChaosLevelSetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class ChaosFlowE2eTest {

  private static final String CHAOS_LEVEL_URI = "/api/chaos/level";
  private static final String CHAOS_EVENTS_URI = "/api/chaos/events";
  private static final String CHAOS_HISTORY_URI = "/api/chaos/history";

  @Autowired private RestTestClient restTestClient;

  @Test
  void getChaosLevelReturnsDefaultLevel() {
    var result =
        restTestClient
            .get()
            .uri(CHAOS_LEVEL_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosLevelResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_LEVEL_URI, 200);
    assertThat(result.getResponseBody().getData()).isEqualTo(ChaosLevel.PAPER_HANDS);
  }

  @Test
  void setChaosLevelChangesLevel() {
    var setResult =
        restTestClient
            .post()
            .uri(CHAOS_LEVEL_URI)
            .contentType(APPLICATION_JSON)
            .body(new ChaosLevelSetRequest().level(HIGH_VOLATILITY))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosLevelResponse.class);

    assertResponseMetadata(setResult.getResponseBody(), CHAOS_LEVEL_URI, 200);
    assertThat(setResult.getResponseBody().getData()).isEqualTo(HIGH_VOLATILITY);

    var getResult =
        restTestClient
            .get()
            .uri(CHAOS_LEVEL_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosLevelResponse.class);

    assertThat(getResult.getResponseBody().getData()).isEqualTo(HIGH_VOLATILITY);
  }

  @Test
  void triggerChaosEventReturnsEvent() {
    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(dev.pollito.stonks_java.generated.model.ChaosEventType.HYPE_WAVE)
                    .severity(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.MEDIUM))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    ChaosEvent event = result.getResponseBody().getData();
    assertThat(event).isNotNull();
    assertThat(event.getTitle()).isNotBlank();
    assertThat(event.getTargetSymbol()).isNotBlank();
    assertThat(event.getSeverity()).isNotNull();
    assertThat(event.getStartedAt()).isNotNull();
    assertThat(event.getExpiresAt()).isNotNull();
  }

  @Test
  void triggerChaosEventPopulatesHistory() {
    restTestClient
        .post()
        .uri(CHAOS_EVENTS_URI)
        .body(
            new ChaosEventTriggerRequest()
                .type(dev.pollito.stonks_java.generated.model.ChaosEventType.HYPE_WAVE)
                .severity(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.MEDIUM))
        .exchange()
        .expectStatus()
        .isOk();

    var result =
        restTestClient
            .get()
            .uri(CHAOS_EVENTS_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventsResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData()).isNotEmpty();
  }

  @Test
  void getChaosHistoryReturnsHistory() {
    restTestClient
        .post()
        .uri(CHAOS_EVENTS_URI)
        .body(
            new ChaosEventTriggerRequest()
                .type(dev.pollito.stonks_java.generated.model.ChaosEventType.HYPE_WAVE)
                .severity(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.MEDIUM))
        .exchange()
        .expectStatus()
        .isOk();

    var result =
        restTestClient
            .get()
            .uri(CHAOS_HISTORY_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventsResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_HISTORY_URI, 200);
    assertThat(result.getResponseBody().getData()).isNotEmpty();
  }

  @Test
  void getChaosEventReturnsNotFound() {
    restTestClient
        .get()
        .uri(CHAOS_EVENTS_URI + "/" + randomUUID())
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void cancelChaosEventReturnsNotFound() {
    restTestClient
        .delete()
        .uri(CHAOS_EVENTS_URI + "/" + randomUUID())
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void triggerChaosEventWithRequestSeverity() {
    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(dev.pollito.stonks_java.generated.model.ChaosEventType.HYPE_WAVE)
                    .severity(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.CRITICAL))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData().getSeverity())
        .isEqualTo(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.CRITICAL);
  }

  @Test
  void triggerChaosEventWithRequestTypeAndSeverity() {
    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(dev.pollito.stonks_java.generated.model.ChaosEventType.DUMP)
                    .severity(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.CRITICAL))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData().getType())
        .isEqualTo(dev.pollito.stonks_java.generated.model.ChaosEventType.DUMP);
    assertThat(result.getResponseBody().getData().getSeverity())
        .isEqualTo(dev.pollito.stonks_java.generated.model.ChaosEventSeverity.CRITICAL);
  }
}
