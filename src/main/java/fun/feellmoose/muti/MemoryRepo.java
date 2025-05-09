package fun.feellmoose.muti;

import com.sun.source.tree.IdentifierTree;
import fun.feellmoose.core.Game;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.*;

public class MemoryRepo<T extends Repo.Identified<T>> implements Repo<T> {
    private final ConcurrentHashMap<String, DelayedObj<T>> saver = new ConcurrentHashMap<>();
    private final DelayQueue<DelayedObj<T>> queue = new DelayQueue<>();
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final long ttl;



    public MemoryRepo(long ttl) {
        this.ttl = ttl;
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    DelayedObj<T> delayed = queue.take();
                    String gameID = delayed.obj.getId();
                    DelayedObj<T> game = saver.get(gameID);
                    if (game != null && game.equals(delayed)) {
                        saver.remove(gameID);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public MemoryRepo() {
        this(Duration.ofDays(1).toMillis());
    }

    private record DelayedObj<T>(T obj, Duration delay) implements Delayed {
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
    public void save(T obj) {
        saver.compute(obj.getId(), (key, value) -> {
            var next = new MemoryRepo.DelayedObj<>(obj, Duration.ofMillis(ttl));
            queue.add(next);
            return next;
        });
    }

    @Override
    public T fetch(String gameID) {
        return saver.get(gameID).obj;
    }

    @Override
    public void remove(String gameID) {
        saver.remove(gameID);
    }
}
