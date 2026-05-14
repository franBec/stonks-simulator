package dev.pollito.stonks_java.trade.adapter.out.cobol.mapper;

import static dev.pollito.stonks_java.util.enums.EnumUtils.fromValue;

import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolPortfolioMgrResult;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PortfolioMgrCobolMapper {

  TradeExecutionResult map(CobolPortfolioMgrResult result);

  default ValidationStatus map(String status) {
    return status == null ? null : fromValue(ValidationStatus.class, status);
  }
}
