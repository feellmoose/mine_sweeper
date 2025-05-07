package fun.feellmoose.muti;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.GameException;
import fun.feellmoose.core.Step;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SinglePlayerGameManager {
    private final ReentrantLock lock = new ReentrantLock();
    private final GameRepo repo;
    private final ConcurrentHashMap<String, String> startedGames = new ConcurrentHashMap<>();

    public SinglePlayerGameManager(GameRepo repo) {
        this.repo = repo;
    }

    @NotNull
    public Game.SerializedGame create(String userID, int width, int height, int num) throws GameException {
        lock.lock();
        try {
            var game = Game.init(width, height, num).serialized();
            repo.save(game);
            startedGames.put(userID, game.gameID());
            return game;
        } finally {
            lock.unlock();
        }
    }

    @NotNull
    public Game.SerializedGame query(String userID) throws GameException {
        String gameID = startedGames.get(userID);
        if (gameID == null) throw new GameException("Games created bt " + userID + " not found");
        var game = repo.fetch(gameID);
        if (game == null) throw new GameException(gameID + " not found");
        return game;
    }

    @NotNull
    public Game.SerializedGame dig(String userID, Step step) throws GameException {
        Game game = query(userID).deserialize();
        if (!game.onTyped(step.x(), step.y())) throw new GameException(step + " is not a valid step");
        var update = game.serialized();
        repo.save(update);
        return update;
    }

    @NotNull
    public Game.SerializedGame flag(String userID, Step step) throws GameException {
        Game game = query(userID).deserialize();
        if (!game.onFlag(step.x(), step.y()))
            throw new GameException(step + " is not valid for planting or cancelling a flag");
        var update = game.serialized();
        repo.save(update);
        return update;
    }

    public void quit(String userID) throws GameException {
        String gameID = startedGames.get(userID);
        if (gameID == null) throw new GameException("Games created bt " + userID + " not found");
        repo.remove(gameID);
        startedGames.remove(userID);
    }

}
