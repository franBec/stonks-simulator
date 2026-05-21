package dev.pollito.stonks_java.chaos.domain;

public class ChaosEventGenerationException extends RuntimeException {

  public ChaosEventGenerationException(String message) {
    super(message);
  }

  public ChaosEventGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}
