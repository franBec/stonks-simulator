package dev.pollito.stonks_java.trading.application.port.out;

import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeValidation;

public interface TradeValidatorPort {
  TradeValidation validate(Trade trade);
}
