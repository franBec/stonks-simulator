package dev.pollito.stonks_java.broadcast.application.service;

import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.broadcast.config.BroadcastProperties;
import dev.pollito.stonks_java.broadcast.domain.PaperTapeEntry;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.domain.TradeAction;
import dev.pollito.stonks_java.trade.domain.TradeExecutedEvent;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class BroadcastSseServiceTest {

  private static final long SSE_TIMEOUT_MS = 300_000L;
  private static final String PAPER_TAPE_FORMAT = "TRADE #%04d | %s %d %s @ $%.2f | TOTAL: $%.2f";
  private static final String TRADE_PAPER_TAPE_FORMAT = "TRADE | %s %d %s @ $%.2f | TOTAL: $%.2f";

  private final TradePortIn tradePortIn = mock(TradePortIn.class);
  private final BroadcastProperties broadcastProperties = mock(BroadcastProperties.class);
  private BroadcastSseService service;

  @BeforeEach
  void setUp() throws Exception {
    when(broadcastProperties.getSseTimeoutMs()).thenReturn(SSE_TIMEOUT_MS);
    when(broadcastProperties.getPaperTapeEntryFormat()).thenReturn(PAPER_TAPE_FORMAT);
    when(broadcastProperties.getTradePaperTapeFormat()).thenReturn(TRADE_PAPER_TAPE_FORMAT);
    service = new BroadcastSseService(tradePortIn, broadcastProperties);
  }

  @Test
  void createEmitterReturnsEmitter() {
    SseEmitter emitter = service.createEmitter();

    assertThat(emitter).isNotNull();
  }

  @Test
  void createEmitterMultipleCallsReturnsDistinctEmitters() {
    SseEmitter first = service.createEmitter();
    SseEmitter second = service.createEmitter();

    assertThat(first).isNotSameAs(second);
  }

  @Test
  void getPaperTapeReturnsFormattedEntries() {
    when(tradePortIn.getTradeHistory(any()))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    new TradeHistoryItem(42L, "BUY", "GMEE", 10, 47.85, 478.50, 9521.50, now()))));

    Page<PaperTapeEntry> result = service.getPaperTape(PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    PaperTapeEntry entry = result.getContent().getFirst();
    assertThat(entry.sequenceNumber()).isEqualTo(42L);
    assertThat(entry.formattedLine()).contains("TRADE #0042");
    assertThat(entry.formattedLine()).contains("BUY 10 GMEE");
    assertThat(entry.formattedLine()).contains("$47.85");
    assertThat(entry.formattedLine()).contains("$478.50");
  }

  @Test
  void getPaperTapeEmptyWhenNoTrades() {
    when(tradePortIn.getTradeHistory(any())).thenReturn(new PageImpl<>(List.of()));
    assertThat(service.getPaperTape(PageRequest.of(0, 10))).isEmpty();
  }

  @Test
  void onStockPriceUpdatedBroadcastsToAllEmitters() throws Exception {
    SseEmitter mockEmitter = mock(SseEmitter.class);
    addMockEmitter(mockEmitter);

    service.onStockPriceUpdated(
        new StockPriceUpdatedEvent(
            new StockPrice(
                "GMEE",
                "GameStonks",
                valueOf(50.25),
                valueOf(50.00),
                valueOf(0.25),
                valueOf(0.50),
                now())));

    verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  void onTradeExecutedBroadcastsToAllEmitters() throws Exception {
    SseEmitter mockEmitter = mock(SseEmitter.class);
    addMockEmitter(mockEmitter);

    service.onTradeExecuted(
        new TradeExecutedEvent(
            TradeAction.BUY,
            new TradeExecutionResult(ValidationStatus.ACCEPTED, null, "OK", 9550.0, 10, 450.0),
            "GMEE",
            10));

    verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  void sendHeartbeatSendsCommentToAllEmitters() throws Exception {
    SseEmitter mockEmitter = mock(SseEmitter.class);
    addMockEmitter(mockEmitter);

    service.sendHeartbeat();

    verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  void deadEmitterIsRemovedOnError() throws Exception {
    SseEmitter mockEmitter = mock(SseEmitter.class);
    doThrow(new IOException("test error"))
        .when(mockEmitter)
        .send(any(SseEmitter.SseEventBuilder.class));
    addMockEmitter(mockEmitter);

    service.onStockPriceUpdated(
        new StockPriceUpdatedEvent(
            new StockPrice(
                "GMEE",
                "GameStonks",
                valueOf(50.25),
                valueOf(50.00),
                valueOf(0.25),
                valueOf(0.50),
                now())));

    assertThat(getEmitters()).isEmpty();
  }

  private void addMockEmitter(SseEmitter emitter) throws Exception {
    getEmitters().add(emitter);
  }

  @SuppressWarnings("unchecked")
  private CopyOnWriteArrayList<SseEmitter> getEmitters() throws Exception {
    Field field = BroadcastSseService.class.getDeclaredField("emitters");
    field.setAccessible(true);
    return (CopyOnWriteArrayList<SseEmitter>) field.get(service);
  }
}
