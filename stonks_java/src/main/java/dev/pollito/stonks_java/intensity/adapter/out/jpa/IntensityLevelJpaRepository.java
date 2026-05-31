package dev.pollito.stonks_java.intensity.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.IntensityLevel;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface IntensityLevelJpaRepository extends CrudRepository<IntensityLevel, Long> {
  Optional<IntensityLevel> findFirstByOrderByUpdatedAtDesc();
}
