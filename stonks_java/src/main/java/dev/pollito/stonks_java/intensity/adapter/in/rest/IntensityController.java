package dev.pollito.stonks_java.intensity.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.generated.api.IntensityLevelApi;
import dev.pollito.stonks_java.generated.model.IntensityLevelResponse;
import dev.pollito.stonks_java.generated.model.IntensityLevelSetRequest;
import dev.pollito.stonks_java.intensity.application.port.in.IntensityPortIn;
import dev.pollito.stonks_java.util.enums.EnumUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IntensityController implements IntensityLevelApi {

  private final IntensityPortIn intensityPortIn;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<IntensityLevelResponse> getIntensityLevel() {
    return ResponseEntity.ok(
        new IntensityLevelResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(
                dev.pollito.stonks_java.generated.model.IntensityLevel.valueOf(
                    intensityPortIn.getCurrentLevel().name())));
  }

  @Override
  public ResponseEntity<IntensityLevelResponse> setIntensityLevel(
      IntensityLevelSetRequest intensityLevelSetRequest) {
    dev.pollito.stonks_java.intensity.domain.IntensityLevel level =
        EnumUtils.fromValue(
            dev.pollito.stonks_java.intensity.domain.IntensityLevel.class,
            intensityLevelSetRequest.getLevel().getValue());
    intensityPortIn.setLevel(level);
    return ResponseEntity.ok(
        new IntensityLevelResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(dev.pollito.stonks_java.generated.model.IntensityLevel.valueOf(level.name())));
  }
}
