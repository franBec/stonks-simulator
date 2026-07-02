package dev.pollito.stonks_java.unit.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaosevent.adapter.in.scheduler.ChaoseventScheduler;
import dev.pollito.stonks_java.chaosevent.application.port.in.ChaoseventPortIn;
import dev.pollito.stonks_java.chaosevent.config.ChaoseventProperties;
import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import dev.pollito.stonks_java.intensity.domain.IntensityLevel;
import dev.pollito.stonks_java.intensity.domain.IntensityLevelChanged;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class ChaoseventSchedulerTest {

  @Mock private ChaoseventPortIn chaoseventPortIn;
  @Mock private IntensityPortIn intensityPortIn;
  @Mock private ChaoseventProperties chaoseventProperties;
  @Mock private TaskScheduler taskScheduler;
  @InjectMocks private ChaoseventScheduler scheduler;

  @Test
  void initSchedulesTriggerAtCurrentLevelInterval() {
    when(intensityPortIn.getCurrentLevel()).thenReturn(IntensityLevel.PAPER_HANDS);

    scheduler.init();

    verify(taskScheduler).scheduleAtFixedRate(any(Runnable.class), eq(Duration.ofMillis(900_000L)));
  }

  @Test
  void onIntensityLevelChangedReschedules() {
    when(intensityPortIn.getCurrentLevel()).thenReturn(IntensityLevel.MAXIMUM_OVERDRIVE);

    scheduler.onIntensityLevelChanged(new IntensityLevelChanged(IntensityLevel.MAXIMUM_OVERDRIVE));

    verify(taskScheduler).scheduleAtFixedRate(any(Runnable.class), eq(Duration.ofMillis(30_000L)));
  }

  @Test
  void triggerSkipsWhenDisabled() {
    when(intensityPortIn.getCurrentLevel()).thenReturn(IntensityLevel.PAPER_HANDS);
    when(chaoseventProperties.isEnabled()).thenReturn(false);
    scheduler.init();

    Runnable trigger = captureTrigger();
    trigger.run();

    verify(chaoseventPortIn, never()).triggerEvent();
  }

  @Test
  void triggerCallsPortWhenEnabled() {
    when(intensityPortIn.getCurrentLevel()).thenReturn(IntensityLevel.PAPER_HANDS);
    when(chaoseventProperties.isEnabled()).thenReturn(true);
    scheduler.init();

    Runnable trigger = captureTrigger();
    trigger.run();

    verify(chaoseventPortIn).triggerEvent();
  }

  private Runnable captureTrigger() {
    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    verify(taskScheduler).scheduleAtFixedRate(captor.capture(), any(Duration.class));
    return captor.getValue();
  }
}
