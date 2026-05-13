package dev.pollito.stonks_java.portfolio.adapter.in.rest.mapper;

import dev.pollito.stonks_java.generated.model.Portfolio;
import dev.pollito.stonks_java.generated.model.Position;
import dev.pollito.stonks_java.portfolio.domain.PortfolioSummary;
import dev.pollito.stonks_java.portfolio.domain.PositionSummary;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PortfolioRestMapper {

  Portfolio map(PortfolioSummary summary);

  Position map(PositionSummary position);
}
