package dev.pollito.stonks_java.stock.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.stock.application.port.out.StockPortOut;
import dev.pollito.stonks_java.stock.application.port.out.StockPriceEnginePortOut;
import dev.pollito.stonks_java.stock.domain.Stock;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

  @Mock private StockPriceEnginePortOut priceEnginePortOut;
  @Mock private StockPortOut stockPortOut;
  @Mock private ApplicationEventPublisher events;

  private StockService stockService;

  private static final Stock COBL =
      new Stock("COBL", "COBOL Corp", new BigDecimal("100.00"), new BigDecimal("0.05"), "BULL");
  private static final Stock GMEE =
      new Stock("GMEE", "GameStonks", new BigDecimal("50.00"), new BigDecimal("0.25"), "MOON");

  @BeforeEach
  void setUp() {
    when(stockPortOut.getStocks()).thenReturn(List.of(COBL, GMEE));
    stockService = new StockService(priceEnginePortOut, stockPortOut, events);
    stockService.initialize();
  }

  @Test
  void simulateUsesCachedCatalogNotPortOut() {
    when(priceEnginePortOut.calculate(any(), any(), any()))
        .thenAnswer(inv -> inv.getArgument(0));

    stockService.simulate();

    verify(stockPortOut, times(1)).getStocks();
  }

  @Test
  void simulateAppliesVolatilityMultiplier() {
    stockService.setVolatilityMultiplier(2.0);
    when(priceEnginePortOut.calculate(any(), any(), any()))
        .thenAnswer(inv -> inv.getArgument(0));

    stockService.simulate();

    ArgumentCaptor<BigDecimal> volatilityCaptor = ArgumentCaptor.forClass(BigDecimal.class);
    verify(priceEnginePortOut)
        .calculate(any(), volatilityCaptor.capture(), eq("BULL"));

    assertThat(volatilityCaptor.getValue())
        .isEqualByComparingTo(new BigDecimal("0.10"));
  }

  @Test
  void simulateWithDefaultMultiplierPassesVolatilityUnchanged() {
    when(priceEnginePortOut.calculate(any(), any(), any()))
        .thenAnswer(inv -> inv.getArgument(0));

    stockService.simulate();

    ArgumentCaptor<BigDecimal> volatilityCaptor = ArgumentCaptor.forClass(BigDecimal.class);
    verify(priceEnginePortOut, times(2))
        .calculate(any(), volatilityCaptor.capture(), any());

    assertThat(volatilityCaptor.getAllValues())
        .usingElementComparator(BigDecimal::compareTo)
        .containsExactly(new BigDecimal("0.05"), new BigDecimal("0.25"));
  }

  @Test
  void simulateSkipsStockOnPriceEngineFailure() {
    when(priceEnginePortOut.calculate(eq(new BigDecimal("100.00")), any(), eq("BULL")))
        .thenThrow(new RuntimeException("COBOL timeout"));
    when(priceEnginePortOut.calculate(eq(new BigDecimal("50.00")), any(), eq("MOON")))
        .thenReturn(new BigDecimal("51.00"));

    stockService.simulate();

    List<StockPrice> prices = stockService.getStocks();
    assertThat(prices).hasSize(2);
    assertThat(prices.stream().filter(p -> p.symbol().equals("GMEE")).findFirst())
        .isPresent()
        .hasValueSatisfying(p -> assertThat(p.price()).isEqualByComparingTo(new BigDecimal("51.00")));
  }

  @Test
  void setVolatilityMultiplierAffectsPriceCalculation() {
    stockService.setVolatilityMultiplier(5.0);
    when(priceEnginePortOut.calculate(any(), any(), any()))
        .thenAnswer(inv -> inv.getArgument(0));

    stockService.simulate();

    ArgumentCaptor<BigDecimal> volatilityCaptor = ArgumentCaptor.forClass(BigDecimal.class);
    verify(priceEnginePortOut)
        .calculate(any(), volatilityCaptor.capture(), eq("BULL"));

    assertThat(volatilityCaptor.getValue())
        .isEqualByComparingTo(new BigDecimal("0.25"));
  }

  @Test
  void applyImpactUpdatesPrice() {
    when(priceEnginePortOut.calculate(any(), any(), any()))
        .thenReturn(new BigDecimal("52.00"));
    stockService.simulate();

    stockService.applyImpact("GMEE", new BigDecimal("10"));

    List<StockPrice> prices = stockService.getStocks();
    StockPrice gmee =
        prices.stream().filter(p -> p.symbol().equals("GMEE")).findFirst().orElseThrow();
    assertThat(gmee.price()).isEqualByComparingTo(new BigDecimal("57.20"));
  }

  @Test
  void applyImpactOnUnknownSymbolIsNoOp() {
    stockService.applyImpact("UNKNOWN", new BigDecimal("10"));

    List<StockPrice> prices = stockService.getStocks();
    assertThat(prices).hasSize(2);
  }
}