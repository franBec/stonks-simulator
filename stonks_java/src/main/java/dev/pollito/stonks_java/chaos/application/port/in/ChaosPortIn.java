package dev.pollito.stonks_java.chaos.application.port.in;

import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity;
import dev.pollito.stonks_java.chaos.domain.ChaosEventType;
import dev.pollito.stonks_java.chaos.domain.ChaosLevel;
import java.util.List;

public interface ChaosPortIn {
  ChaosEvent triggerEvent();

  ChaosEvent triggerEvent(ChaosEventType type, ChaosEventSeverity severity, String targetSymbol);

  List<ChaosEvent> getHistory();

  ChaosLevel getCurrentLevel();

  void setLevel(ChaosLevel level);
}
