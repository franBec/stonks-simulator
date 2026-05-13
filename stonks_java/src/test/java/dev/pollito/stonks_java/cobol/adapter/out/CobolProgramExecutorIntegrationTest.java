package dev.pollito.stonks_java.cobol.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollito.stonks_java.cobol.config.CobolProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;

class CobolProgramExecutorIntegrationTest {

  @Test
  void executesRealProcess() throws Exception {
    Path script = Files.createTempFile("test-script", ".sh");
    Files.writeString(script, "#!/bin/sh\ncat > /dev/null\necho '{\"value\":\"hello\"}'\n");
    Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr-x"));

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

  public record TestRequest(String value) {}

  public record TestResponse(String value) {}
}
