package dev.pollito.stonks_java.cobol.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollito.stonks_java.cobol.CobolEngineException;
import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.cobol.config.CobolProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CobolProgramExecutor implements CobolAppPortOut {
  private final CobolProperties properties;
  private final ObjectMapper objectMapper;

  @Override
  public <REQ, RES> RES execute(String programName, REQ request, Class<RES> responseType) {
    CobolProperties.ProgramConfig config = properties.getPrograms().get(programName);
    if (config == null) {
      throw new CobolEngineException("No configuration found for COBOL program: " + programName);
    }
    if (config.getPath() == null || config.getPath().isBlank()) {
      throw new CobolEngineException("Path not configured for COBOL program: " + programName);
    }

    try {
      String resolvedPath = resolveProgramPath(config.getPath());
      ProcessBuilder pb = new ProcessBuilder(resolvedPath);
      pb.redirectErrorStream(true);
      Process process = startProcess(pb);

      if (request != null) {
        try (Writer writer = new OutputStreamWriter(process.getOutputStream())) {
          writer.write(objectMapper.writeValueAsString(request));
          writer.flush();
        }
      }

      StringBuilder output = new StringBuilder();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line);
        }
      }

      boolean finished = process.waitFor(config.getTimeoutSeconds(), TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        throw new CobolEngineException(
            "COBOL process timed out after " + config.getTimeoutSeconds() + " seconds");
      }

      int exitCode = process.exitValue();
      if (exitCode != 0) {
        throw new CobolEngineException(
            "COBOL process exited with code " + exitCode + ". Output: " + output);
      }

      return objectMapper.readValue(output.toString().trim(), responseType);
    } catch (IOException e) {
      throw new CobolEngineException("Failed to execute COBOL program: " + programName, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CobolEngineException("Failed to execute COBOL program: " + programName, e);
    }
  }

  Process startProcess(ProcessBuilder pb) throws IOException {
    return pb.start();
  }

  private String resolveProgramPath(String rawPath) {
    Path path = Path.of(rawPath);
    if (path.isAbsolute()) {
      return rawPath;
    }
    String userDir = System.getProperty("user.dir");
    return Path.of(userDir).resolve(path).normalize().toString();
  }
}
