package dev.pollito.stonks_java.stocks.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.generated.api.MarketApi;
import dev.pollito.stonks_java.generated.model.MarketStocksResponse;
import dev.pollito.stonks_java.stocks.adapter.in.rest.mapper.StockRestMapper;
import dev.pollito.stonks_java.stocks.application.port.in.GetStocksUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StocksController implements MarketApi {
  private final GetStocksUseCase useCase;
  private final StockRestMapper mapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<MarketStocksResponse> getStocks() {
    return ResponseEntity.ok(
        new MarketStocksResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(useCase.getStocks().stream().map(mapper::map).toList()));
  }
}
