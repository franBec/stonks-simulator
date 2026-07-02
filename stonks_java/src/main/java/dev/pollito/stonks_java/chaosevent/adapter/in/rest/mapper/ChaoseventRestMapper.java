package dev.pollito.stonks_java.chaosevent.adapter.in.rest.mapper;

import dev.pollito.stonks_java.generated.model.ChaoticEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChaoseventRestMapper {

  @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
  @Mapping(target = "title", source = "headline")
  @Mapping(target = "description", source = "explanation")
  @Mapping(target = "targetSymbol", source = "symbol")
  @Mapping(
      target = "priceEffect",
      expression =
          "java(domainEvent.impactPercent().divide(new java.math.BigDecimal(\"100\"), 10, java.math.RoundingMode.HALF_UP).add(java.math.BigDecimal.ONE).doubleValue())")
  @Mapping(target = "startedAt", source = "occurredAt")
  @Mapping(target = "expiresAt", expression = "java(domainEvent.occurredAt().plusHours(1))")
  ChaoticEvent map(dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent domainEvent);
}
