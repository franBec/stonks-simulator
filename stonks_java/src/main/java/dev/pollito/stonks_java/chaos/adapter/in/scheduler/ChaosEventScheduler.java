package dev.pollito.stonks_java.chaos.adapter.in.scheduler;

import static java.time.Duration.between;

import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.chaos.config.ChaosProperties;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChaosEventScheduler {

  private final ChaosPortIn chaosPortIn;
  private final ChaosProperties chaosProperties;

  private final AtomicReference<Instant> lastEvent = new AtomicReference<>(Instant.now());

  @Scheduled(fixedRateString = "${stonks.chaos.event-check-interval-ms:10000}")
  void checkAndTriggerEvent() {
    if (!chaosProperties.isEnabled()) {
      return;
    }

    Instant now = Instant.now();

    if (between(lastEvent.get(), now).toMillis()
        >= chaosPortIn.getCurrentLevel().getAiEventIntervalMs()) {
      log.debug("Chaos event interval elapsed, triggering new event");
      chaosPortIn.triggerEvent();
      lastEvent.set(now);
    }
  }
}
