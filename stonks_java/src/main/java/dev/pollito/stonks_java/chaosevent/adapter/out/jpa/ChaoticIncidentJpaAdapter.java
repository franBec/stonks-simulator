package dev.pollito.stonks_java.chaosevent.adapter.out.jpa;

import static java.util.Collections.reverse;

import dev.pollito.stonks_java.chaosevent.adapter.out.jpa.mapper.ChaoticIncidentJpaMapper;
import dev.pollito.stonks_java.chaosevent.application.port.out.ChaoticIncidentPortOut;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.generated.entity.ChaoseventIncidentLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChaoticIncidentJpaAdapter implements ChaoticIncidentPortOut {

  private final ChaoticIncidentJpaRepository repo;
  private final ChaoticIncidentJpaMapper mapper;

  @Override
  public void recordEvent(ChaoticEvent event) {
    repo.save(mapper.toEntity(event));
  }

  @Override
  public List<ChaoticEvent> loadHistory() {
    List<ChaoseventIncidentLog> entities = repo.findTop100ByOrderByOccurredAtDesc();
    if (entities.isEmpty()) return List.of();
    reverse(entities);
    return entities.stream().map(mapper::toDomain).toList();
  }
}
