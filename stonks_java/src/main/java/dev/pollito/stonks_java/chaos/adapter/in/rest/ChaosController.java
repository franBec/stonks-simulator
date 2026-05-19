package dev.pollito.stonks_java.chaos.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.chaos.adapter.in.rest.mapper.ChaosRestMapper;
import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.generated.api.ChaosApi;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggerRequest;
import dev.pollito.stonks_java.generated.model.ChaosEventTriggeredResponse;
import dev.pollito.stonks_java.generated.model.ChaosEventsResponse;
import dev.pollito.stonks_java.generated.model.ChaosLevelResponse;
import dev.pollito.stonks_java.util.enums.EnumUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
    List<dev.pollito.stonks_java.generated.model.ChaosEvent> events =
        chaosPortIn.getHistory().stream().map(mapper::map).toList();
    return ResponseEntity.ok(
        new ChaosEventsResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(events));
  }

  @Override
  public ResponseEntity<ChaosEventTriggeredResponse> triggerChaosEvent(
      ChaosEventTriggerRequest chaosEventTriggerRequest) {
    dev.pollito.stonks_java.chaos.domain.ChaosEvent domainEvent = chaosPortIn.triggerEvent();
    return ResponseEntity.ok(
        new ChaosEventTriggeredResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.map(domainEvent)));
  }

  @Override
  public ResponseEntity<ChaosLevelResponse> getChaosLevel() {
    dev.pollito.stonks_java.chaos.domain.ChaosLevel level = chaosPortIn.getCurrentLevel();
    return ResponseEntity.ok(
        new ChaosLevelResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.mapLevel(level)));
  }

  @Override
  public ResponseEntity<ChaosLevelResponse> setChaosLevel(String body) {
    dev.pollito.stonks_java.chaos.domain.ChaosLevel level =
        EnumUtils.fromValue(dev.pollito.stonks_java.chaos.domain.ChaosLevel.class, body);
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
    List<dev.pollito.stonks_java.generated.model.ChaosEvent> events =
        chaosPortIn.getHistory().stream().map(mapper::map).toList();
    return ResponseEntity.ok(
        new ChaosEventsResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(events));
  }
}
