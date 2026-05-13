package dev.pollito.stonks_java.stock.adapter.out.cobol.dto;

import java.math.BigDecimal;

public record CobolPriceEngineRequest(
    BigDecimal currentPrice, BigDecimal volatility, String trend) {}
