package dev.pollito.stonks_java.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES;

import dev.pollito.stonks_java.news.application.port.in.NewsPortIn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES, webEnvironment = RANDOM_PORT)
// Tests via NewsPortIn directly (not REST) because the news module has no REST adapter — it is
// an internal module consumed by other modules (chaos). @ApplicationModuleTest verifies Spring
// context wiring and stub integration, which is the most meaningful test for a headless module.
class NewsModuleE2eTest {

  @Autowired private NewsPortIn newsPortIn;

  @Test
  void stubReturnsHeadlines() {
    var headlines = newsPortIn.getHeadlines();
    assertThat(headlines).isNotEmpty();
    assertThat(headlines.get(0).title()).isNotBlank();
    assertThat(headlines.get(0).source()).isNotBlank();
  }
}
