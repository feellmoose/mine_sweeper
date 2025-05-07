package fun.feellmoose.muti;

import fun.feellmoose.core.Game;

public interface GameRepo {
    void save(Game.SerializedGame game);
    Game.SerializedGame fetch(String gameID);
    void remove(String gameID);
    void shutdown();
}
