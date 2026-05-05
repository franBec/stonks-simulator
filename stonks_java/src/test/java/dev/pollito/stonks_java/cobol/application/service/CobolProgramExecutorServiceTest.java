package dev.pollito.stonks_java.cobol.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollito.stonks_java.cobol.CobolEngineException;
import dev.pollito.stonks_java.cobol.config.CobolProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CobolProgramExecutorServiceTest {

  private CobolProperties properties;
  private CobolProgramExecutorService service;

  @BeforeEach
  void setUp() {
    properties = mock(CobolProperties.class);
    service = spy(new CobolProgramExecutorService(properties, new ObjectMapper()));
  }

  @AfterEach
  void tearDown() {
    Thread.interrupted();
  }

  @Test
  void throwsWhenProgramConfigMissing() {
    when(properties.getPrograms()).thenReturn(new HashMap<>());

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("missing", "req", String.class));

    assertEquals("No configuration found for COBOL program: missing", ex.getMessage());
  }

  @Test
  void throwsWhenPathIsBlank() {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("   ");
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("prog", "req", String.class));

    assertEquals("Path not configured for COBOL program: prog", ex.getMessage());
  }

  @Test
  void throwsWhenPathIsNull() {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath(null);
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("prog", "req", String.class));

    assertEquals("Path not configured for COBOL program: prog", ex.getMessage());
  }

  @Test
  void happyPath() throws Exception {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("/some/path");
    config.setTimeoutSeconds(5);
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    Process process = mock(Process.class);
    ByteArrayOutputStream processStdin = new ByteArrayOutputStream();
    when(process.getOutputStream()).thenReturn(processStdin);
    String jsonOut = "{\"value\":\"hello\"}";
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(jsonOut.getBytes()));
    when(process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
    when(process.exitValue()).thenReturn(0);

    doReturn(process).when(service).startProcess(any(ProcessBuilder.class));

    TestResponse result = service.execute("prog", new TestRequest("world"), TestResponse.class);

    assertEquals("hello", result.value());
    String written = processStdin.toString();
    assertTrue(written.contains("world"));
  }

  @Test
  void throwsWhenProcessTimesOut() throws Exception {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("/some/path");
    config.setTimeoutSeconds(2);
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    Process process = mock(Process.class);
    when(process.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(process.waitFor(2, TimeUnit.SECONDS)).thenReturn(false);
    when(process.destroyForcibly()).thenReturn(process);

    doReturn(process).when(service).startProcess(any(ProcessBuilder.class));

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("prog", "req", String.class));

    assertEquals("COBOL process timed out after 2 seconds", ex.getMessage());
    verify(process).destroyForcibly();
  }

  @Test
  void throwsWhenNonZeroExitCode() throws Exception {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("/some/path");
    config.setTimeoutSeconds(5);
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    Process process = mock(Process.class);
    when(process.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream("error output".getBytes()));
    when(process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
    when(process.exitValue()).thenReturn(1);

    doReturn(process).when(service).startProcess(any(ProcessBuilder.class));

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("prog", "req", String.class));

    assertTrue(ex.getMessage().contains("exited with code 1"));
    assertTrue(ex.getMessage().contains("error output"));
  }

  @Test
  void throwsWhenStartProcessFails() throws Exception {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("/some/path");
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    doThrow(new IOException("boom")).when(service).startProcess(any(ProcessBuilder.class));

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("prog", "req", String.class));

    assertEquals("Failed to execute COBOL program: prog", ex.getMessage());
    assertEquals("boom", ex.getCause().getMessage());
    assertFalse(Thread.currentThread().isInterrupted());
  }

  @Test
  void throwsWhenInterrupted() throws Exception {
    Map<String, CobolProperties.ProgramConfig> programs = new HashMap<>();
    CobolProperties.ProgramConfig config = new CobolProperties.ProgramConfig();
    config.setPath("/some/path");
    config.setTimeoutSeconds(5);
    programs.put("prog", config);
    when(properties.getPrograms()).thenReturn(programs);

    Process process = mock(Process.class);
    when(process.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(process.waitFor(5, TimeUnit.SECONDS)).thenThrow(new InterruptedException("interrupted"));

    doReturn(process).when(service).startProcess(any(ProcessBuilder.class));

    CobolEngineException ex =
        assertThrows(
            CobolEngineException.class, () -> service.execute("prog", "req", String.class));

    assertEquals("Failed to execute COBOL program: prog", ex.getMessage());
    assertTrue(Thread.currentThread().isInterrupted());
  }

  public record TestRequest(String value) {}

  public record TestResponse(String value) {}
}
