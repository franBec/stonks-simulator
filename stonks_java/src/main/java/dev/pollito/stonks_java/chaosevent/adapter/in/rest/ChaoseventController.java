package dev.pollito.stonks_java.chaosevent.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import dev.pollito.stonks_java.chaosevent.adapter.in.rest.mapper.ChaoseventRestMapper;
import dev.pollito.stonks_java.chaosevent.application.port.in.ChaoseventPortIn;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.generated.api.ChaoticEventsApi;
import dev.pollito.stonks_java.generated.model.ChaoticEventTriggerRequest;
import dev.pollito.stonks_java.generated.model.ChaoticEventTriggeredResponse;
import dev.pollito.stonks_java.generated.model.ChaoticEventsResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChaoseventController implements ChaoticEventsApi {

  private final ChaoseventPortIn chaoseventPortIn;
  private final ChaoseventRestMapper mapper;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<ChaoticEventsResponse> getChaoticEvents() {
    return ResponseEntity.ok(
        new ChaoticEventsResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(chaoseventPortIn.getHistory().stream().map(mapper::map).toList()));
  }

  @Override
  public ResponseEntity<ChaoticEventTriggeredResponse> triggerChaoticEvent(
      ChaoticEventTriggerRequest req) {
    return ResponseEntity.ok(
        new ChaoticEventTriggeredResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(
                mapper.map(
                    chaoseventPortIn.triggerEvent(
                        ChaoticEventType.valueOf(req.getType().getValue()),
                        ChaoticEventSeverity.valueOf(req.getSeverity().getValue()),
                        req.getTargetSymbol()))));
  }
}
