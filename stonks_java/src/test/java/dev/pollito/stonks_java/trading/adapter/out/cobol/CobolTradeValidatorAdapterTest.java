package dev.pollito.stonks_java.trading.adapter.out.cobol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.cobol.CobolProgramExecutor;
import dev.pollito.stonks_java.trading.adapter.out.cobol.dto.CobolTradeValidationRequest;
import dev.pollito.stonks_java.trading.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trading.adapter.out.cobol.mapper.TradeCobolMapper;
import dev.pollito.stonks_java.trading.adapter.out.cobol.mapper.TradeCobolMapperImpl;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeAction;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import dev.pollito.stonks_java.trading.domain.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CobolTradeValidatorAdapterTest {

  @Mock private CobolProgramExecutor cobolProgramExecutor;
  @Spy private TradeCobolMapper tradeCobolMapper = new TradeCobolMapperImpl();
  @InjectMocks private CobolTradeValidatorAdapter adapter;

  @Test
  void validate() {
    Trade trade = new Trade(TradeAction.BUY, "GMEE", 10, 45.0, 10000.0);
    CobolTradeValidationResult result =
        new CobolTradeValidationResult("ACCEPTED", null, "Valid", 450.0, 9550.0);

    when(cobolProgramExecutor.execute(
            "trade-validator",
            new CobolTradeValidationRequest("BUY", "GMEE", 10, 45.0, 10000.0),
            CobolTradeValidationResult.class))
        .thenReturn(result);

    TradeValidation actual = adapter.validate(trade);

    assertEquals(
        new TradeValidation(ValidationStatus.ACCEPTED, null, "Valid", 450.0, 9550.0), actual);
    verify(tradeCobolMapper).map(trade);
    verify(tradeCobolMapper).map(result);
    verify(cobolProgramExecutor)
        .execute(
            "trade-validator",
            new CobolTradeValidationRequest("BUY", "GMEE", 10, 45.0, 10000.0),
            CobolTradeValidationResult.class);
  }
}
