package fun.feellmoose.computer;

import fun.feellmoose.core.IGame;
import fun.feellmoose.core.Step;

@FunctionalInterface
public interface Algo {
    Step resolve(IGame game);
}