package dev.pollito.stonks_java.chaos.adapter.out;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.List;
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

  public ChaosEventGeneratorOpenRouterAdapter(
      ChatClient.Builder builder, RateLimiterRegistry registry) {
    this.chatClient = builder.build();
    this.perMinuteRateLimiter = registry.rateLimiter("ai-chaos-per-minute");
    this.perDayRateLimiter = registry.rateLimiter("ai-chaos-per-day");
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
      return chatClient
          .prompt()
          .system(SYSTEM_PROMPT)
          .user(buildPrompt(headlines, stocks))
          .call()
          .entity(ChaosEvent.class);
    } catch (ChaosEventGenerationException e) {
      throw e;
    } catch (Exception e) {
      throw new ChaosEventGenerationException("Failed to generate chaos event via AI", e);
    }
  }

  static class ChaosEventGenerationException extends RuntimeException {
    ChaosEventGenerationException(String message) {
      super(message);
    }

    ChaosEventGenerationException(String message, Throwable cause) {
      super(message, cause);
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
