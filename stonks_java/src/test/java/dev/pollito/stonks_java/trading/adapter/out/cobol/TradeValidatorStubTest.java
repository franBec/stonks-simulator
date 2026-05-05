package dev.pollito.stonks_java.trading.adapter.out.cobol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.pollito.stonks_java.trading.domain.Trade;
import dev.pollito.stonks_java.trading.domain.TradeAction;
import dev.pollito.stonks_java.trading.domain.TradeValidation;
import dev.pollito.stonks_java.trading.domain.ValidationStatus;
import org.junit.jupiter.api.Test;

class TradeValidatorStubTest {
  private final TradeValidatorStub stub = new TradeValidatorStub();

  @Test
  void validBuy() {
    TradeValidation result = stub.validate(new Trade(TradeAction.BUY, "GMEE", 10, 45.0, 10000.0));

    assertEquals(ValidationStatus.ACCEPTED, result.status());
    assertEquals(450.0, result.totalCost());
    assertEquals(9550.0, result.remainingCash());
    assertEquals(null, result.errorCode());
  }

  @Test
  void insufficientFunds() {
    TradeValidation result = stub.validate(new Trade(TradeAction.BUY, "GMEE", 1000, 45.0, 100.0));

    assertEquals(ValidationStatus.REJECTED, result.status());
    assertEquals("S222", result.errorCode());
  }

  @Test
  void invalidSymbol() {
    TradeValidation result = stub.validate(new Trade(TradeAction.BUY, "FAKE", 10, 45.0, 10000.0));

    assertEquals(ValidationStatus.REJECTED, result.status());
    assertEquals("S001", result.errorCode());
  }

  @Test
  void invalidQuantity() {
    TradeValidation result = stub.validate(new Trade(TradeAction.BUY, "GMEE", 0, 45.0, 10000.0));

    assertEquals(ValidationStatus.REJECTED, result.status());
    assertEquals("S224", result.errorCode());
  }

  @Test
  void invalidAction() {
    TradeValidation result = stub.validate(new Trade(null, "GMEE", 10, 45.0, 10000.0));

    assertEquals(ValidationStatus.REJECTED, result.status());
    assertEquals("S225", result.errorCode());
  }

  @Test
  void sellIsAcceptedWithoutShareCheck() {
    TradeValidation result = stub.validate(new Trade(TradeAction.SELL, "GMEE", 10, 45.0, 1000.0));

    assertEquals(ValidationStatus.ACCEPTED, result.status());
    assertEquals(450.0, result.totalCost());
  }
}
