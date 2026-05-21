package dev.pollito.stonks_java.chaos.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.chaos.adapter.in.rest.mapper.ChaosRestMapper;
import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity;
import dev.pollito.stonks_java.chaos.domain.ChaosEventType;
import dev.pollito.stonks_java.chaos.domain.ChaosLevel;
import dev.pollito.stonks_java.generated.api.ChaosApi;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggerRequest;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggeredResponse;
import dev.pollito.stonks_java.generated.model.ChaosEventsResponse;
import dev.pollito.stonks_java.generated.model.ChaosLevelResponse;
import dev.pollito.stonks_java.generated.model.ChaosLevelSetRequest;
import dev.pollito.stonks_java.util.enums.EnumUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChaosController implements ChaosApi {

  private final ChaosPortIn chaosPortIn;
  private final ChaosRestMapper mapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<ChaosEventsResponse> getChaosEvents() {
    return ResponseEntity.ok(
        new ChaosEventsResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(chaosPortIn.getHistory().stream().map(mapper::map).toList()));
  }

  @Override
  public ResponseEntity<ChaosEventTriggeredResponse> triggerChaosEvent(
      ChaosEventTriggerRequest req) {
    return ResponseEntity.ok(
        new ChaosEventTriggeredResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(
                mapper.map(
                    chaosPortIn.triggerEvent(
                        ChaosEventType.valueOf(req.getType().getValue()),
                        ChaosEventSeverity.valueOf(req.getSeverity().getValue()),
                        req.getTargetSymbol()))));
  }

  @Override
  public ResponseEntity<ChaosLevelResponse> getChaosLevel() {
    return ResponseEntity.ok(
        new ChaosLevelResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.mapLevel(chaosPortIn.getCurrentLevel())));
  }

  @Override
  public ResponseEntity<ChaosLevelResponse> setChaosLevel(
      ChaosLevelSetRequest chaosLevelSetRequest) {
    ChaosLevel level =
        EnumUtils.fromValue(ChaosLevel.class, chaosLevelSetRequest.getLevel().getValue());
    chaosPortIn.setLevel(level);
    return ResponseEntity.ok(
        new ChaosLevelResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.mapLevel(level)));
  }

  @Override
  public ResponseEntity<ChaosEventsResponse> getChaosHistory() {
    return ResponseEntity.ok(
        new ChaosEventsResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(chaosPortIn.getHistory().stream().map(mapper::map).toList()));
  }
}
