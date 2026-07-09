package dev.pollito.stonks_java.config;

import dev.pollito.stonks_java.config.GameStatus;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class GameStateService {

  private final AtomicReference<GameStatus> status =
      new AtomicReference<>(GameStatus.PLAYING);

  public boolean isPlaying() {
    return status.get() == GameStatus.PLAYING;
  }

  public void markWon() {
    status.compareAndSet(GameStatus.PLAYING, GameStatus.WON);
  }

  public void markLost() {
    status.compareAndSet(GameStatus.PLAYING, GameStatus.LOST);
  }

  public void reset() {
    status.set(GameStatus.PLAYING);
  }

  public GameStatus getStatus() {
    return status.get();
  }
}
