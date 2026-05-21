package dev.pollito.stonks_java.trade.adapter.out.jpa.mapper;

import dev.pollito.stonks_java.generated.entity.Portfolio;
import dev.pollito.stonks_java.generated.entity.TradeHistory;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TradeExecutionEntityMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "action", expression = "java(trade.action().getValue())")
  @Mapping(target = "symbol", source = "trade.symbol")
  @Mapping(target = "quantity", expression = "java((long) trade.quantity())")
  @Mapping(
      target = "price",
      expression = "java(java.math.BigDecimal.valueOf(result.totalCost() / trade.quantity()))")
  @Mapping(target = "totalCost", source = "result.totalCost")
  @Mapping(target = "cashBalanceAfter", source = "result.newCashBalance")
  @Mapping(target = "executedAt", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "portfolio", source = "portfolio")
  TradeHistory map(Trade trade, TradeExecutionResult result, Portfolio portfolio);

  default BigDecimal map(double value) {
    return BigDecimal.valueOf(value);
  }
}
