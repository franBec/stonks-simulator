package dev.pollito.stonks_java.chaos.application.port.in;

import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosLevel;
import java.util.List;

public interface ChaosPortIn {
  ChaosEvent triggerEvent();

  List<ChaosEvent> getHistory();

  ChaosLevel getCurrentLevel();

  void setLevel(ChaosLevel level);
}
