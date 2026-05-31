package dev.pollito.stonks_java.chaosevent.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChaoticEventType implements ValuedEnum<String> {
  HYPE_WAVE("HYPE_WAVE"),
  MEME_STORM("MEME_STORM"),
  DUMP("DUMP"),
  WHALE_ALERT("WHALE_ALERT"),
  RUG_PULL("RUG_PULL"),
  PUMP_AND_DUMP("PUMP_AND_DUMP"),
  NEWS_FLASH("NEWS_FLASH");

  private final String value;
}
