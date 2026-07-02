package dev.pollito.stonks_java.chaosevent.adapter.out.jpa.mapper;

import static dev.pollito.stonks_java.util.strings.Truncate.value;
import static java.lang.String.join;
import static java.time.ZoneOffset.UTC;

import dev.pollito.stonks_java.chaosevent.adapter.out.jpa.ChaoseventIncidentLogEntity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChaoticIncidentJpaMapper {

  @Mapping(target = "symbol", source = "targetSymbol")
  @Mapping(
      target = "affectedSymbols",
      expression = "java(parseAffectedSymbols(entity.getAffectedSymbols()))")
  @Mapping(target = "type", expression = "java(parseEventType(entity.getEventType()))")
  @Mapping(target = "severity", expression = "java(parseEventSeverity(entity.getEventSeverity()))")
  ChaoticEvent toDomain(ChaoseventIncidentLogEntity entity);

  @Mapping(
      target = "headline",
      expression =
          "java(dev.pollito.stonks_java.util.strings.Truncate.value(event.headline(), 500))")
  @Mapping(target = "targetSymbol", source = "symbol")
  @Mapping(
      target = "explanation",
      expression =
          "java(dev.pollito.stonks_java.util.strings.Truncate.value(event.explanation(), 2000))")
  @Mapping(
      target = "affectedSymbols",
      expression = "java(affectedSymbolsToString(event.affectedSymbols()))")
  @Mapping(
      target = "sourceHeadline",
      expression =
          "java(dev.pollito.stonks_java.util.strings.Truncate.value(event.sourceHeadline(), 500))")
  @Mapping(target = "eventType", expression = "java(eventTypeToString(event.type()))")
  @Mapping(target = "eventSeverity", expression = "java(eventSeverityToString(event.severity()))")
  @Mapping(target = "id", ignore = true)
  ChaoseventIncidentLogEntity toEntity(ChaoticEvent event);

  default OffsetDateTime map(LocalDateTime source) {
    return source == null ? null : source.atOffset(UTC);
  }

  default LocalDateTime map(OffsetDateTime source) {
    return source == null ? null : source.withOffsetSameInstant(UTC).toLocalDateTime();
  }

  default List<String> parseAffectedSymbols(String value) {
    return value == null || value.isEmpty() ? List.of() : List.of(value.split(","));
  }

  default String affectedSymbolsToString(List<String> affectedSymbols) {
    return affectedSymbols != null && !affectedSymbols.isEmpty()
        ? value(join(",", affectedSymbols), 2000)
        : null;
  }

  default ChaoticEventType parseEventType(String value) {
    return value == null ? null : ChaoticEventType.valueOf(value);
  }

  default String eventTypeToString(ChaoticEventType value) {
    return value == null ? null : value.name();
  }

  default ChaoticEventSeverity parseEventSeverity(String value) {
    return value == null ? null : ChaoticEventSeverity.valueOf(value);
  }

  default String eventSeverityToString(ChaoticEventSeverity value) {
    return value == null ? null : value.name();
  }
}
