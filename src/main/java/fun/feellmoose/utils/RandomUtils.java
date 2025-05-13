package fun.feellmoose.utils;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class RandomUtils {

    public static double randomDensity(double min, double max, Function<Double, Double> refactor) {
        double random = ThreadLocalRandom.current().nextDouble(0, 1);
        return max + (max - min) * refactor.apply(random);
    }

    public static String randomString(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
