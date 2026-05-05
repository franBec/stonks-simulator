package dev.pollito.stonks_java.trading.application.service;

import dev.pollito.stonks_java.trading.application.port.in.ValidateTradeUseCase;
import dev.pollito.stonks_java.trading.application.port.out.TradeValidatorPort;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements ValidateTradeUseCase {
  private final TradeValidatorPort tradeValidatorPort;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradeValidatorPort.validate(trade);
  }
}
