package dev.pollito.stonks_java.chaosevent.adapter.in.scheduler;

import static java.time.Duration.between;
import static java.time.Instant.now;

import dev.pollito.stonks_java.chaosevent.application.port.in.ChaoseventPortIn;
import dev.pollito.stonks_java.chaosevent.config.ChaoseventProperties;
import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChaoseventScheduler {

  private final ChaoseventPortIn chaoseventPortIn;
  private final IntensityPortIn intensityPortIn;
  private final ChaoseventProperties chaoseventProperties;

  private final AtomicReference<Instant> lastEvent = new AtomicReference<>(now());
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Scheduled(fixedRateString = "${stonks.chaos.event-check-interval-ms:30000}")
  public void checkAndTriggerEvent() {
    if (!running.compareAndSet(false, true)) {
      log.warn("Skipping chaos check — previous check still running");
      return;
    }
    try {
      if (!chaoseventProperties.isEnabled()) {
        return;
      }

      Instant now = now();

      if (between(lastEvent.get(), now).toMillis()
          >= intensityPortIn.getCurrentLevel().getAiEventIntervalMs()) {
        log.debug("Chaos event interval elapsed, triggering new event");
        chaoseventPortIn.triggerEvent();
        lastEvent.set(now);
      }
    } finally {
      running.set(false);
    }
  }
}
