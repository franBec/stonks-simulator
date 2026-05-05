package dev.pollito.stonks_java.test.util;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;

public final class MockMvcResultMatchers {

  private MockMvcResultMatchers() {}

  public static ResultMatcher hasStandardApiResponseFields(
      String expectedInstance, HttpStatus expectedStatus) {
    return result -> {
      jsonPath("$.instance").value(expectedInstance).match(result);
      jsonPath("$.status").value(expectedStatus.value()).match(result);
      jsonPath("$.timestamp").exists().match(result);
      jsonPath("$.trace").exists().match(result);
    };
  }

  public static ResultMatcher hasErrorFields(HttpStatus expectedStatus) {
    return result -> jsonPath("$.title").value(expectedStatus.getReasonPhrase()).match(result);
  }
}
