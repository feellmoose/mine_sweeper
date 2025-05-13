package fun.feellmoose.game.mine.computer;

import fun.feellmoose.game.mine.core.IGame;
import fun.feellmoose.game.mine.core.Step;

@FunctionalInterface
public interface Algo {
    Step resolve(IGame game);
}