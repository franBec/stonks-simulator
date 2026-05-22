package dev.pollito.stonks_java.chaos.adapter.in.scheduler;

import static dev.pollito.stonks_java.chaos.domain.ChaosLevel.PAPER_HANDS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.chaos.config.ChaosProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Low coverage on ChaosEventScheduler.java is acceptable. The remaining branches
// (reentrant guard and happy path after interval elapses) depend on private mutable
// state (AtomicBoolean running, AtomicReference<Instant> lastEvent) that can only be
// set via reflection, which we prefer to avoid.
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
    when(chaosPortIn.getCurrentLevel()).thenReturn(PAPER_HANDS);

    scheduler.checkAndTriggerEvent();

    verify(chaosPortIn).getCurrentLevel();
    verify(chaosPortIn, never()).triggerEvent();
  }
}
