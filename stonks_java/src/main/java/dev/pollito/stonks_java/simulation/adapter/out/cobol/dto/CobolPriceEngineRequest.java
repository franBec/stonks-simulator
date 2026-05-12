package dev.pollito.stonks_java.simulation.adapter.out.cobol.dto;

import java.math.BigDecimal;

public record CobolPriceEngineRequest(
    BigDecimal currentPrice, BigDecimal volatility, String trend) {}
