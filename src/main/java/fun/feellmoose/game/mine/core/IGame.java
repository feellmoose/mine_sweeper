package fun.feellmoose.game.mine.core;

import java.time.Duration;
import java.util.List;

public interface IGame {
    int width();
    int height();
    IUnit[][] units();
    List<Step> steps();
    List<Step> mines();
    List<Step> flags();
    int typed();
    int unknown();
    int last();

    boolean onTyped(int x, int y);

    boolean onFlag(int x, int y);

    Status status();

    boolean isWin();

    Duration time();

    enum Status {
        Init, Running, End
    }
}
