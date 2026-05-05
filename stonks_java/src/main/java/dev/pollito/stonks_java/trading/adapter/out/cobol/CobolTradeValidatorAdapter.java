package dev.pollito.stonks_java.trading.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.CobolProgramExecutor;
import dev.pollito.stonks_java.trading.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trading.adapter.out.cobol.mapper.TradeCobolMapper;
import dev.pollito.stonks_java.trading.application.port.out.TradeValidatorPort;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CobolTradeValidatorAdapter implements TradeValidatorPort {
  private static final String PROGRAM_NAME = "trade-validator";

  private final CobolProgramExecutor cobolProgramExecutor;
  private final TradeCobolMapper tradeCobolMapper;

  @Override
  public TradeValidation validate(Trade trade) {
    return tradeCobolMapper.map(
        cobolProgramExecutor.execute(
            PROGRAM_NAME, tradeCobolMapper.map(trade), CobolTradeValidationResult.class));
  }
}
