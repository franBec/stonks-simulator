package dev.pollito.stonks_java;

import static org.assertj.core.api.Assertions.assertThat;

import dev.pollito.stonks_java.util.metadata.ResponseMetadata;

public final class RestTestClientAssertions {

  private RestTestClientAssertions() {}

  public static void assertResponseMetadata(
      ResponseMetadata metadata, String expectedInstance, int expectedStatus) {
    assertThat(metadata).isNotNull();
    assertThat(metadata.getInstance()).isEqualTo(expectedInstance);
    assertThat(metadata.getStatus()).isEqualTo(expectedStatus);
    assertThat(metadata.getTimestamp()).isNotNull();
    assertThat(metadata.getTrace()).isNotNull();
  }
}
