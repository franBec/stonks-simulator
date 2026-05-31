package dev.pollito.stonks_java.chaosevent.domain;

public class ChaoticEventGenerationException extends RuntimeException {
  public ChaoticEventGenerationException(String message) {
    super(message);
  }

  public ChaoticEventGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}
