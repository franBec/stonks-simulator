package dev.pollito.stonks_java.portfolio.domain;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummary(
    BigDecimal cashBalance, List<PositionSummary> positions, BigDecimal unrealizedPnl) {}
