package fun.feellmoose.muti;

import fun.feellmoose.core.Game;

public interface GameTimeMachineRepo extends GameRepo{
    Game.SerializedGame cancel(int steps);
}
