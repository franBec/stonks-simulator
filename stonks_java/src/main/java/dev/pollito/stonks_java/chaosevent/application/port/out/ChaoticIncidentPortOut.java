package dev.pollito.stonks_java.chaosevent.application.port.out;

import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import java.util.List;

public interface ChaoticIncidentPortOut {
  void recordEvent(ChaoticEvent event);

  List<ChaoticEvent> loadHistory();
}
