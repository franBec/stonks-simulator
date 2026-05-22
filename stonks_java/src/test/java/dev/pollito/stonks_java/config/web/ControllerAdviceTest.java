package dev.pollito.stonks_java.config.web;

import static dev.pollito.stonks_java.RestTestClientAssertions.assertResponseMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.generated.model.Error;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// Unit test (not E2E) because the generic Exception handler (500) and
// ConstraintViolationException handler are not easily triggerable through HTTP
// in the default test profile. Testing handler methods directly verifies the
// error response contract and the SERVER_ERROR/CLIENT_ERROR switch branches
// in buildProblemDetail.
@ExtendWith(MockitoExtension.class)
class ControllerAdviceTest {

  private static final String REQUEST_URI = "/api/something";

  @Mock private HttpServletRequest request;

  @InjectMocks private ControllerAdvice controllerAdvice;

  @Test
  void handleGenericExceptionReturnsInternalServerError() {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    when(request.getRequestURI()).thenReturn(REQUEST_URI);

    ResponseEntity<Error> result = controllerAdvice.handle(new RuntimeException("something broke"));

    assertThat(result.getStatusCode().value()).isEqualTo(status.value());
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getTitle()).isEqualTo(status.getReasonPhrase());
    assertThat(result.getBody().getDetail()).isEqualTo("something broke");
    assertResponseMetadata(result.getBody(), REQUEST_URI, status.value());
  }

  @Test
  void handleNoResourceFoundExceptionReturnsNotFound() {
    HttpStatus status = HttpStatus.NOT_FOUND;
    when(request.getRequestURI()).thenReturn(REQUEST_URI);

    ResponseEntity<Error> result =
        controllerAdvice.handle(
            new NoResourceFoundException(HttpMethod.GET, REQUEST_URI, "No static resource found"));

    assertThat(result.getStatusCode().value()).isEqualTo(status.value());
    assertResponseMetadata(result.getBody(), REQUEST_URI, status.value());
  }

  @Test
  void handleNoSuchElementExceptionReturnsNotFound() {
    HttpStatus status = HttpStatus.NOT_FOUND;
    when(request.getRequestURI()).thenReturn(REQUEST_URI);

    ResponseEntity<Error> result =
        controllerAdvice.handle(new NoSuchElementException("portfolio not found"));

    assertThat(result.getStatusCode().value()).isEqualTo(status.value());
    assertThat(result.getBody().getDetail()).isEqualTo("portfolio not found");
    assertResponseMetadata(result.getBody(), REQUEST_URI, status.value());
  }

  @Test
  void handleMethodArgumentNotValidExceptionReturnsBadRequest() {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    when(request.getRequestURI()).thenReturn(REQUEST_URI);
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getLocalizedMessage()).thenReturn("Validation failed");

    ResponseEntity<Error> result = controllerAdvice.handle(exception);

    assertThat(result.getStatusCode().value()).isEqualTo(status.value());
    assertThat(result.getBody().getDetail()).isEqualTo("Validation failed");
    assertResponseMetadata(result.getBody(), REQUEST_URI, status.value());
  }

  @Test
  void handleConstraintViolationExceptionReturnsBadRequest() {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    when(request.getRequestURI()).thenReturn(REQUEST_URI);
    ConstraintViolationException exception = mock(ConstraintViolationException.class);
    when(exception.getLocalizedMessage()).thenReturn("Constraint violation");

    ResponseEntity<Error> result = controllerAdvice.handle(exception);

    assertThat(result.getStatusCode().value()).isEqualTo(status.value());
    assertThat(result.getBody().getDetail()).isEqualTo("Constraint violation");
    assertResponseMetadata(result.getBody(), REQUEST_URI, status.value());
  }

  @Test
  void handleAsyncRequestNotUsableExceptionReturnsNull() {
    assertThat(
            controllerAdvice.handle(new AsyncRequestNotUsableException("SSE client disconnected")))
        .isNull();
  }
}
