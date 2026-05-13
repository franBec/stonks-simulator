package dev.pollito.stonks_java.trade.application.service;

import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.application.port.out.TradePortOut;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService implements TradePortIn {
  private final TradePortOut tradePortOut;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradePortOut.validateTrade(trade);
  }
}
