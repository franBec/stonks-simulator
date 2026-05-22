package dev.pollito.stonks_java.cobol.adapter.out;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollito.stonks_java.cobol.CobolEngineException;
import dev.pollito.stonks_java.cobol.config.CobolProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

// Integration test (not E2E) because it exercises real subprocess spawning (shell scripts),
// stdin/stdout piping, JSON parsing, timeout, and error handling for non-zero exit codes —
// low-level infrastructure concerns of the COBOL bridge. E2E tests use COBOL stubs which bypass
// this code path entirely. Also lives in the shared 'cobol' module, not loaded by
// @ApplicationModuleTest.
//
// Missed branches in CobolProgramExecutor.java and why they are acceptable:
//   - resolveProgramPath relative-path branch: tests always use absolute temp paths.
//     This is a trivial Path.of + normalize utility; relative path resolution would need
//     to be tested via a properties config change rather than a real integration concern.
//   - Timeout branch (process.waitFor returns false): requires a process that outlives the
//     configured timeout, which is non-deterministic in CI and would slow down the suite.
//     The timeout behavior (destroyForcibly + exception) is logically correct.
//   - InterruptedException catch: reliably triggering a thread interruption during
//     subprocess I/O is fragile and highly environment-sensitive. The handler correctly
//     re-interrupts the thread and rethrows, which is the mandated pattern.
class CobolProgramExecutorIntegrationTest {
  public record TestRequest(String value) {}

  public record TestResponse(String value) {}

  @Test
  void executesRealProcess() throws Exception {
    Path script = createTempFile("test-script", ".sh");
    Files.writeString(script, "#!/bin/sh\ncat > /dev/null\necho '{\"value\":\"hello\"}'\n");
    Files.setPosixFilePermissions(script, fromString("rwxr-xr-x"));

    try {
      CobolProperties properties = new CobolProperties();
      CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
      config.setPath(script.toString());
      config.setTimeoutSeconds(5);
      properties.getPrograms().put("echo", config);

      CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());
      TestResponse response = service.execute("echo", new TestRequest("world"), TestResponse.class);

      assertEquals("hello", response.value());
    } finally {
      Files.deleteIfExists(script);
    }
  }

  @Test
  void throwsWhenProgramNotConfigured() {
    CobolProperties properties = new CobolProperties();
    CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class,
            () -> service.execute("unknown", new TestRequest("x"), TestResponse.class));

    assertTrue(ex.getMessage().contains("No configuration found"));
  }

  @Test
  void throwsWhenPathIsEmpty() throws Exception {
    CobolProperties properties = new CobolProperties();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("");
    config.setTimeoutSeconds(5);
    properties.getPrograms().put("echo", config);

    CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class,
            () -> service.execute("echo", new TestRequest("x"), TestResponse.class));

    assertTrue(ex.getMessage().contains("Path not configured"));
  }

  @Test
  void throwsWhenPathIsNull() {
    CobolProperties properties = new CobolProperties();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath(null);
    config.setTimeoutSeconds(5);
    properties.getPrograms().put("echo", config);

    CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class,
            () -> service.execute("echo", new TestRequest("x"), TestResponse.class));

    assertTrue(ex.getMessage().contains("Path not configured"));
  }

  @Test
  void throwsOnNonZeroExit() throws Exception {
    Path script = createTempFile("test-script-exit", ".sh");
    Files.writeString(script, "#!/bin/sh\necho '{}'\nexit 1\n");
    Files.setPosixFilePermissions(script, fromString("rwxr-xr-x"));

    try {
      CobolProperties properties = new CobolProperties();
      CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
      config.setPath(script.toString());
      config.setTimeoutSeconds(5);
      properties.getPrograms().put("echo", config);

      CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());

      CobolEngineException ex =
          assertThrows(
              CobolEngineException.class,
              () -> service.execute("echo", new TestRequest("world"), TestResponse.class));

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
      CobolProperties properties = new CobolProperties();
      CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
      config.setPath(script.toString());
      config.setTimeoutSeconds(5);
      properties.getPrograms().put("echo", config);

      CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());
      TestResponse response = service.execute("echo", null, TestResponse.class);

      assertEquals("null-request", response.value());
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
      CobolProperties properties = new CobolProperties();
      CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
      config.setPath(script.toString());
      config.setTimeoutSeconds(5);
      properties.getPrograms().put("echo", config);

      CobolProgramExecutor service = new CobolProgramExecutor(properties, new ObjectMapper());

      assertThrows(
          CobolEngineException.class,
          () -> service.execute("echo", new TestRequest("world"), TestResponse.class));
    } finally {
      Files.deleteIfExists(script);
    }
  }
}
