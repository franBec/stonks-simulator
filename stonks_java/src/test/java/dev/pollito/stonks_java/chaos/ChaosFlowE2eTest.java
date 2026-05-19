package dev.pollito.stonks_java.chaos;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.generated.model.ChaosEvent;
import dev.pollito.stonks_java.generated.model.ChaosEventSeverity;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggerRequest;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggeredResponse;
import dev.pollito.stonks_java.generated.model.ChaosEventType;
import dev.pollito.stonks_java.generated.model.ChaosEventsResponse;
import dev.pollito.stonks_java.generated.model.ChaosLevel;
import dev.pollito.stonks_java.generated.model.ChaosLevelResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
class ChaosFlowE2eTest {

  private static final String CHAOS_LEVEL_URI = "/api/chaos/level";
  private static final String CHAOS_EVENTS_URI = "/api/chaos/events";
  private static final String CHAOS_HISTORY_URI = "/api/chaos/history";

  @Autowired private RestTestClient restTestClient;
  @Autowired private ChaosEventGeneratorPortOut chaosEventGeneratorPortOut;

  @TestConfiguration
  static class MockConfig {
    @Bean
    static BeanPostProcessor chaosGeneratorReplacer() {
      return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
          if (bean instanceof ChaosEventGeneratorPortOut) {
            return Mockito.mock(ChaosEventGeneratorPortOut.class);
          }
          return bean;
        }
      };
    }
  }

  @BeforeEach
  void setUp() {
    when(chaosEventGeneratorPortOut.generate(any(), any()))
        .thenReturn(
            new dev.pollito.stonks_java.chaos.domain.ChaosEvent(
                "Meme Stonks Go Brrr!",
                "GMEE",
                valueOf(15.0),
                "The algo detected extreme meme energy in the market. To the moon!",
                of("GMEE"),
                "Market Pulse",
                now()));
  }

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
            .contentType(MediaType.APPLICATION_JSON)
            .body("HIGH_VOLATILITY")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosLevelResponse.class);

    assertResponseMetadata(setResult.getResponseBody(), CHAOS_LEVEL_URI, 200);
    assertThat(setResult.getResponseBody().getData()).isEqualTo(ChaosLevel.HIGH_VOLATILITY);

    var getResult =
        restTestClient
            .get()
            .uri(CHAOS_LEVEL_URI)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosLevelResponse.class);

    assertThat(getResult.getResponseBody().getData()).isEqualTo(ChaosLevel.HIGH_VOLATILITY);
  }

  @Test
  void triggerChaosEventReturnsEvent() {
    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(ChaosEventType.HYPE_WAVE)
                    .severity(ChaosEventSeverity.MEDIUM))
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
                .type(ChaosEventType.HYPE_WAVE)
                .severity(ChaosEventSeverity.MEDIUM))
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
                .type(ChaosEventType.HYPE_WAVE)
                .severity(ChaosEventSeverity.MEDIUM))
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
        .uri(CHAOS_EVENTS_URI + "/" + UUID.randomUUID())
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void cancelChaosEventReturnsNotFound() {
    restTestClient
        .delete()
        .uri(CHAOS_EVENTS_URI + "/" + UUID.randomUUID())
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void triggerChaosEventWithCriticalSeverity() {
    when(chaosEventGeneratorPortOut.generate(any(), any()))
        .thenReturn(
            new dev.pollito.stonks_java.chaos.domain.ChaosEvent(
                "Meme Stonks Go Brrr!",
                "GMEE",
                valueOf(25.0),
                "The algo detected extreme meme energy.",
                of("GMEE"),
                "Market Pulse",
                now()));

    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(ChaosEventType.HYPE_WAVE)
                    .severity(ChaosEventSeverity.MEDIUM))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData().getSeverity())
        .isEqualTo(ChaosEventSeverity.CRITICAL);
  }

  @Test
  void triggerChaosEventWithHighSeverity() {
    when(chaosEventGeneratorPortOut.generate(any(), any()))
        .thenReturn(
            new dev.pollito.stonks_java.chaos.domain.ChaosEvent(
                "Meme Stonks Go Brrr!",
                "GMEE",
                valueOf(15.0),
                "The algo detected extreme meme energy.",
                of("GMEE"),
                "Market Pulse",
                now()));

    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(ChaosEventType.HYPE_WAVE)
                    .severity(ChaosEventSeverity.MEDIUM))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData().getSeverity()).isEqualTo(ChaosEventSeverity.HIGH);
  }

  @Test
  void triggerChaosEventWithMediumSeverity() {
    when(chaosEventGeneratorPortOut.generate(any(), any()))
        .thenReturn(
            new dev.pollito.stonks_java.chaos.domain.ChaosEvent(
                "Meme Stonks Go Brrr!",
                "GMEE",
                valueOf(7.5),
                "The algo detected extreme meme energy.",
                of("GMEE"),
                "Market Pulse",
                now()));

    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(ChaosEventType.HYPE_WAVE)
                    .severity(ChaosEventSeverity.MEDIUM))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData().getSeverity())
        .isEqualTo(ChaosEventSeverity.MEDIUM);
  }

  @Test
  void triggerChaosEventWithLowSeverity() {
    when(chaosEventGeneratorPortOut.generate(any(), any()))
        .thenReturn(
            new dev.pollito.stonks_java.chaos.domain.ChaosEvent(
                "Meme Stonks Go Brrr!",
                "GMEE",
                valueOf(3.0),
                "The algo detected extreme meme energy.",
                of("GMEE"),
                "Market Pulse",
                now()));

    var result =
        restTestClient
            .post()
            .uri(CHAOS_EVENTS_URI)
            .body(
                new ChaosEventTriggerRequest()
                    .type(ChaosEventType.HYPE_WAVE)
                    .severity(ChaosEventSeverity.MEDIUM))
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ChaosEventTriggeredResponse.class);

    assertResponseMetadata(result.getResponseBody(), CHAOS_EVENTS_URI, 200);
    assertThat(result.getResponseBody().getData().getSeverity()).isEqualTo(ChaosEventSeverity.LOW);
  }
}
