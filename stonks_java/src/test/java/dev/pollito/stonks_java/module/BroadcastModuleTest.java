package dev.pollito.stonks_java.module;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventTriggered;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import dev.pollito.stonks_java.stock.domain.Trend;
import dev.pollito.stonks_java.trade.domain.TradeAction;
import dev.pollito.stonks_java.trade.domain.TradeExecutedEvent;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BroadcastModuleTest {

  @LocalServerPort private int port;

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Autowired private ObjectMapper objectMapper;

  private HttpClient client;
  private final List<BufferedReader> readers = new ArrayList<>();

  @BeforeEach
  void setUp() {
    client = HttpClient.newBuilder().build();
  }

  @AfterEach
  void tearDown() throws Exception {
    readers.forEach(
        r -> {
          try {
            r.close();
          } catch (Exception ignored) {
          }
        });
    readers.clear();
    client.close();
  }

  private HttpRequest sseRequest() {
    return HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + port + "/api/stream"))
        .header("Accept", "text/event-stream")
        .GET()
        .build();
  }

  private BufferedReader connect() throws Exception {
    var response = client.send(sseRequest(), HttpResponse.BodyHandlers.ofInputStream());
    var reader = new BufferedReader(new InputStreamReader(response.body(), UTF_8));
    readers.add(reader);
    assertThat(reader.readLine()).isEqualTo("event:connected");
    assertThat(reader.readLine()).startsWith("data:");
    reader.readLine();
    assertThat(reader.readLine()).isEqualTo("event:SPEED_CONFIG");
    assertThat(reader.readLine()).startsWith("data:");
    reader.readLine();
    return reader;
  }

  private String readEventName(BufferedReader reader) throws Exception {
    String line;
    do {
      line = reader.readLine();
    } while (line != null && (line.isEmpty() || line.charAt(0) == ':'));
    return line;
  }

  private String readDataLine(BufferedReader reader) throws Exception {
    String line = reader.readLine();
    assertThat(line).startsWith("data:");
    return line.substring("data:".length());
  }

  private static StockPrice sampleStockPrice() {
    return new StockPrice(
        "GMEE",
        "GME Engine",
        BigDecimal.valueOf(150.25),
        BigDecimal.valueOf(145.00),
        BigDecimal.valueOf(5.25),
        BigDecimal.valueOf(3.62),
        Trend.MOON,
        BigDecimal.valueOf(0.25),
        OffsetDateTime.now());
  }

  @Test
  void connectionEvent() throws Exception {
    var reader = connect();
    assertThat(reader.readLine()).isEmpty();
  }

  @Test
  void priceTickBroadcast() throws Exception {
    var reader = connect();
    eventPublisher.publishEvent(new StockPriceUpdatedEvent(sampleStockPrice()));

    assertThat(readEventName(reader)).isEqualTo("event:PRICE_TICK");
    var data = objectMapper.readValue(readDataLine(reader), Map.class);
    reader.readLine();
    assertThat(data.get("symbol")).isEqualTo("GMEE");
    assertThat(data.get("price")).isEqualTo(150.25);
  }

  @Test
  void tradeExecutedBroadcast() throws Exception {
    var reader = connect();
    var result = new TradeExecutionResult(ValidationStatus.ACCEPTED, null, "OK", 9500.0, 10, 500.0);
    eventPublisher.publishEvent(new TradeExecutedEvent(TradeAction.BUY, result, "GMEE", 10));

    assertThat(readEventName(reader)).isEqualTo("event:TRADE_EXECUTED");
    var data = objectMapper.readValue(readDataLine(reader), Map.class);
    reader.readLine();
    assertThat(data.get("symbol")).isEqualTo("GMEE");
    assertThat(data.get("quantity")).isEqualTo(10);
    assertThat(data.get("paperTape")).isEqualTo("TRADE | BUY 10 GMEE @ $50.00 | TOTAL: $500.00");
  }

  @Test
  void chaosEventBroadcast() throws Exception {
    var reader = connect();
    var chaoticEvent =
        new ChaoticEvent(
            "Fed raises rates",
            "GMEE",
            BigDecimal.valueOf(-5.0),
            "Rate hike explanation",
            List.of("GMEE", "COBL"),
            "Source headline",
            OffsetDateTime.now(),
            ChaoticEventType.NEWS_FLASH,
            ChaoticEventSeverity.HIGH);
    eventPublisher.publishEvent(new ChaoticEventTriggered(chaoticEvent));

    assertThat(readEventName(reader)).isEqualTo("event:CHAOS_EVENT");
    var data = objectMapper.readValue(readDataLine(reader), Map.class);
    reader.readLine();
    assertThat(data.get("headline")).isEqualTo("Fed raises rates");
    assertThat(data.get("symbol")).isEqualTo("GMEE");
    assertThat(data.get("impact")).isEqualTo(-5.0);
    assertThat(data.get("explanation")).isEqualTo("Rate hike explanation");
  }

  @Test
  void multipleClients() throws Exception {
    var reader1 = connect();
    var reader2 = connect();

    eventPublisher.publishEvent(new StockPriceUpdatedEvent(sampleStockPrice()));

    assertThat(readEventName(reader1)).isEqualTo("event:PRICE_TICK");
    readDataLine(reader1);
    reader1.readLine();

    assertThat(readEventName(reader2)).isEqualTo("event:PRICE_TICK");
    readDataLine(reader2);
    reader2.readLine();
  }

  @Test
  void deadClientCleanup() throws Exception {
    var response1 = client.send(sseRequest(), HttpResponse.BodyHandlers.ofInputStream());
    var reader1 = new BufferedReader(new InputStreamReader(response1.body(), UTF_8));
    assertThat(reader1.readLine()).isEqualTo("event:connected");
    assertThat(reader1.readLine()).startsWith("data:");

    var reader2 = connect();

    response1.body().close();
    Thread.sleep(500);

    eventPublisher.publishEvent(new StockPriceUpdatedEvent(sampleStockPrice()));

    assertThat(readEventName(reader2)).isEqualTo("event:PRICE_TICK");
  }
}
