package dev.pollito.stonks_java.util.metadata;

import java.time.OffsetDateTime;

public interface ResponseMetadata {
  String getInstance();

  Integer getStatus();

  OffsetDateTime getTimestamp();

  String getTrace();
}
