package dev.pollito.stonks_java.config.web;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.Instant.*;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {
  private static @NonNull ProblemDetail buildProblemDetail(
      @NonNull Exception e, @NonNull HttpStatus status) {
    String exceptionSimpleName = e.getClass().getSimpleName();
    String logMessage = "{} being handled";

    switch (status.series()) {
      case SERVER_ERROR -> log.error(logMessage, exceptionSimpleName, e);
      case CLIENT_ERROR -> log.warn(logMessage, exceptionSimpleName, e);
      default -> log.info(logMessage, exceptionSimpleName, e);
    }

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, e.getLocalizedMessage());
    problemDetail.setProperty("timestamp", ISO_INSTANT.format(now()));
    problemDetail.setProperty("trace", current().getSpanContext().getTraceId());

    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handle(Exception e) {
    return buildProblemDetail(e, INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handle(NoResourceFoundException e) {
    return buildProblemDetail(e, NOT_FOUND);
  }
}
