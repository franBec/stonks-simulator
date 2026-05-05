package dev.pollito.stonks_java.config.web;

import static dev.pollito.stonks_java.test.util.MockMvcResultMatchers.hasErrorFields;
import static dev.pollito.stonks_java.test.util.MockMvcResultMatchers.hasStandardApiResponseFields;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class ControllerAdviceMockMvcTest {

  private MockMvc mockMvc;
  private final HttpServletRequest request = mock(HttpServletRequest.class);

  @RestController
  @RequestMapping("/fake")
  @Profile("test")
  private static class FakeController {

    @GetMapping("/not-found")
    @SuppressWarnings("unused")
    public void throwNoResourceFoundException() throws NoResourceFoundException {
      throw new NoResourceFoundException(GET, "/fake", "no-resource-found");
    }

    @GetMapping("/error")
    @SuppressWarnings("unused")
    public void throwException() throws Exception {
      throw new Exception("Test exception");
    }

    @GetMapping("/bad-request")
    @SuppressWarnings("unused")
    public void throwConstraintViolationException() {
      throw new ConstraintViolationException("Constraint violation", Set.of());
    }

    @GetMapping("/method-arg-not-valid")
    @SuppressWarnings({"unused"})
    public void throwMethodArgumentNotValidException() throws Exception {
      throw new MethodArgumentNotValidException(
          new MethodParameter(
              FakeController.class.getMethod("throwMethodArgumentNotValidException"), -1),
          mock(BindingResult.class));
    }
  }

  @BeforeEach
  void setUp() {
    mockMvc =
        standaloneSetup(new FakeController())
            .setControllerAdvice(new ControllerAdvice(request))
            .build();
  }

  static @NonNull Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of("/fake/not-found", NOT_FOUND),
        Arguments.of("/fake/error", INTERNAL_SERVER_ERROR),
        Arguments.of("/fake/bad-request", BAD_REQUEST),
        Arguments.of("/fake/method-arg-not-valid", BAD_REQUEST));
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void exceptionHandlingReturnsCorrectStatus(String path, @NonNull HttpStatus expectedStatus)
      throws Exception {
    when(request.getRequestURI()).thenReturn(path);
    mockMvc
        .perform(get(path))
        .andExpect(status().is(expectedStatus.value()))
        .andExpect(hasStandardApiResponseFields(path, expectedStatus))
        .andExpect(hasErrorFields(expectedStatus));
  }
}
