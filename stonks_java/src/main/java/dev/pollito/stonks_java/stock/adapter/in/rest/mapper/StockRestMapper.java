package dev.pollito.stonks_java.stock.adapter.in.rest.mapper;

import dev.pollito.stonks_java.generated.model.StockPrice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockRestMapper {

  StockPrice map(dev.pollito.stonks_java.stock.domain.StockPrice source);
}
