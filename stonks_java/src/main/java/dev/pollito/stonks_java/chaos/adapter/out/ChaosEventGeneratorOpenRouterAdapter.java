package dev.pollito.stonks_java.chaos.adapter.out;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
@RequiredArgsConstructor
@Slf4j
public class ChaosEventGeneratorOpenRouterAdapter implements ChaosEventGeneratorPortOut {

  private static final String SYSTEM_PROMPT =
      "You are a chaotic meme stock market generator. Given real news headlines and a list of meme"
          + " stocks, generate a single chaotic trading event in valid JSON with NO markdown"
          + " formatting, NO code fences, NO extra text. Respond ONLY with a raw JSON object.";

  private final ChatModel chatModel;

  @Override
  public ChaosEvent generate(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    try {
      BeanOutputConverter<ChaosEvent> converter = new BeanOutputConverter<>(ChaosEvent.class);
      return converter.convert(
          chatModel
              .call(
                  new Prompt(
                      List.of(
                          new SystemMessage(SYSTEM_PROMPT + "\n" + converter.getFormat()),
                          new UserMessage(buildPrompt(headlines, stocks)))))
              .getResult()
              .getOutput()
              .getText());
    } catch (Exception e) {
      throw new ChaosEventGenerationException("Failed to generate chaos event via AI", e);
    }
  }

  static class ChaosEventGenerationException extends RuntimeException {
    ChaosEventGenerationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private String buildPrompt(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    StringBuilder sb = new StringBuilder("Today's news headlines:\n");
    for (NewsHeadline h : headlines) {
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
