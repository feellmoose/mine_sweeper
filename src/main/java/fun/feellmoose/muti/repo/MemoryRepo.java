package fun.feellmoose.muti.repo;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
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
                    String id = delayed.obj.id();
                    DelayedObj<T> obj = saver.get(id);
                    if (obj != null && obj.equals(delayed)) {
                        saver.remove(id);
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
        saver.compute(Objects.requireNonNull(obj.id()), (key, value) -> {
            var next = new MemoryRepo.DelayedObj<>(obj, Duration.ofMillis(ttl));
            queue.add(next);
            return next;
        });
    }

    @Override
    public T fetch(String id) {
        DelayedObj<T> delayed = saver.get(id);
        if (delayed == null) return null;
        return delayed.obj;
    }

    @Override
    public void remove(String id) {
        saver.remove(id);
    }
}
