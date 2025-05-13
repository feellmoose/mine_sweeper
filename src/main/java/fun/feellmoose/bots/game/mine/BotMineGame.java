package fun.feellmoose.bots.game.mine;

import fun.feellmoose.bots.game.Game;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public interface BotMineGame<T extends BotMineGame<T>> extends Game {

    int steps();

    int mines();

    int width();

    int height();

    Box[][] boxes();

    History[] history();

    GameStatus status();

    Duration duration();

    boolean win();



    record Position(int x, int y) {
        public boolean check(int width, int height) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }
    }

    record History(
            Position position,
            GameOption option,
            LocalDateTime update,
            @Nullable History[] related
    ) {

    }

    interface Serialized<T extends BotMineGame<T>> extends Serializable {
        T deserialize();
    }

    enum GameStatus {
        Init, Running, End
    }

    enum GameOption {
        Click, Flag, Boom
    }

    record Box(
            int value
    ) {

        public static Box num(int value) {
            return new Box(value);
        }

        public static Box mine() {
            return new Box(0X10000);
        }

        public int num() {
            return this.value & 0xFF;
        }

        public Box flagged() {
            if (isFlagged()) return new Box(this.value & ~0x1000);
            else return new Box(this.value | 0x1000);
        }

        public Box clicked() {
            if (isClicked()) return new Box(this.value & ~0x100000);
            else return new Box(this.value | 0x100000);
        }

        public boolean isFlagged() {
            return (this.value & 0X1000) > 0;
        }

        public boolean isClicked() {
            return (this.value & 0X100000) > 0;
        }

        public boolean isMine() {
            return (this.value & 0X10000) > 0;
        }

    }

}
