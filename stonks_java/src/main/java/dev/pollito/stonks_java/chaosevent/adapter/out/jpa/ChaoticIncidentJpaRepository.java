package dev.pollito.stonks_java.chaosevent.adapter.out.jpa;

import dev.pollito.stonks_java.chaosevent.adapter.out.jpa.ChaoseventIncidentLogEntity;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ChaoticIncidentJpaRepository extends CrudRepository<ChaoseventIncidentLogEntity, Long> {
  List<ChaoseventIncidentLogEntity> findTop100ByOrderByOccurredAtDesc();
}
