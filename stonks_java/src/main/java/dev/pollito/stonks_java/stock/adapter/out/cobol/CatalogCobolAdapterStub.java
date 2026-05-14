package dev.pollito.stonks_java.stock.adapter.out.cobol;

import static java.util.List.of;

import dev.pollito.stonks_java.stock.application.port.out.StockPortOut;
import dev.pollito.stonks_java.stock.domain.Stock;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!cobol & !production")
@Slf4j
public class CatalogCobolAdapterStub implements StockPortOut {

  @Override
  public List<Stock> getStocks() {
    log.warn("Using dev stub for StockPortOut — no real COBOL engine is running");
    return of(
        new Stock("COBL", "COBOL Corp", new BigDecimal("100.00"), new BigDecimal("0.05"), "BULL"),
        new Stock("GMEE", "GameStonks", new BigDecimal("50.00"), new BigDecimal("0.25"), "MOON"),
        new Stock("DOGE", "DogeCoin Ltd", new BigDecimal("10.00"), new BigDecimal("0.30"), "CHAOS"),
        new Stock("TEND", "Tendie Inc", new BigDecimal("25.00"), new BigDecimal("0.20"), "BEAR"),
        new Stock("FOMO", "FOMO Holdings", new BigDecimal("75.00"), new BigDecimal("0.15"), "BULL"),
        new Stock("PAPR", "Paper Hands", new BigDecimal("15.00"), new BigDecimal("0.10"), "BEAR"),
        new Stock("YOLO", "YOLO Capital", new BigDecimal("20.00"), new BigDecimal("0.50"), "CHAOS"),
        new Stock("MEME", "MemeStonks", new BigDecimal("10.00"), new BigDecimal("0.20"), "MOON"),
        new Stock(
            "BUGS", "Buggy Software", new BigDecimal("30.00"), new BigDecimal("0.20"), "CRASH"),
        new Stock("JAVA", "JavaBeans", new BigDecimal("150.00"), new BigDecimal("0.05"), "BULL"));
  }
}
