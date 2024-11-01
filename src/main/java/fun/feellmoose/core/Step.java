package fun.feellmoose.core;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public record Step(int x, int y) {
    public static Step of(int x, int y) {
        return new Step(x, y);
    }

    public static Step random(int width, int height) {
        Random random = ThreadLocalRandom.current();
        return new Step(random.nextInt(width - 1), random.nextInt(height - 1));
    }

    public static Step randomX(int width, int y) {
        Random random = ThreadLocalRandom.current();
        return new Step(random.nextInt(width - 1), y);
    }

    public static Step randomY(int x, int height) {
        Random random = ThreadLocalRandom.current();
        return new Step(x, random.nextInt(height - 1));
    }

}
