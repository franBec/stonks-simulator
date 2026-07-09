package dev.pollito.stonks_java.broadcast.application.service;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Map.of;
import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

import dev.pollito.stonks_java.broadcast.application.port.in.BroadcastPortIn;
import dev.pollito.stonks_java.broadcast.config.BroadcastProperties;
import dev.pollito.stonks_java.broadcast.domain.BroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.ChaosBroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.GameLostBroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.GameResetBroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.GameWonBroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.PriceTickBroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.SpeedBroadcastEvent;
import dev.pollito.stonks_java.broadcast.domain.TradeExecutedBroadcastEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventTriggered;
import dev.pollito.stonks_java.config.GameLostEvent;
import dev.pollito.stonks_java.config.GameWonEvent;
import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import dev.pollito.stonks_java.intensity.domain.IntensityLevel;
import dev.pollito.stonks_java.intensity.domain.IntensityLevelChanged;
import dev.pollito.stonks_java.portfolio.domain.GameResetEvent;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import dev.pollito.stonks_java.trade.domain.TradeAction;
import dev.pollito.stonks_java.trade.domain.TradeExecutedEvent;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastSseService implements BroadcastPortIn {

  private final BroadcastProperties broadcastProperties;
  private final IntensityPortIn intensityPortIn;

  @Value("${stonks.market.simulation.interval-ms:5000}")
  private long tickIntervalMs;

  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  private final AtomicLong paperTapeSequence = new AtomicLong(0);

  @Override
  public SseEmitter createEmitter() {
    SseEmitter emitter = new SseEmitter(broadcastProperties.getSseTimeoutMs());
    emitters.add(emitter);
    log.info("SSE client connected, total: {}", emitters.size());

    emitter.onCompletion(
        () -> {
          emitters.remove(emitter);
          log.info("SSE client disconnected, total: {}", emitters.size());
        });
    emitter.onTimeout(
        () -> {
          emitters.remove(emitter);
          log.info("SSE client timed out, total: {}", emitters.size());
        });
    emitter.onError(
        ex -> {
          emitters.remove(emitter);
          log.info("SSE client error, total: {}", emitters.size());
        });

    try {
      emitter.send(event().name("connected").data("{\"message\":\"Connected to stonks stream\"}"));
      emitter.send(buildSseEvent(buildSpeedConfig()));
    } catch (IOException e) {
      log.warn("Failed to send initial connection events", e);
    }

    return emitter;
  }

  private void broadcast(BroadcastEvent event) {
    List<SseEmitter> deadEmitters = new ArrayList<>();
    for (SseEmitter emitter : emitters) {
      try {
        SseEmitter.SseEventBuilder sseEvent = buildSseEvent(event);
        emitter.send(sseEvent);
      } catch (Exception e) {
        deadEmitters.add(emitter);
      }
    }
    emitters.removeAll(deadEmitters);
  }

  @EventListener
  void onStockPriceUpdated(StockPriceUpdatedEvent event) {
    broadcast(new PriceTickBroadcastEvent(event.stockPrice()));
  }

  @EventListener
  void onTradeExecuted(TradeExecutedEvent event) {
    broadcast(
        new TradeExecutedBroadcastEvent(
            event.action(), event.result(), event.symbol(), event.quantity()));
  }

  @EventListener
  void onChaoticEventTriggered(ChaoticEventTriggered event) {
    broadcast(
        new ChaosBroadcastEvent(
            event.chaoticEvent().headline(),
            event.chaoticEvent().symbol(),
            event.chaoticEvent().impactPercent().doubleValue(),
            event.chaoticEvent().explanation()));
  }

  @EventListener
  void onIntensityLevelChanged(IntensityLevelChanged event) {
    broadcast(buildSpeedConfig());
  }

  @EventListener
  void onGameReset(GameResetEvent event) {
    broadcast(new GameResetBroadcastEvent());
  }

  @EventListener
  void onGameWon(GameWonEvent event) {
    broadcast(new GameWonBroadcastEvent());
  }

  @EventListener
  void onGameLost(GameLostEvent event) {
    broadcast(new GameLostBroadcastEvent());
  }

  @Scheduled(fixedRateString = "${stonks.broadcast.heartbeat-rate-ms:15000}")
  void sendHeartbeat() {
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(event().comment("heartbeat"));
      } catch (IOException ignored) {
        // Will be cleaned up on next broadcast or timeout
      }
    }
  }

  private SseEmitter.SseEventBuilder buildSseEvent(BroadcastEvent event) {
    String eventName = event.type().name();
    Object dataToSend;

    switch (event) {
      case PriceTickBroadcastEvent pt -> dataToSend = pt.stockPrice();
      case TradeExecutedBroadcastEvent te ->
          dataToSend =
              of(
                  "result", te.result(),
                  "symbol", te.symbol(),
                  "quantity", te.quantity(),
                  "paperTape",
                      formatTradePaperTape(te.result(), te.symbol(), te.quantity(), te.action()));
      case ChaosBroadcastEvent ch ->
          dataToSend =
              of(
                  "headline", ch.headline(),
                  "symbol", ch.symbol(),
                  "impact", ch.impact(),
                  "explanation", ch.explanation());
      case SpeedBroadcastEvent sc ->
          dataToSend =
              of(
                  "tickIntervalMs", sc.tickIntervalMs(),
                  "intensityLevel", sc.intensityLevel(),
                  "volatilityMultiplier", sc.volatilityMultiplier(),
                  "aiEventIntervalMs", sc.aiEventIntervalMs());
      case GameResetBroadcastEvent gr ->
          dataToSend = of("message", "Game has been reset");
      case GameWonBroadcastEvent gw ->
          dataToSend = of("message", "You won!");
      case GameLostBroadcastEvent gl ->
          dataToSend = of("message", "You lost!");
      default -> dataToSend = event;
    }

    return event()
        .name(eventName)
        .data(dataToSend)
        .id(valueOf(paperTapeSequence.incrementAndGet()));
  }

  private String formatTradePaperTape(
      TradeExecutionResult result, String symbol, int quantity, TradeAction action) {
    double unitPrice = quantity > 0 ? result.totalCost() / quantity : 0;
    return format(
        broadcastProperties.getTradePaperTapeFormat(),
        action.getValue(),
        quantity,
        symbol,
        unitPrice,
        result.totalCost());
  }

  private SpeedBroadcastEvent buildSpeedConfig() {
    IntensityLevel level = intensityPortIn.getCurrentLevel();
    return new SpeedBroadcastEvent(
        tickIntervalMs,
        level.name(),
        level.getVolatilityMultiplier(),
        level.getAiEventIntervalMs());
  }
}
