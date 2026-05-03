package dev.pollito.stonks_java.portfolio;

/**
 * The {@code portfolio} module tracks the player's financial state.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Track the $10,000 play-money balance
 *   <li>Record stock holdings / positions per symbol
 *   <li>Calculate real-time P&L based on current market prices
 *   <li>Manage session state for the single anonymous user
 * </ul>
 *
 * <p>This module depends on {@code market} to compute current valuations. It is updated by the
 * {@code trading} module after each executed trade.
 */
public class PortfolioPlaceholder {
  public PortfolioPlaceholder() {
    throw new RuntimeException("boundary placeholder: portfolio module");
  }
}
