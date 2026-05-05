package dev.pollito.stonks_java.cobol;

public class CobolEngineException extends RuntimeException {
  public CobolEngineException(String message) {
    super(message);
  }

  public CobolEngineException(String message, Throwable cause) {
    super(message, cause);
  }
}
