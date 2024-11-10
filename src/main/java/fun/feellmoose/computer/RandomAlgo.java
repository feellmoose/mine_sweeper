package fun.feellmoose.computer;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.IGame;
import fun.feellmoose.core.IUnit;
import fun.feellmoose.core.Step;

public class RandomAlgo implements Algo {
    @Override
    public Step resolve(IGame game) {
        Step random = Step.random(game.width(), game.height());
        while (game.units()[random.x()][random.y()].status() == IUnit.Status.Typed) {
            random = Step.random(game.width(), game.height());
        }
        game.onTyped(random.x(), random.y());
        return random;
    }
}
