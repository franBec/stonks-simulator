package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;

public interface TradeValidationPortOut {
  TradeValidation validateTrade(Trade trade);
}
