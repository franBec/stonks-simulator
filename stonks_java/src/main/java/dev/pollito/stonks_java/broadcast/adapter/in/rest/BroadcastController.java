package dev.pollito.stonks_java.broadcast.adapter.in.rest;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import dev.pollito.stonks_java.broadcast.application.port.in.BroadcastPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class BroadcastController {

  private final BroadcastPortIn broadcastPortIn;

  @GetMapping(value = "/api/stream", produces = TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamEvents() {
    return broadcastPortIn.createEmitter();
  }
}
