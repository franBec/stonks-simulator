package dev.pollito.stonks_java.chaosevent.application.port.in;

import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import java.util.List;

public interface ChaoseventPortIn {
  ChaoticEvent triggerEvent();

  ChaoticEvent triggerEvent(
      ChaoticEventType type, ChaoticEventSeverity severity, String targetSymbol);

  List<ChaoticEvent> getHistory();
}
