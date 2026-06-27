package dev.pollito.stonks_java.chaosevent.adapter.out.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chaosevent_incident_log")
public class ChaoseventIncidentLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "headline", nullable = false, length = 512)
  private String headline;

  @Column(name = "target_symbol", nullable = false, length = 4)
  private String targetSymbol;

  @Column(name = "impact_percent", nullable = false, precision = 10, scale = 2)
  private BigDecimal impactPercent;

  @Column(name = "explanation", length = 2048)
  private String explanation;

  @Column(name = "affected_symbols", length = 2048)
  private String affectedSymbols;

  @Column(name = "source_headline", length = 512)
  private String sourceHeadline;

  @Column(name = "event_type", length = 32)
  private String eventType;

  @Column(name = "event_severity", length = 16)
  private String eventSeverity;

  @Column(name = "occurred_at", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime occurredAt;
}
