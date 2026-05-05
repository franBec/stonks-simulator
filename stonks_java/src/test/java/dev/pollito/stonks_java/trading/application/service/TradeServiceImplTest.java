package dev.pollito.stonks_java.trading.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.trading.application.port.out.TradeValidatorPort;
import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeAction;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import dev.pollito.stonks_java.trading.domain.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradeServiceImplTest {
  @Mock private TradeValidatorPort tradeValidatorPort;
  @InjectMocks private TradeServiceImpl tradeService;

  @Test
  void mapsAcceptedResult() {
    when(tradeValidatorPort.validate(any()))
        .thenReturn(
            new TradeValidation(ValidationStatus.ACCEPTED, null, "TRADE VALIDATED", 450.0, 9550.0));

    Trade request = new Trade(TradeAction.BUY, "GMEE", 10, 45.0, 10000.0);
    TradeValidation result = tradeService.validateTrade(request);

    assertEquals(ValidationStatus.ACCEPTED, result.status());
    assertEquals(450.0, result.totalCost());
    assertEquals(9550.0, result.remainingCash());
    assertEquals(null, result.errorCode());
  }

  @Test
  void mapsRejectedResult() {
    when(tradeValidatorPort.validate(any()))
        .thenReturn(new TradeValidation(ValidationStatus.REJECTED, "S222", "JOB ABEND", 0.0, 0.0));

    Trade request = new Trade(TradeAction.BUY, "GMEE", 1000, 45.0, 100.0);
    TradeValidation result = tradeService.validateTrade(request);

    assertEquals(ValidationStatus.REJECTED, result.status());
    assertEquals("S222", result.errorCode());
  }
}
