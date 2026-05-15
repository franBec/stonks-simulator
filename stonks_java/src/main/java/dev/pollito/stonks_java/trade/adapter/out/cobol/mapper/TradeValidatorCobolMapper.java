package dev.pollito.stonks_java.trade.adapter.out.cobol.mapper;

import static dev.pollito.stonks_java.util.enums.EnumUtils.fromValue;

import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolTradeValidationRequest;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeAction;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TradeValidatorCobolMapper {

  CobolTradeValidationRequest map(Trade trade);

  TradeValidation map(CobolTradeValidationResult result);

  default String map(TradeAction action) {
    return action == null ? null : action.getValue();
  }

  default ValidationStatus map(String status) {
    return status == null ? null : fromValue(ValidationStatus.class, status);
  }
}
