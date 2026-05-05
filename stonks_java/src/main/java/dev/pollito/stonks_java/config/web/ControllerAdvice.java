package dev.pollito.stonks_java.config.web;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.status;

import dev.pollito.stonks_java.generated.model.Error;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ControllerAdvice {
  private final HttpServletRequest request;

  private @NonNull ResponseEntity<Error> buildProblemDetail(
      @NonNull Exception e, @NonNull HttpStatus status) {
    String exceptionSimpleName = e.getClass().getSimpleName();
    String logMessage = "{} being handled";

    switch (status.series()) {
      case SERVER_ERROR -> log.error(logMessage, exceptionSimpleName, e);
      case CLIENT_ERROR -> log.warn(logMessage, exceptionSimpleName, e);
      default -> log.info(logMessage, exceptionSimpleName, e);
    }

    return status(status)
        .body(
            new Error()
                .detail(e.getLocalizedMessage())
                .instance(request.getRequestURI())
                .status(status.value())
                .timestamp(now())
                .title(status.getReasonPhrase())
                .trace(current().getSpanContext().getTraceId()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handle(Exception e) {
    return buildProblemDetail(e, INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Error> handle(NoResourceFoundException e) {
    return buildProblemDetail(e, NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Error> handle(MethodArgumentNotValidException e) {
    return buildProblemDetail(e, BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Error> handle(ConstraintViolationException e) {
    return buildProblemDetail(e, BAD_REQUEST);
  }
}
