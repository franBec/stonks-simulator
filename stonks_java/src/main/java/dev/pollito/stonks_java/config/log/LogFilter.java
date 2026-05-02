package dev.pollito.stonks_java.config.log;

import static java.util.Collections.list;
import static java.util.Map.entry;
import static java.util.stream.Collectors.joining;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order()
@Slf4j
public class LogFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    logRequestDetails(request);
    filterChain.doFilter(request, response);
    logResponseDetails(response);
  }

  private void logRequestDetails(@NonNull HttpServletRequest request) {
    log.info(
        ">>>> Method: {}; URI: {}; QueryString: {}; Headers: {}",
        request.getMethod(),
        request.getRequestURI(),
        request.getQueryString(),
        headersToString(request));
  }

  private String headersToString(@NonNull HttpServletRequest request) {
    return list(request.getHeaderNames()).stream()
        .filter(name -> name != null && !name.isBlank())
        .map(name -> entry(name, request.getHeader(name)))
        .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
        .map(entry -> entry.getKey() + ": " + entry.getValue())
        .collect(joining(", ", "{", "}"));
  }

  private void logResponseDetails(@NonNull HttpServletResponse response) {
    log.info("<<<< Response Status: {}", response.getStatus());
  }
}
