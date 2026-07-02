package dev.pollito.stonks_java.intensity.adapter.out.jpa;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

import dev.pollito.stonks_java.intensity.application.port.out.IntensityLevelPortOut;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntensityLevelJpaAdapter implements IntensityLevelPortOut {

  private final IntensityLevelJpaRepository repo;

  @Override
  public Optional<dev.pollito.stonks_java.intensity.domain.IntensityLevel> loadCurrentLevel() {
    return repo.findFirstByOrderByUpdatedAtDesc()
        .map(e -> dev.pollito.stonks_java.intensity.domain.IntensityLevel.valueOf(e.getLevel()));
  }

  @Override
  public void saveLevel(dev.pollito.stonks_java.intensity.domain.IntensityLevel level) {
    repo.deleteAll();
    var entity = new IntensityLevelEntity();
    entity.setLevel(level.name());
    entity.setUpdatedAt(now(UTC));
    repo.save(entity);
  }
}
