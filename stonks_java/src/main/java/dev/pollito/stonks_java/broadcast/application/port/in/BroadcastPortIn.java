package dev.pollito.stonks_java.broadcast.application.port.in;

import dev.pollito.stonks_java.broadcast.domain.PaperTapeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BroadcastPortIn {
  SseEmitter createEmitter();

  Page<PaperTapeEntry> getPaperTape(Pageable pageable);
}
