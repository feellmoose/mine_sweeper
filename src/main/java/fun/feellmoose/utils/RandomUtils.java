package fun.feellmoose.utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class RandomUtils {

    public static double randomDensity(double min, double max, Function<Double, Double> refactor) {
        double random = ThreadLocalRandom.current().nextDouble(0, 1);
        return max + (max - min) * refactor.apply(random);
    }

}
