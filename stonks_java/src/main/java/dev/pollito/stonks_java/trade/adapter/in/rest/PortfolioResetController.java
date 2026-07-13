package dev.pollito.stonks_java.trade.adapter.in.rest;

import dev.pollito.stonks_java.generated.api.GameApi;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortfolioResetController implements GameApi {
  private final TradePortIn tradePortIn;

  @Override
  public ResponseEntity<Void> resetGame() {
    tradePortIn.resetPortfolio();
    return ResponseEntity.noContent().build();
  }
}
