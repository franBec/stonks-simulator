package dev.pollito.stonks_java.chaos.adapter.out;

import static java.util.stream.Collectors.toSet;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.config.ChaosProperties;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosEventGenerationException;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
@Slf4j
public class ChaosEventGeneratorOpenRouterAdapter implements ChaosEventGeneratorPortOut {

  private static final String SYSTEM_PROMPT =
      "You are a chaotic meme stock market generator. Given real news headlines and a list of meme"
          + " stocks, generate a single chaotic trading event. "
          + "Respond ONLY with a raw JSON object (NO markdown, NO code fences, NO extra text) "
          + "using this exact schema:\n"
          + "{\n"
          + "  \"headline\": \"<short catchy title>\",\n"
          + "  \"symbol\": \"<stock ticker symbol>\",\n"
          + "  \"impactPercent\": <number representing price change percentage>,\n"
          + "  \"explanation\": \"<why this event is happening>\",\n"
          + "  \"affectedSymbols\": [\"<symbol1>\", \"<symbol2>\", ...],\n"
          + "  \"sourceHeadline\": \"<title of the news article that inspired this>\",\n"
          + "  \"occurredAt\": \"<ISO-8601 timestamp>\"\n"
          + "}";

  private final ChatClient chatClient;
  private final RateLimiter perMinuteRateLimiter;
  private final RateLimiter perDayRateLimiter;
  private final BigDecimal maxImpact;

  public ChaosEventGeneratorOpenRouterAdapter(
      ChatClient.Builder builder, RateLimiterRegistry registry, ChaosProperties chaosProperties) {
    this.chatClient = builder.build();
    this.perMinuteRateLimiter = registry.rateLimiter("ai-chaos-per-minute");
    this.perDayRateLimiter = registry.rateLimiter("ai-chaos-per-day");
    this.maxImpact = BigDecimal.valueOf(chaosProperties.getMaxImpactPercent());
  }

  @Override
  @Retryable(
      retryFor = ChaosEventGenerationException.class,
      maxAttempts = 2,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public ChaosEvent generate(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    if (!perMinuteRateLimiter.acquirePermission()) {
      throw new ChaosEventGenerationException("AI request rate limited (per-minute)");
    }
    if (!perDayRateLimiter.acquirePermission()) {
      throw new ChaosEventGenerationException("AI request rate limited (per-day)");
    }
    try {
      ChaosEvent event =
          chatClient
              .prompt()
              .system(SYSTEM_PROMPT)
              .user(buildPrompt(headlines, stocks))
              .call()
              .entity(ChaosEvent.class);
      validate(event, stocks);
      return event;
    } catch (ChaosEventGenerationException e) {
      throw e;
    } catch (Exception e) {
      throw new ChaosEventGenerationException("Failed to generate chaos event via AI", e);
    }
  }

  private void validate(ChaosEvent event, List<StockPrice> stocks) {
    if (event.symbol() == null) {
      throw new ChaosEventGenerationException("AI generated null symbol");
    }
    if (event.impactPercent() == null) {
      throw new ChaosEventGenerationException("AI generated null impactPercent");
    }

    Set<String> validSymbols = stocks.stream().map(StockPrice::symbol).collect(toSet());

    if (!validSymbols.contains(event.symbol())) {
      log.warn("AI hallucinated invalid symbol: {}", event.symbol());
      throw new ChaosEventGenerationException("AI hallucinated invalid symbol: " + event.symbol());
    }
    if (event.impactPercent().abs().compareTo(maxImpact) > 0) {
      log.warn("AI generated absurd impact: {}", event.impactPercent());
      throw new ChaosEventGenerationException(
          "AI generated absurd impact: " + event.impactPercent());
    }
    if (event.affectedSymbols() != null) {
      for (String s : event.affectedSymbols()) {
        if (!validSymbols.contains(s)) {
          log.warn("AI hallucinated invalid affectedSymbol: {}", s);
          throw new ChaosEventGenerationException("AI hallucinated invalid affectedSymbol: " + s);
        }
      }
    }
  }

  private String buildPrompt(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    List<NewsHeadline> topHeadlines = headlines.size() > 10 ? headlines.subList(0, 10) : headlines;
    StringBuilder sb = new StringBuilder("Today's news headlines:\n");
    for (NewsHeadline h : topHeadlines) {
      sb.append("- ").append(h.title()).append(" (").append(h.source()).append(")\n");
    }
    sb.append("\nAvailable meme stocks:\n");
    for (StockPrice s : stocks) {
      sb.append("- ").append(s.symbol()).append(": $").append(s.price()).append("\n");
    }
    sb.append("\nGenerate a chaotic trading event based on this information.");
    return sb.toString();
  }
}
