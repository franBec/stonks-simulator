package dev.pollito.stonks_java.news.domain;

import java.time.OffsetDateTime;

public record NewsHeadline(
    String title, String source, String category, String url, OffsetDateTime publishedAt) {}
