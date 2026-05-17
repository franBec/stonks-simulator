package dev.pollito.stonks_java.broadcast.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.broadcast.config.BroadcastProperties;
import dev.pollito.stonks_java.broadcast.domain.PaperTapeEntry;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import java.time.OffsetDateTime;
import java.util.List;
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
  void setUp() {
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
    var historyItem =
        new TradeHistoryItem(42L, "BUY", "GMEE", 10, 47.85, 478.50, 9521.50, OffsetDateTime.now());

    Page<TradeHistoryItem> historyPage = new PageImpl<>(List.of(historyItem));
    when(tradePortIn.getTradeHistory(any())).thenReturn(historyPage);

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
    Page<TradeHistoryItem> emptyPage = new PageImpl<>(List.of());
    when(tradePortIn.getTradeHistory(any())).thenReturn(emptyPage);

    Page<PaperTapeEntry> result = service.getPaperTape(PageRequest.of(0, 10));

    assertThat(result).isEmpty();
  }
}
