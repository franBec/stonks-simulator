package dev.pollito.stonks_java.trade.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.TradeValidatorCobolMapper;
import dev.pollito.stonks_java.trade.application.port.out.TradeValidationPortOut;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "cobol", havingValue = "real")
@RequiredArgsConstructor
public class TradeValidatorCobolAdapter implements TradeValidationPortOut {
  private static final String PROGRAM_NAME = "trade-validator";

  private final CobolAppPortOut cobolApp;
  private final TradeValidatorCobolMapper mapper;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return mapper.map(
        cobolApp.execute(PROGRAM_NAME, mapper.map(trade), CobolTradeValidationResult.class));
  }
}
