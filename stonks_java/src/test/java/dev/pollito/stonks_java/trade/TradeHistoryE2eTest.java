package dev.pollito.stonks_java.trade;

import static dev.pollito.stonks_java.test.util.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.generated.entity.TradeHistory;
import dev.pollito.stonks_java.generated.model.TradeHistoryResponse;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPortfolioJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeHistoryJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
@TestPropertySource(
    properties = {"spring.datasource.url=jdbc:h2:mem:stonks-history-test;DB_CLOSE_DELAY=0"})
@AutoConfigureRestTestClient
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TradeHistoryE2eTest {

  @Autowired private RestTestClient restTestClient;
  @Autowired private TradeExecutionPortfolioJpaRepository portfolioRepo;
  @Autowired private TradeHistoryJpaRepository tradeHistoryRepo;

  @Test
  void paginatedHistory() {
    var portfolio = portfolioRepo.findById(1L).orElseThrow();

    var h1 = new TradeHistory();
    h1.setPortfolio(portfolio);
    h1.setAction("BUY");
    h1.setSymbol("GMEE");
    h1.setQuantity(10L);
    h1.setPrice(BigDecimal.valueOf(45.0));
    h1.setTotalCost(BigDecimal.valueOf(450.0));
    h1.setCashBalanceAfter(BigDecimal.valueOf(9550.0));
    h1.setExecutedAt(LocalDateTime.of(2026, 1, 3, 17, 0));
    tradeHistoryRepo.save(h1);

    var h2 = new TradeHistory();
    h2.setPortfolio(portfolio);
    h2.setAction("SELL");
    h2.setSymbol("GMEE");
    h2.setQuantity(5L);
    h2.setPrice(BigDecimal.valueOf(50.0));
    h2.setTotalCost(BigDecimal.valueOf(250.0));
    h2.setCashBalanceAfter(BigDecimal.valueOf(9800.0));
    h2.setExecutedAt(LocalDateTime.of(2026, 1, 4, 17, 0));
    tradeHistoryRepo.save(h2);

    var result =
        restTestClient
            .get()
            .uri("/api/trades/history?page=0&size=1")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeHistoryResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades/history", 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getTotalElements()).isEqualTo(2);
    assertThat(data.getTotalPages()).isEqualTo(2);
    assertThat(data.getPageable().getPageNumber()).isZero();
    assertThat(data.getPageable().getPageSize()).isEqualTo(1);
    assertThat(data.getContent()).hasSize(1);

    var item = data.getContent().getFirst();
    assertThat(item.getAction()).isEqualTo("SELL");
    assertThat(item.getSymbol()).isEqualTo("GMEE");
    assertThat(item.getQuantity()).isEqualTo(5);
  }

  @Test
  void emptyHistory() {
    var result =
        restTestClient
            .get()
            .uri("/api/trades/history")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(TradeHistoryResponse.class);

    assertResponseMetadata(result.getResponseBody(), "/api/trades/history", 200);
    var data = result.getResponseBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getContent()).isEmpty();
    assertThat(data.getTotalElements()).isZero();
    assertThat(data.getTotalPages()).isZero();
  }
}
