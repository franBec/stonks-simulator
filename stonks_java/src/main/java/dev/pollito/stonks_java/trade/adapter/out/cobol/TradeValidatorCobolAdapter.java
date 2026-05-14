package dev.pollito.stonks_java.trade.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.TradeCobolMapper;
import dev.pollito.stonks_java.trade.application.port.out.TradePortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"cobol", "production"})
@RequiredArgsConstructor
public class TradeValidatorCobolAdapter implements TradePortOutCobol {
  private static final String PROGRAM_NAME = "trade-validator";

  private final CobolAppPortOut cobolApp;
  private final TradeCobolMapper mapper;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return mapper.map(
        cobolApp.execute(PROGRAM_NAME, mapper.map(trade), CobolTradeValidationResult.class));
  }

  @Override
  public TradeExecutionResult executeTrade(Trade trade) {
    throw new UnsupportedOperationException();
  }
}
