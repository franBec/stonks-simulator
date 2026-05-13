package dev.pollito.stonks_java.stock.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.generated.api.StocksApi;
import dev.pollito.stonks_java.generated.model.StocksResponse;
import dev.pollito.stonks_java.stock.adapter.in.rest.mapper.StockRestMapper;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockController implements StocksApi {
  private final StockPortIn stockPortIn;
  private final StockRestMapper mapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<StocksResponse> getStocks() {
    return ResponseEntity.ok(
        new StocksResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(stockPortIn.getStocks().stream().map(mapper::map).toList()));
  }
}
