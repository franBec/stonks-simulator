package dev.pollito.stonks_java.broadcast.adapter.in.rest.mapper;

import dev.pollito.stonks_java.broadcast.domain.PaperTapeEntry;
import dev.pollito.stonks_java.generated.model.PaperTapeResponseData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BroadcastRestMapper {

  dev.pollito.stonks_java.generated.model.PaperTapeEntry toPaperTapeEntryDto(PaperTapeEntry entry);

  PaperTapeResponseData toPaperTapeResponseData(
      org.springframework.data.domain.Page<PaperTapeEntry> page);
}
