package dev.pollito.stonks_java.trade.application.port.in;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;

public interface TradePortIn {
  TradeValidation validateTrade(Trade trade);
}
