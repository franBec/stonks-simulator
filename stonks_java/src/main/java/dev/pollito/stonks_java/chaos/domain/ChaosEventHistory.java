package dev.pollito.stonks_java.chaos.domain;

import java.util.ArrayDeque;
import java.util.List;

public class ChaosEventHistory {

  private static final int MAX_HISTORY = 100;

  private final ArrayDeque<ChaosEvent> events = new ArrayDeque<>(MAX_HISTORY);

  public synchronized void add(ChaosEvent event) {
    if (events.size() >= MAX_HISTORY) {
      events.removeFirst();
    }
    events.addLast(event);
  }

  public synchronized List<ChaosEvent> getAll() {
    return List.copyOf(events);
  }
}
