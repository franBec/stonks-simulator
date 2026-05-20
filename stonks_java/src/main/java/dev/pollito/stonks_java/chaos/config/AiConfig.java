package dev.pollito.stonks_java.chaos.config;

import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
public class AiConfig {

  @Bean
  public OpenAiChatModel openAiChatModel(
      @Value("${spring.ai.openai.base-url}") String baseUrl,
      @Value("${spring.ai.openai.api-key}") String apiKey,
      @Value("${spring.ai.openai.chat.options.model}") String model) {
    return OpenAiChatModel.builder()
        .openAiClient(OpenAIOkHttpClient.builder().baseUrl(baseUrl).apiKey(apiKey).build())
        .openAiClientAsync(
            OpenAIOkHttpClientAsync.builder().baseUrl(baseUrl).apiKey(apiKey).build())
        .options(OpenAiChatOptions.builder().model(model).build())
        .toolCallingManager(ToolCallingManager.builder().build())
        .observationRegistry(ObservationRegistry.NOOP)
        .build();
  }

  @Bean
  public ChatClient.Builder chatClientBuilder(OpenAiChatModel openAiChatModel) {
    return ChatClient.builder(openAiChatModel);
  }
}
