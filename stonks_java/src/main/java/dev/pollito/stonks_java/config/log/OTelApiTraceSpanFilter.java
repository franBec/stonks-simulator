package dev.pollito.stonks_java.config.log;

import static io.opentelemetry.api.trace.Span.current;
import static org.slf4j.MDC.put;
import static org.slf4j.MDC.remove;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(LOWEST_PRECEDENCE - 1)
public class OTelApiTraceSpanFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    SpanContext spanContext = current().getSpanContext();
    if (spanContext.isValid()) {
      put("trace_id", spanContext.getTraceId());
      put("span_id", spanContext.getSpanId());
      put("trace_flags", spanContext.getTraceFlags().isSampled() ? "01" : "00");
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      remove("trace_id");
      remove("span_id");
      remove("trace_flags");
    }
  }
}
