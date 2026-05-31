package dev.pollito.stonks_java.module;

import static dev.pollito.stonks_java.testsupport.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.model.ChaoticEventSeverity;
import dev.pollito.stonks_java.generated.model.ChaoticEventTriggerRequest;
import dev.pollito.stonks_java.generated.model.ChaoticEventTriggeredResponse;
import dev.pollito.stonks_java.generated.model.ChaoticEventType;
import dev.pollito.stonks_java.generated.model.ChaoticEventsResponse;
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
    module = "chaosevent")
@AutoConfigureRestTestClient
@Sql(scripts = {"classpath:sql/intensity-level.sql", "classpath:sql/chaosevent-incident-log.sql"})
class ChaoseventModuleTest {

  private static final String CHAOTIC_EVENTS_URI = "/api/chaotic-events";

  @Autowired private RestTestClient restTestClient;

  private static Stream<Arguments> chaoticEventTypeAndSeverityProvider() {
    return Stream.of(
        arguments(ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.MEDIUM),
        arguments(ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.CRITICAL),
        arguments(ChaoticEventType.DUMP, ChaoticEventSeverity.CRITICAL),
        arguments(ChaoticEventType.NEWS_FLASH, ChaoticEventSeverity.LOW),
        arguments(ChaoticEventType.WHALE_ALERT, ChaoticEventSeverity.MEDIUM),
        arguments(ChaoticEventType.MEME_STORM, ChaoticEventSeverity.HIGH),
        arguments(ChaoticEventType.RUG_PULL, ChaoticEventSeverity.CRITICAL),
        arguments(ChaoticEventType.PUMP_AND_DUMP, ChaoticEventSeverity.HIGH));
  }

  @ParameterizedTest(name = "triggerChaoticEvent({0}, {1})")
  @MethodSource("chaoticEventTypeAndSeverityProvider")
  void triggerChaoticEvent(ChaoticEventType type, ChaoticEventSeverity severity) {
    var result =
        restTestClient
            .post()
            .uri(CHAOTIC_EVENTS_URI)
            .body(new ChaoticEventTriggerRequest().type(type).severity(severity))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaoticEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOTIC_EVENTS_URI, 200);
    var event = result.getResponseBody().getData();
    assertThat(event).isNotNull();
    assertThat(event.getType()).isEqualTo(type);
    assertThat(event.getSeverity()).isEqualTo(severity);
    assertThat(event.getTitle()).isNotBlank();
    assertThat(event.getTargetSymbol()).isNotBlank();
    assertThat(event.getStartedAt()).isNotNull();
    assertThat(event.getExpiresAt()).isNotNull();
  }

  @Test
  void triggerChaoticEventPopulatesHistory() {
    restTestClient
        .post()
        .uri(CHAOTIC_EVENTS_URI)
        .body(
            new ChaoticEventTriggerRequest()
                .type(ChaoticEventType.HYPE_WAVE)
                .severity(ChaoticEventSeverity.MEDIUM))
        .exchange()
        .expectStatus()
        .isOk();

    var result =
        restTestClient
            .get()
            .uri(CHAOTIC_EVENTS_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaoticEventsResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOTIC_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData()).isNotEmpty();
  }
}
