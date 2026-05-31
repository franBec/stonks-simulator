package dev.pollito.stonks_java.chaosevent.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.ChaoseventIncidentLog;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ChaoticIncidentJpaRepository extends CrudRepository<ChaoseventIncidentLog, Long> {
  List<ChaoseventIncidentLog> findTop100ByOrderByOccurredAtDesc();
}
