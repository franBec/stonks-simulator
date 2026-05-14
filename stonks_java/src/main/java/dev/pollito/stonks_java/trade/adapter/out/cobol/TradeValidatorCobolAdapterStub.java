package dev.pollito.stonks_java.trade.adapter.out.cobol;

import dev.pollito.stonks_java.trade.application.port.out.TradeValidatorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeAction;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!cobol & !production")
@Slf4j
public class TradeValidatorCobolAdapterStub implements TradeValidatorPortOutCobol {

  private static final Set<String> VALID_SYMBOLS =
      Set.of("COBL", "GMEE", "DOGE", "TEND", "FOMO", "PAPR", "YOLO", "MEME", "BUGS", "JAVA");

  @Override
  public TradeValidation validateTrade(Trade trade) {
    log.warn("Using dev stub for TradeValidatorPortOutCobol — no real COBOL engine is running");

    if (trade.action() == null) {
      return rejected("S225", "JOB ABEND S225 - INVALID ACTION");
    }

    if (!VALID_SYMBOLS.contains(trade.symbol())) {
      return rejected("S001", "JOB ABEND S001 - UNKNOWN SYMBOL " + trade.symbol());
    }

    if (trade.quantity() == 0) {
      return rejected("S224", "JOB ABEND S224 - INVALID QTY");
    }

    if (trade.price() == 0.0) {
      return rejected("S226", "JOB ABEND S226 - INVALID PRICE");
    }

    double totalCost = trade.quantity() * trade.price();
    double remainingCash = trade.cashBalance() - totalCost;

    if (trade.action() == TradeAction.BUY && remainingCash < 0) {
      return rejected("S222", "JOB ABEND S222 - INSUFF FUNDS");
    }

    // NOTE: SELL validation for sufficient shares is a known COBOL-layer limitation

    return new TradeValidation(
        ValidationStatus.ACCEPTED,
        null,
        "TRADE VALIDATED - PROCEED TO EXECUTION",
        totalCost,
        remainingCash);
  }

  private TradeValidation rejected(String errorCode, String message) {
    return new TradeValidation(ValidationStatus.REJECTED, errorCode, message, 0.0, 0.0);
  }
}
