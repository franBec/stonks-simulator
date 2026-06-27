package dev.pollito.stonks_java.trade.adapter.out.jpa.mapper;

import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeHistoryEntity;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TradeHistoryJpaMapper {

  TradeHistoryItem map(TradeHistoryEntity entity);

  default OffsetDateTime map(LocalDateTime localDateTime) {
    return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
  }
}
