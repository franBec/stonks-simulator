package dev.pollito.stonks_java.trade.adapter.out.cobol;

import static dev.pollito.stonks_java.trade.domain.TradeAction.BUY;
import static dev.pollito.stonks_java.trade.domain.TradeAction.SELL;
import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.trade.application.port.out.TradeExecutionPortOut;
import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    prefix = "stonks.adapters",
    name = "cobol",
    havingValue = "stub",
    matchIfMissing = true)
@Slf4j
public class TradePortfolioMgrCobolAdapterStub implements TradeExecutionPortOut {

  private static final Set<String> VALID_SYMBOLS =
      Set.of("COBL", "GMEE", "DOGE", "TEND", "FOMO", "PAPR", "YOLO", "MEME", "BUGS", "JAVA");

  @Override
  public TradeExecutionResult executeTrade(TradeExecutionInput input) {
    log.warn("Using dev stub for TradeExecutionPortOut — no real COBOL engine is running");

    if (input.action() == null) {
      return rejected("S225", "JOB ABEND S225 - INVALID ACTION", input);
    }

    if (!VALID_SYMBOLS.contains(input.symbol())) {
      return rejected("S001", "JOB ABEND S001 - UNKNOWN SYMBOL " + input.symbol(), input);
    }

    if (input.quantity() <= 0) {
      return rejected("S224", "JOB ABEND S224 - INVALID QTY", input);
    }

    if (input.price() <= 0.0) {
      return rejected("S226", "JOB ABEND S226 - INVALID PRICE", input);
    }

    double totalCost = input.quantity() * input.price();
    double newCashBalance;
    int newQuantity;

    if (input.action() == BUY) {
      if (input.cashBalance() < totalCost) {
        return rejected("S222", "JOB ABEND S222 - INSUFF FUNDS", input);
      }
      newCashBalance = input.cashBalance() - totalCost;
      newQuantity = input.holdingQty() + input.quantity();
    } else if (input.action() == SELL) {
      if (input.holdingQty() < input.quantity()) {
        return rejected("S223", "JOB ABEND S223 - INSUFF SHARES", input);
      }
      newCashBalance = input.cashBalance() + totalCost;
      newQuantity = input.holdingQty() - input.quantity();
    } else {
      return rejected("S225", "JOB ABEND S225 - INVALID ACTION", input);
    }

    return new TradeExecutionResult(
        ACCEPTED,
        null,
        "TRADE EXECUTED - " + input.action().getValue() + " " + input.symbol(),
        newCashBalance,
        newQuantity,
        totalCost);
  }

  private TradeExecutionResult rejected(
      String errorCode, String message, TradeExecutionInput input) {
    return new TradeExecutionResult(
        ValidationStatus.REJECTED,
        errorCode,
        message,
        input.cashBalance(),
        input.holdingQty(),
        0.0);
  }
}
