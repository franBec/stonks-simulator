package dev.pollito.stonks_java.broadcast.application.port.in;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BroadcastPortIn {
  SseEmitter createEmitter();
}
