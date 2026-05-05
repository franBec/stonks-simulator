package dev.pollito.stonks_java.trading.adapter.out.cobol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
  void validateDelegatesToMapperAndExecutor() {
    Trade trade = new Trade(TradeAction.BUY, "GMEE", 10, 45.0, 10000.0);
    CobolTradeValidationResult cobolResult =
        new CobolTradeValidationResult("ACCEPTED", null, "TRADE VALIDATED", 450.0, 9550.0);

    when(cobolProgramExecutor.execute(
            eq("trade-validator"),
            any(CobolTradeValidationRequest.class),
            eq(CobolTradeValidationResult.class)))
        .thenReturn(cobolResult);

    TradeValidation result = adapter.validate(trade);

    assertNotNull(result);
    assertEquals(ValidationStatus.ACCEPTED, result.status());
    assertEquals(450.0, result.totalCost());
    assertEquals(9550.0, result.remainingCash());
    assertEquals(null, result.errorCode());

    verify(tradeCobolMapper).map(trade);
    verify(cobolProgramExecutor)
        .execute(
            eq("trade-validator"),
            any(CobolTradeValidationRequest.class),
            eq(CobolTradeValidationResult.class));
    verify(tradeCobolMapper).map(cobolResult);
  }

  @Test
  void validateMapsRejectedResult() {
    Trade trade = new Trade(TradeAction.BUY, "GMEE", 1000, 45.0, 100.0);
    CobolTradeValidationResult cobolResult =
        new CobolTradeValidationResult("REJECTED", "S222", "JOB ABEND", 0.0, 0.0);

    when(cobolProgramExecutor.execute(
            eq("trade-validator"),
            any(CobolTradeValidationRequest.class),
            eq(CobolTradeValidationResult.class)))
        .thenReturn(cobolResult);

    TradeValidation result = adapter.validate(trade);

    assertNotNull(result);
    assertEquals(ValidationStatus.REJECTED, result.status());
    assertEquals("S222", result.errorCode());
    assertEquals("JOB ABEND", result.message());
    assertEquals(0.0, result.totalCost());
    assertEquals(0.0, result.remainingCash());

    verify(tradeCobolMapper).map(trade);
    verify(cobolProgramExecutor)
        .execute(
            eq("trade-validator"),
            any(CobolTradeValidationRequest.class),
            eq(CobolTradeValidationResult.class));
    verify(tradeCobolMapper).map(cobolResult);
  }
}
