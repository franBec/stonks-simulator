package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public record PaperTapeEntry(
    long sequenceNumber, String formattedLine, OffsetDateTime executedAt) {}
