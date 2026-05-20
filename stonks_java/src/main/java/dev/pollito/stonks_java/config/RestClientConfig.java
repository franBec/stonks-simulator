package dev.pollito.stonks_java.config;

import static java.time.Duration.ofSeconds;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .requestFactory(
            new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(ofSeconds(5)).build()))
        .build();
  }
}
