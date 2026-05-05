package dev.pollito.stonks_java.trading.adapter.out.cobol.mapper;

import dev.pollito.stonks_java.trading.adapter.out.cobol.dto.CobolTradeValidationRequest;
import dev.pollito.stonks_java.trading.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeAction;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import dev.pollito.stonks_java.trading.domain.ValidationStatus;
import dev.pollito.stonks_java.util.enums.EnumUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TradeCobolMapper {

  CobolTradeValidationRequest map(Trade trade);

  TradeValidation map(CobolTradeValidationResult result);

  default String map(TradeAction action) {
    return action == null ? null : action.getValue();
  }

  default ValidationStatus map(String status) {
    return status == null ? null : EnumUtils.fromValue(ValidationStatus.class, status);
  }
}
