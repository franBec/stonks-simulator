package dev.pollito.stonks_java.broadcast.adapter.in.rest;

import static io.opentelemetry.api.trace.Span.current;
import static java.time.OffsetDateTime.now;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import dev.pollito.stonks_java.broadcast.adapter.in.rest.mapper.BroadcastRestMapper;
import dev.pollito.stonks_java.broadcast.application.port.in.BroadcastPortIn;
import dev.pollito.stonks_java.generated.api.BroadcastApi;
import dev.pollito.stonks_java.generated.model.PaperTapeResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class BroadcastController implements BroadcastApi {

  private final BroadcastPortIn broadcastPortIn;
  private final BroadcastRestMapper mapper;
  private final HttpServletRequest request;

  @GetMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamEvents() {
    return broadcastPortIn.createEmitter();
  }

  @Override
  public ResponseEntity<PaperTapeResponse> getPaperTape(Pageable pageable) {
    return ResponseEntity.ok(
        new PaperTapeResponse()
            .instance(request.getRequestURI())
            .status(OK.value())
            .timestamp(now())
            .trace(current().getSpanContext().getTraceId())
            .data(mapper.toPaperTapeResponseData(broadcastPortIn.getPaperTape(pageable))));
  }
}
