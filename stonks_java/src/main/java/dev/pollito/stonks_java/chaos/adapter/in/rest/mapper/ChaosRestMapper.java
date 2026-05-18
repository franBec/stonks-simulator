package dev.pollito.stonks_java.chaos.adapter.in.rest.mapper;

import dev.pollito.stonks_java.generated.model.ChaosEventSeverity;
import dev.pollito.stonks_java.generated.model.ChaosLevel;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChaosRestMapper {

  @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
  @Mapping(
      target = "type",
      expression = "java(dev.pollito.stonks_java.generated.model.ChaosEventType.HYPE_WAVE)")
  @Mapping(target = "severity", source = "impactPercent", qualifiedByName = "mapSeverity")
  @Mapping(target = "title", source = "headline")
  @Mapping(target = "description", source = "explanation")
  @Mapping(target = "targetSymbol", source = "symbol")
  @Mapping(target = "priceEffect", source = "impactPercent")
  @Mapping(target = "startedAt", source = "occurredAt")
  @Mapping(target = "expiresAt", expression = "java(source.occurredAt().plusHours(1))")
  dev.pollito.stonks_java.generated.model.ChaosEvent map(
      dev.pollito.stonks_java.chaos.domain.ChaosEvent source);

  @Named("mapSeverity")
  default ChaosEventSeverity mapSeverity(BigDecimal impactPercent) {
    if (impactPercent != null) {
      double abs = impactPercent.abs().doubleValue();
      if (abs > 20.0) return ChaosEventSeverity.CRITICAL;
      if (abs > 10.0) return ChaosEventSeverity.HIGH;
      if (abs > 5.0) return ChaosEventSeverity.MEDIUM;
    }
    return ChaosEventSeverity.LOW;
  }

  default ChaosLevel mapLevel(dev.pollito.stonks_java.chaos.domain.ChaosLevel source) {
    return ChaosLevel.fromValue(source.name());
  }
}
