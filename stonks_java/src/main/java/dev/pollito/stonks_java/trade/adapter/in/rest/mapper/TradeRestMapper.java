package dev.pollito.stonks_java.trade.adapter.in.rest.mapper;

import dev.pollito.stonks_java.generated.model.TradeAction;
import dev.pollito.stonks_java.generated.model.TradeValidationRequest;
import dev.pollito.stonks_java.generated.model.TradeValidationResult;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import dev.pollito.stonks_java.util.enums.EnumUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TradeRestMapper {

  Trade map(TradeValidationRequest request);

  TradeValidationResult map(TradeValidation tradeValidation);

  default dev.pollito.stonks_java.trade.domain.TradeAction map(TradeAction action) {
    return action == null
        ? null
        : EnumUtils.fromValue(
            dev.pollito.stonks_java.trade.domain.TradeAction.class, action.getValue());
  }

  default TradeValidationResult.StatusEnum map(ValidationStatus status) {
    return status == null ? null : TradeValidationResult.StatusEnum.fromValue(status.getValue());
  }
}
