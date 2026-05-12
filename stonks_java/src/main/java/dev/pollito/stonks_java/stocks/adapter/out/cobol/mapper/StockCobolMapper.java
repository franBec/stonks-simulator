package dev.pollito.stonks_java.stocks.adapter.out.cobol.mapper;

import dev.pollito.stonks_java.stocks.adapter.out.cobol.dto.CobolCatalogStock;
import dev.pollito.stonks_java.stocks.domain.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockCobolMapper {

  Stock map(CobolCatalogStock source);
}
