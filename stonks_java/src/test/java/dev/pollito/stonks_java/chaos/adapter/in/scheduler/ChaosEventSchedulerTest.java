package dev.pollito.stonks_java.chaos.adapter.in.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.chaos.config.ChaosProperties;
import dev.pollito.stonks_java.chaos.domain.ChaosLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Unit test (not E2E) because the scheduler's internal state (isEnabled flag and
// lastEvent timestamp) is not observable through HTTP. The two branches guarded by
// these flags — chaos disabled and interval not yet elapsed — cannot be triggered via
// REST endpoints without coupling to scheduler timing.
@ExtendWith(MockitoExtension.class)
class ChaosEventSchedulerTest {

  @Mock private ChaosPortIn chaosPortIn;
  @Mock private ChaosProperties chaosProperties;
  @InjectMocks private ChaosEventScheduler scheduler;

  @Test
  void doesNothingWhenChaosDisabled() {
    scheduler.checkAndTriggerEvent();

    verifyNoInteractions(chaosPortIn);
  }

  @Test
  void doesNothingWhenIntervalNotElapsed() {
    when(chaosProperties.isEnabled()).thenReturn(true);
    when(chaosPortIn.getCurrentLevel()).thenReturn(ChaosLevel.PAPER_HANDS);

    scheduler.checkAndTriggerEvent();

    verify(chaosPortIn).getCurrentLevel();
    verify(chaosPortIn, never()).triggerEvent();
  }
}
