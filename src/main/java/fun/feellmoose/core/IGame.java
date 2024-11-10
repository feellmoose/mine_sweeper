package fun.feellmoose.core;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

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

    boolean onEnd(Consumer<Boolean> consumer);

    Duration time();

    enum Status {
        Init, Running, End
    }
}
