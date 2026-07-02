package dev.pollito.stonks_java.chaosevent.adapter.in.scheduler;

import dev.pollito.stonks_java.chaosevent.application.port.in.ChaoseventPortIn;
import dev.pollito.stonks_java.chaosevent.config.ChaoseventProperties;
import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import dev.pollito.stonks_java.intensity.domain.IntensityLevelChanged;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChaoseventScheduler {

  private final ChaoseventPortIn chaoseventPortIn;
  private final IntensityPortIn intensityPortIn;
  private final ChaoseventProperties chaoseventProperties;
  private final TaskScheduler taskScheduler;

  private final AtomicBoolean running = new AtomicBoolean(false);
  private ScheduledFuture<?> scheduledFuture;

  @PostConstruct
  public void init() {
    scheduleTrigger();
  }

  @EventListener
  public void onIntensityLevelChanged(IntensityLevelChanged event) {
    scheduleTrigger();
  }

  private void scheduleTrigger() {
    cancelCurrent();
    long intervalMs = intensityPortIn.getCurrentLevel().getAiEventIntervalMs();
    scheduledFuture =
        taskScheduler.scheduleAtFixedRate(this::triggerIfEnabled, Duration.ofMillis(intervalMs));
    log.debug("Chaos event trigger scheduled every {}ms", intervalMs);
  }

  private void cancelCurrent() {
    if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
      scheduledFuture.cancel(false);
      scheduledFuture = null;
    }
  }

  void triggerIfEnabled() {
    if (!running.compareAndSet(false, true)) {
      log.warn("Skipping chaos event — previous trigger still running");
      return;
    }
    try {
      if (!chaoseventProperties.isEnabled()) {
        return;
      }
      log.debug("Triggering chaos event");
      chaoseventPortIn.triggerEvent();
    } finally {
      running.set(false);
    }
  }
}
