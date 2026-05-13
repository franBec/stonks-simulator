package dev.pollito.stonks_java.trading.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.application.port.out.CobolPortOut;
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

  private final CobolPortOut cobolPortOut;
  private final TradeCobolMapper mapper;

  @Override
  public TradeValidation validate(Trade trade) {
    return mapper.map(
        cobolPortOut.execute(PROGRAM_NAME, mapper.map(trade), CobolTradeValidationResult.class));
  }
}
