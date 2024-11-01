package fun.feellmoose.computer;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.Step;

@FunctionalInterface
    public interface Algo {
        Step resolve(Game game);
    }