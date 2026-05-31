package dev.pollito.stonks_java.chaosevent.domain;

import java.util.ArrayDeque;
import java.util.List;

public class ChaoticEventHistory {

  private static final int MAX_HISTORY = 100;

  private final ArrayDeque<ChaoticEvent> events = new ArrayDeque<>(MAX_HISTORY);

  public synchronized void loadFrom(List<ChaoticEvent> initialEvents) {
    events.clear();
    for (ChaoticEvent event : initialEvents) {
      if (events.size() >= MAX_HISTORY) break;
      events.addLast(event);
    }
  }

  public synchronized void add(ChaoticEvent event) {
    if (events.size() >= MAX_HISTORY) {
      events.removeFirst();
    }
    events.addLast(event);
  }

  public synchronized List<ChaoticEvent> getAll() {
    return List.copyOf(events);
  }
}
