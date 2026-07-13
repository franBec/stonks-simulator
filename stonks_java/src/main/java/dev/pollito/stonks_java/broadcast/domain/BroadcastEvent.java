package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public sealed interface BroadcastEvent
    permits PriceTickBroadcastEvent,
        TradeExecutedBroadcastEvent,
        ChaosBroadcastEvent,
        SpeedBroadcastEvent,
        GameResetBroadcastEvent,
        GameWonBroadcastEvent,
        GameLostBroadcastEvent,
        GameConfigBroadcastEvent {
  BroadcastEventType type();

  OffsetDateTime occurredAt();
}
