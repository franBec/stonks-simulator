package dev.pollito.stonks_java.trading.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.generated.api.TradesApi;
import dev.pollito.stonks_java.generated.model.TradeValidationRequest;
import dev.pollito.stonks_java.generated.model.TradeValidationResponse;
import dev.pollito.stonks_java.trading.adapter.in.rest.mapper.TradeRestMapper;
import dev.pollito.stonks_java.trading.application.port.in.ValidateTradeUseCase;
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
    return ResponseEntity.ok(
        new TradeValidationResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(
                tradeRestMapper.map(
                    validateTradeUseCase.validateTrade(
                        tradeRestMapper.map(tradeValidationRequest)))));
  }
}
