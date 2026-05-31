package dev.pollito.stonks_java.unit.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaosevent.adapter.in.scheduler.ChaoseventScheduler;
import dev.pollito.stonks_java.chaosevent.application.port.in.ChaoseventPortIn;
import dev.pollito.stonks_java.chaosevent.config.ChaoseventProperties;
import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import dev.pollito.stonks_java.intensity.domain.IntensityLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChaoseventSchedulerTest {

  @Mock private ChaoseventPortIn chaoseventPortIn;
  @Mock private IntensityPortIn intensityPortIn;
  @Mock private ChaoseventProperties chaoseventProperties;
  @InjectMocks private ChaoseventScheduler scheduler;

  @Test
  void checkAndTriggerEventSkipsWhenChaosDisabled() {
    when(chaoseventProperties.isEnabled()).thenReturn(false);
    scheduler.checkAndTriggerEvent();
    verify(chaoseventPortIn, never()).triggerEvent();
  }

  @Test
  void checkAndTriggerEventSkipsWhenIntervalNotElapsed() {
    when(chaoseventProperties.isEnabled()).thenReturn(true);
    when(intensityPortIn.getCurrentLevel()).thenReturn(IntensityLevel.MAXIMUM_OVERDRIVE);
    scheduler.checkAndTriggerEvent();
    verify(chaoseventPortIn, never()).triggerEvent();
  }
}
