package dev.pollito.stonks_java.portfolio.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.generated.api.PortfolioApi;
import dev.pollito.stonks_java.generated.model.PortfolioResponse;
import dev.pollito.stonks_java.portfolio.adapter.in.rest.mapper.PortfolioRestMapper;
import dev.pollito.stonks_java.portfolio.application.port.in.PortfolioPortIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortfolioController implements PortfolioApi {
  private final PortfolioPortIn portfolioPortIn;
  private final PortfolioRestMapper mapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<PortfolioResponse> getPortfolio() {
    return ResponseEntity.ok(
        new PortfolioResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.map(portfolioPortIn.getPortfolio())));
  }
}
