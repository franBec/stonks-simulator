package dev.pollito.stonks_java.broadcast.application.service;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.broadcast.config.BroadcastProperties;
import dev.pollito.stonks_java.broadcast.domain.PaperTapeEntry;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// Unit test (not E2E) because SSE is push-based and server-initiated.
// Low coverage on BroadcastSseService.java is acceptable — broadcasting, heartbeat, and
// dead-emitter removal tests were removed to avoid field reflection.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BroadcastSseServiceTest {

  private static final long SSE_TIMEOUT_MS = 300_000L;
  private static final String PAPER_TAPE_FORMAT = "TRADE #%04d | %s %d %s @ $%.2f | TOTAL: $%.2f";

  @Mock private TradePortIn tradePortIn;
  @Mock private BroadcastProperties broadcastProperties;
  @InjectMocks private BroadcastSseService service;

  @BeforeEach
  void setUp() {
    when(broadcastProperties.getSseTimeoutMs()).thenReturn(SSE_TIMEOUT_MS);
    when(broadcastProperties.getPaperTapeEntryFormat()).thenReturn(PAPER_TAPE_FORMAT);
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
    when(tradePortIn.getTradeHistory(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    assertThat(service.getPaperTape(PageRequest.of(0, 10))).isEmpty();
  }
}
