package fun.feellmoose.muti;

import fun.feellmoose.core.Game;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.*;

public class MemoryGameRepo implements GameRepo {

    private final ConcurrentHashMap<String, DelayedGame> games = new ConcurrentHashMap<>();
    private final DelayQueue<DelayedGame> queue = new DelayQueue<>();
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final long ttl;

    public MemoryGameRepo(long ttl) {
        this.ttl = ttl;
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    DelayedGame delayed = queue.take();
                    String gameID = delayed.game.gameID();
                    DelayedGame game = games.get(gameID);
                    if (game != null && game.equals(delayed)) {
                        games.remove(gameID);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public MemoryGameRepo() {
        this(Duration.ofDays(1).toMillis());
    }

    private record DelayedGame(Game.SerializedGame game, Duration delay) implements Delayed {
        private static final TimeUnit TIME_UNIT = TimeUnit.NANOSECONDS;

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(delay);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return Long.compare(TIME_UNIT.convert(delay), o.getDelay(TIME_UNIT));
        }
    }

    @Override
    public void shutdown(){
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Force shutdown
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
    }

    @Override
    public void save(Game.SerializedGame game) {
        games.compute(game.gameID(), (key, value) -> {
            var next = new DelayedGame(game, Duration.ofMillis(ttl));
            queue.add(next);
            return next;
        });
    }

    @Override
    public Game.SerializedGame fetch(String gameID) {
        return games.get(gameID).game;
    }

    @Override
    public void remove(String gameID) {
        games.remove(gameID);
    }
}
