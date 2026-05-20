package dev.pollito.stonks_java.trade.adapter.out.cobol;

import static dev.pollito.stonks_java.trade.domain.TradeAction.BUY;
import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolTradeValidationRequest;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolTradeValidationResult;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.TradeValidatorCobolMapper;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.TradeValidatorCobolMapperImpl;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

// Unit test (not E2E) because TradeValidatorCobolAdapter is
// @ConditionalOnProperty(prefix = "stonks.adapters", name = "cobol", havingValue = "real")
// — never loaded under the default (H2 + stubs) profile used by E2E tests. Verifies correct
// COBOL program name, input/output DTOs, and mapper integration in isolation.
@ExtendWith(MockitoExtension.class)
class TradeValidatorCobolAdapterTest {

  private static final String PROGRAM = "trade-validator";
  @Mock private CobolAppPortOut cobolApp;
  @Spy private TradeValidatorCobolMapper mapper = new TradeValidatorCobolMapperImpl();
  @InjectMocks private TradeValidatorCobolAdapter adapter;

  @Test
  void validate() {
    Trade trade = new Trade(BUY, "GMEE", 10, 45.0, 10000.0);
    CobolTradeValidationResult result =
        new CobolTradeValidationResult("ACCEPTED", null, "Valid", 450.0, 9550.0);

    when(cobolApp.execute(
            PROGRAM,
            new CobolTradeValidationRequest("BUY", "GMEE", 10, 45.0, 10000.0),
            CobolTradeValidationResult.class))
        .thenReturn(result);

    assertEquals(
        new TradeValidation(ACCEPTED, null, "Valid", 450.0, 9550.0), adapter.validateTrade(trade));
    verify(mapper).map(trade);
    verify(mapper).map(result);
    verify(cobolApp)
        .execute(
            PROGRAM,
            new CobolTradeValidationRequest("BUY", "GMEE", 10, 45.0, 10000.0),
            CobolTradeValidationResult.class);
  }
}
