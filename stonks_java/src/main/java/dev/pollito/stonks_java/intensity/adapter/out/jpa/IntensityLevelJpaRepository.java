package dev.pollito.stonks_java.intensity.adapter.out.jpa;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface IntensityLevelJpaRepository extends CrudRepository<IntensityLevelEntity, Long> {
  Optional<IntensityLevelEntity> findFirstByOrderByUpdatedAtDesc();
}
