package dev.pollito.stonks_java.trade.adapter.out.cobol;

import dev.pollito.stonks_java.trade.application.port.out.TradeExecutorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"cobol", "production"})
@RequiredArgsConstructor
public class PortfolioMgrCobolAdapter implements TradeExecutorPortOutCobol {

  @Override
  public TradeExecutionResult executeTrade(Trade trade) {
    throw new UnsupportedOperationException();
  }
}
