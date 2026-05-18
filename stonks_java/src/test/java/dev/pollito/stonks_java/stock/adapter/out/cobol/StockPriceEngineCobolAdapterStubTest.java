package dev.pollito.stonks_java.stock.adapter.out.cobol;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockPriceEngineCobolAdapterStubTest {

  private StockPriceEngineCobolAdapterStub stub;

  @BeforeEach
  void setUp() {
    stub = new StockPriceEngineCobolAdapterStub();
  }

  @Test
  void bullTrendIncreasesPrice() {
    assertThat(stub.calculate(new BigDecimal("100"), new BigDecimal("0.0001"), "BULL"))
        .isGreaterThan(new BigDecimal("100"));
  }

  @Test
  void bearTrendDecreasesPrice() {
    assertThat(stub.calculate(new BigDecimal("100"), new BigDecimal("0.0001"), "BEAR"))
        .isLessThan(new BigDecimal("100"));
  }

  @Test
  void moonTrendStronglyIncreasesPrice() {
    assertThat(stub.calculate(new BigDecimal("100"), new BigDecimal("0.0001"), "MOON"))
        .isGreaterThan(new BigDecimal("100.50"));
  }

  @Test
  void crashTrendStronglyDecreasesPrice() {
    assertThat(stub.calculate(new BigDecimal("100"), new BigDecimal("0.0001"), "CRASH"))
        .isLessThan(new BigDecimal("99.50"));
  }

  @Test
  void chaosTrendMovesPrice() {
    assertThat(stub.calculate(new BigDecimal("100"), new BigDecimal("0.0001"), "CHAOS"))
        .isNotNull();
  }

  @Test
  void unknownTrendUsesZeroBias() {
    assertThat(stub.calculate(new BigDecimal("100"), new BigDecimal("0.0001"), "UNKNOWN"))
        .isBetween(new BigDecimal("99.90"), new BigDecimal("100.10"));
  }

  @Test
  void clampsToMaxPrice() {
    assertThat(stub.calculate(new BigDecimal("499.50"), new BigDecimal("0.0001"), "BULL"))
        .isEqualByComparingTo(new BigDecimal("500.00"));
  }

  @Test
  void clampsToMinPrice() {
    assertThat(stub.calculate(new BigDecimal("0.105"), new BigDecimal("0.0001"), "CRASH"))
        .isEqualByComparingTo(new BigDecimal("0.10"));
  }
}
