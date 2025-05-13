package fun.feellmoose.game.mine.computer;

import fun.feellmoose.game.mine.core.IGame;
import fun.feellmoose.game.mine.core.Step;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AlgoGame implements Algo {
    private final List<Algo> algoList = new ArrayList<>();
    private Usage usage;

    private AlgoGame() {}

    public static AlgoGame getInstance() {
        return new AlgoGame();
    }

    public AlgoGame registerAlgo(Algo algo){
        algoList.add(algo);
        return this;
    }

    @SneakyThrows
    public void start(IGame game) {
        while (game.status() != IGame.Status.End) {
            Thread.sleep(50);
            Step step = this.resolve(game);
            if (step == null) break;
        }
    }

    public Usage startWithUsage(IGame game) {
        this.usage = Usage.init(algoList.size());
        while (game.status() != IGame.Status.End) {
            Step step = this.resolveWithUsage(game,usage);
            if (step == null) break;
        }
        return usage;
    }

    @Override
    public Step resolve(IGame game) {
        if (game.status() == IGame.Status.End) return null;
        if (game.status() == IGame.Status.Init) {
            Step step = Step.random(game.width(), game.height());
            game.onTyped(step.x(),step.y());
        }
        for (Algo algo : algoList) {
            Step step = algo.resolve(game);
            if (step != null) return step;
        }
        return null;
    }

    private Step resolveWithUsage(IGame game, Usage usage) {
        if (game.status() == IGame.Status.End) return null;
        if (game.status() == IGame.Status.Init) {
            Step step = Step.random(game.width(), game.height());
            game.onTyped(step.x(),step.y());
        }
        for (int i = 0; i < algoList.size(); i++) {
            int finalI = i;
            AtomicReference<Step> step = new AtomicReference<>();
            usage.add(i, () -> step.set(algoList.get(finalI).resolve(game)));
            if (step.get() != null) return step.get();
        }
        return null;
    }

}
