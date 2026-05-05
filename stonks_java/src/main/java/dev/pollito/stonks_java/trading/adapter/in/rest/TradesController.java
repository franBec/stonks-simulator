package dev.pollito.stonks_java.trading.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;

import dev.pollito.stonks_java.generated.api.TradesApi;
import dev.pollito.stonks_java.generated.model.TradeValidationRequest;
import dev.pollito.stonks_java.generated.model.TradeValidationResponse;
import dev.pollito.stonks_java.trading.adapter.in.rest.mapper.TradeRestMapper;
import dev.pollito.stonks_java.trading.application.port.in.ValidateTradeUseCase;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TradesController implements TradesApi {
  private final ValidateTradeUseCase validateTradeUseCase;
  private final TradeRestMapper tradeRestMapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<TradeValidationResponse> validateTrade(
      TradeValidationRequest tradeValidationRequest) {
    Trade trade = tradeRestMapper.map(tradeValidationRequest);
    TradeValidation tradeValidation = validateTradeUseCase.validateTrade(trade);

    TradeValidationResponse response =
        new TradeValidationResponse()
            .instance(request.getRequestURI())
            .status(200)
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(tradeRestMapper.map(tradeValidation));

    return ResponseEntity.ok(response);
  }
}
