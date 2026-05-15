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
public interface TradeExecutionHistoryEntityMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "executedAt", expression = "java(java.time.LocalDateTime.now())")
  TradeHistory map(Trade trade, TradeExecutionResult result, Portfolio portfolio);

  default String mapAction(Trade trade) {
    return trade.action().getValue();
  }

  default BigDecimal mapPrice(Trade trade) {
    return BigDecimal.valueOf(trade.price());
  }

  default BigDecimal mapTotalCost(TradeExecutionResult result) {
    return BigDecimal.valueOf(result.totalCost());
  }

  default BigDecimal mapCashBalanceAfter(TradeExecutionResult result) {
    return BigDecimal.valueOf(result.newCashBalance());
  }

  default Long mapQuantity(Trade trade) {
    return (long) trade.quantity();
  }
}
