package dev.pollito.stonks_java.integration;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollito.stonks_java.cobol.CobolEngineException;
import dev.pollito.stonks_java.cobol.adapter.out.CobolProgramExecutor;
import dev.pollito.stonks_java.cobol.config.CobolProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CobolProgramExecutorIntegrationTest {
  public record TestRequest(String value) {}

  public record TestResponse(String value) {}

  private CobolProperties propertiesWithProgram(String path) {
    CobolProperties properties = new CobolProperties();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath(path);
    config.setTimeoutSeconds(5);
    properties.getPrograms().put("echo", config);
    return properties;
  }

  @Test
  void executesRealProcess() throws Exception {
    Path script = createTempFile("test-script", ".sh");
    Files.writeString(script, "#!/bin/sh\ncat > /dev/null\necho '{\"value\":\"hello\"}'\n");
    Files.setPosixFilePermissions(script, fromString("rwxr-xr-x"));

    try {
      assertEquals(
          "hello",
          new CobolProgramExecutor(propertiesWithProgram(script.toString()), new ObjectMapper())
              .execute("echo", new TestRequest("world"), TestResponse.class)
              .value());
    } finally {
      Files.deleteIfExists(script);
    }
  }

  @Test
  void throwsWhenProgramNotConfigured() {
    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class,
            () ->
                new CobolProgramExecutor(new CobolProperties(), new ObjectMapper())
                    .execute("unknown", new TestRequest("x"), TestResponse.class));

    assertTrue(ex.getMessage().contains("No configuration found"));
  }

  @Test
  void throwsWhenPathIsEmpty() throws Exception {
    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class,
            () ->
                new CobolProgramExecutor(propertiesWithProgram(""), new ObjectMapper())
                    .execute("echo", new TestRequest("x"), TestResponse.class));

    assertTrue(ex.getMessage().contains("Path not configured"));
  }

  @Test
  void throwsWhenPathIsNull() {
    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class,
            () ->
                new CobolProgramExecutor(propertiesWithProgram(null), new ObjectMapper())
                    .execute("echo", new TestRequest("x"), TestResponse.class));

    assertTrue(ex.getMessage().contains("Path not configured"));
  }

  @Test
  void throwsOnNonZeroExit() throws Exception {
    Path script = createTempFile("test-script-exit", ".sh");
    Files.writeString(script, "#!/bin/sh\necho '{}'\nexit 1\n");
    Files.setPosixFilePermissions(script, fromString("rwxr-xr-x"));

    try {
      CobolEngineException ex =
          assertThrows(
              CobolEngineException.class,
              () ->
                  new CobolProgramExecutor(
                          propertiesWithProgram(script.toString()), new ObjectMapper())
                      .execute("echo", new TestRequest("world"), TestResponse.class));

      assertTrue(ex.getMessage().contains("exited with code 1"));
    } finally {
      Files.deleteIfExists(script);
    }
  }

  @Test
  void handlesNullRequest() throws Exception {
    Path script = createTempFile("test-script-null-req", ".sh");
    Files.writeString(script, "#!/bin/sh\necho '{\"value\":\"null-request\"}'\n");
    Files.setPosixFilePermissions(script, fromString("rwxr-xr-x"));

    try {
      assertEquals(
          "null-request",
          new CobolProgramExecutor(propertiesWithProgram(script.toString()), new ObjectMapper())
              .execute("echo", null, TestResponse.class)
              .value());
    } finally {
      Files.deleteIfExists(script);
    }
  }

  @Test
  void throwsOnMalformedJson() throws Exception {
    Path script = createTempFile("test-script-malformed", ".sh");
    Files.writeString(script, "#!/bin/sh\necho 'not-json'\n");
    Files.setPosixFilePermissions(script, fromString("rwxr-xr-x"));

    try {
      assertThrows(
          CobolEngineException.class,
          () ->
              new CobolProgramExecutor(propertiesWithProgram(script.toString()), new ObjectMapper())
                  .execute("echo", new TestRequest("world"), TestResponse.class));
    } finally {
      Files.deleteIfExists(script);
    }
  }
}
