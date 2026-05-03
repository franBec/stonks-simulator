package dev.pollito.stonks_java.market;

/**
 * The {@code market} module is the price engine of the simulator.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Define and manage the catalog of 10 meme stocks (COBL, GMEE, DOGE, etc.)
 *   <li>Store per-stock properties: base price, volatility, trend direction, AI keywords
 *   <li>Run continuous price simulation using random walk with trend bias
 *   <li>Apply circuit-breaker logic after extreme moves (+/- thresholds)
 *   <li>Trigger flash crashes and moon shots as rare events
 * </ul>
 *
 * <p>It is a leaf module in the modulith graph — other modules depend on it, but it does not depend
 * on business modules.
 */
public class MarketPlaceholder {
  public MarketPlaceholder() {
    throw new RuntimeException("boundary placeholder: market module");
  }
}
