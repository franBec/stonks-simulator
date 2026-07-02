package dev.pollito.stonks_java.intensity.adapter.out.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "intensity_level")
public class IntensityLevelEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "level", nullable = false, length = 32)
  private String level;

  @Column(
      name = "updated_at",
      nullable = false,
      columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
}
