package fun.feellmoose.game.mine.computer;

import fun.feellmoose.game.mine.core.IGame;
import fun.feellmoose.game.mine.core.IUnit;
import fun.feellmoose.game.mine.core.Step;

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
