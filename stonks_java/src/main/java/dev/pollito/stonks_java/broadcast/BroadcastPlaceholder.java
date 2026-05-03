package dev.pollito.stonks_java.broadcast;

/**
 * The {@code broadcast} module streams real-time updates to the retro terminal frontend.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Expose SSE (Server-Sent Events) endpoints for the React UI
 *   <li>Broadcast price ticks from the {@code market} module
 *   <li>Broadcast chaos events from the {@code chaos} module
 *   <li>Broadcast trade confirmations from the {@code trading} module
 *   <li>Implement "paper tape" transaction logging for the retro aesthetic
 * </ul>
 *
 * <p>This module sits at the top of the dependency graph — it depends on {@code market}, {@code
 * trading}, {@code portfolio}, and {@code chaos} to push updates to connected clients.
 */
public class BroadcastPlaceholder {
  public BroadcastPlaceholder() {
    throw new RuntimeException("boundary placeholder: broadcast module");
  }
}
