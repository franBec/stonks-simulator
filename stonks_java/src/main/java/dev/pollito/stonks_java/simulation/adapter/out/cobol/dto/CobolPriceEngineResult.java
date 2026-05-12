package dev.pollito.stonks_java.simulation.adapter.out.cobol.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CobolPriceEngineResult(@JsonProperty("newPrice") BigDecimal newPrice) {}
