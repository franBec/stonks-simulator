package dev.pollito.stonks_java.trade.adapter.out.cobol;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolPortfolioMgrResult;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.PortfolioMgrCobolMapper;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"cobol", "production"})
@RequiredArgsConstructor
public class PortfolioMgrCobolAdapter implements TradeExecutorPortOutCobol {

  private static final String PROGRAM_NAME = "portfolio-mgr";

  private final CobolAppPortOut cobolApp;
  private final PortfolioMgrCobolMapper mapper;

  @Override
  public TradeExecutionResult executeTrade(TradeExecutionInput input) {
    CobolPortfolioMgrResult cobolResult =
        cobolApp.execute(PROGRAM_NAME, mapper.map(input), CobolPortfolioMgrResult.class);
    return mapper.map(cobolResult);
  }
}
