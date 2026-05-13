package dev.pollito.stonks_java.stock.adapter.out.cobol.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CobolCatalogStock(
    @JsonProperty("symbol") String symbol,
    @JsonProperty("name") String name,
    @JsonProperty("basePrice") BigDecimal basePrice,
    @JsonProperty("volatility") BigDecimal volatility,
    @JsonProperty("trend") String trend) {}
