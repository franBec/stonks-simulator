package dev.pollito.stonks_java.trade.adapter.out.jpa.mapper;

import dev.pollito.stonks_java.generated.entity.Portfolio;
import dev.pollito.stonks_java.generated.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TradeExecutionPositionEntityMapper {

  @Mapping(target = "id", ignore = true)
  Position map(Portfolio portfolio, String symbol, Long quantity);
}
