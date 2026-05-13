package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;

public interface TradePortOut {
  TradeValidation validateTrade(Trade trade);
}
