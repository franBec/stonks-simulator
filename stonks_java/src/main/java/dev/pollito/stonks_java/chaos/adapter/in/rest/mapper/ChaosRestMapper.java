package dev.pollito.stonks_java.chaos.adapter.in.rest.mapper;

import dev.pollito.stonks_java.generated.model.ChaosLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChaosRestMapper {

  @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
  @Mapping(target = "type", source = "source.type")
  @Mapping(target = "severity", source = "source.severity")
  @Mapping(target = "title", source = "headline")
  @Mapping(target = "description", source = "explanation")
  @Mapping(target = "targetSymbol", source = "symbol")
  @Mapping(target = "priceEffect", source = "impactPercent")
  @Mapping(target = "startedAt", source = "occurredAt")
  @Mapping(target = "expiresAt", expression = "java(source.occurredAt().plusHours(1))")
  dev.pollito.stonks_java.generated.model.ChaosEvent map(
      dev.pollito.stonks_java.chaos.domain.ChaosEvent source);

  default ChaosLevel mapLevel(dev.pollito.stonks_java.chaos.domain.ChaosLevel source) {
    return ChaosLevel.fromValue(source.name());
  }
}
