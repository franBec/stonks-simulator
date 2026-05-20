package dev.pollito.stonks_java.trade.adapter.out.cobol;

import static dev.pollito.stonks_java.trade.domain.TradeAction.BUY;
import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolPortfolioMgrRequest;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolPortfolioMgrResult;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.TradePortfolioMgrCobolMapper;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.TradePortfolioMgrCobolMapperImpl;
import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

// Unit test (not E2E) because TradePortfolioMgrCobolAdapter is
// @ConditionalOnProperty(prefix = "stonks.adapters", name = "cobol", havingValue = "real")
// — never loaded under the default (H2 + stubs) profile used by E2E tests. Verifies correct
// COBOL program name, input/output DTOs, and mapper integration in isolation.
@ExtendWith(MockitoExtension.class)
class TradePortfolioMgrCobolAdapterTest {

  private static final String PROGRAM = "portfolio-mgr";
  @Mock private CobolAppPortOut cobolApp;
  @Spy private TradePortfolioMgrCobolMapper mapper = new TradePortfolioMgrCobolMapperImpl();
  @InjectMocks private TradePortfolioMgrCobolAdapter adapter;

  @Test
  void executeTrade() {
    TradeExecutionInput input = new TradeExecutionInput(BUY, "GMEE", 10, 45.0, 10000.0, 0);
    CobolPortfolioMgrResult result =
        new CobolPortfolioMgrResult("ACCEPTED", null, "Valid", 9550.0, 10, 450.0);

    when(cobolApp.execute(
            PROGRAM,
            new CobolPortfolioMgrRequest("BUY", "GMEE", 10, 45.0, 10000.0, 0),
            CobolPortfolioMgrResult.class))
        .thenReturn(result);

    assertEquals(
        new TradeExecutionResult(ACCEPTED, null, "Valid", 9550.0, 10, 450.0),
        adapter.executeTrade(input));
    verify(mapper).map(input);
    verify(mapper).map(result);
    verify(cobolApp)
        .execute(
            PROGRAM,
            new CobolPortfolioMgrRequest("BUY", "GMEE", 10, 45.0, 10000.0, 0),
            CobolPortfolioMgrResult.class);
  }
}
