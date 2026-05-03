package dev.pollito.stonks_java.trading;

/**
 * The {@code trading} module handles the order flow and execution pipeline.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Accept and validate buy/sell orders
 *   <li>Enforce "manager approval" workflow for large trades
 *   <li>Maintain order book and trade history
 *   <li>Orchestrate trade execution with the {@code cobol} legacy engine
 *   <li>Update the {@code portfolio} module after successful trades
 * </ul>
 *
 * <p>This module depends on {@code market} (for current prices), {@code portfolio} (for holdings
 * and balance), and {@code cobol} (for legacy validation).
 */
public class TradingPlaceholder {
  public TradingPlaceholder() {
    throw new RuntimeException("boundary placeholder: trading module");
  }
}
