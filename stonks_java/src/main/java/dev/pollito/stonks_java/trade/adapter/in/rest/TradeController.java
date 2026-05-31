package dev.pollito.stonks_java.trade.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.generated.api.TradesApi;
import dev.pollito.stonks_java.generated.model.TradeExecutionRequest;
import dev.pollito.stonks_java.generated.model.TradeExecutionResponse;
import dev.pollito.stonks_java.generated.model.TradeHistoryResponse;
import dev.pollito.stonks_java.trade.adapter.in.rest.mapper.TradeRestMapper;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TradeController implements TradesApi {
  private final TradePortIn tradePortIn;
  private final TradeRestMapper mapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<TradeExecutionResponse> executeTrade(
      TradeExecutionRequest tradeExecutionRequest) {
    return ResponseEntity.ok(
        new TradeExecutionResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.map(tradePortIn.executeTrade(mapper.map(tradeExecutionRequest)))));
  }

  @Override
  public ResponseEntity<TradeHistoryResponse> getTradeHistory(Pageable pageable) {
    return ResponseEntity.ok(
        new TradeHistoryResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.map(tradePortIn.getTradeHistory(pageable))));
  }
}
