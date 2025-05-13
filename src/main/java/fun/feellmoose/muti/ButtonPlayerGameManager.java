package fun.feellmoose.muti;

import fun.feellmoose.game.mine.core.Game;
import fun.feellmoose.game.mine.core.GameException;
import fun.feellmoose.game.mine.core.IGame;
import fun.feellmoose.game.mine.core.Step;
import fun.feellmoose.muti.repo.Repo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class ButtonPlayerGameManager {
    private final ReentrantLock lock = new ReentrantLock();
    private final Repo<Game.SerializedGame> repo;

    public static String random(int length) {
        Random random = ThreadLocalRandom.current();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public ButtonPlayerGameManager(Repo<Game.SerializedGame> repo) {
        this.repo = repo;
    }

    @NotNull
    public Game.SerializedGame create(int width, int height, int num) throws GameException {
        lock.lock();
        try {
            var game = Game.fake(width, height, num).serialized();
            int length = 8;
            String shortGameID = random(length);
            while (repo.fetch(shortGameID) != null) {
                length++;
                shortGameID = random(length);
            }
            Game.SerializedGame saved = new Game.SerializedGame(
                    shortGameID,
                    game.width(),
                    game.height(),
                    game.sum(),
                    game.units(),
                    game.steps(),
                    game.mines(),
                    game.flags(),
                    game.currentStepFlag(),
                    game.status(),
                    game.typed(),
                    game.start(),
                    game.duration()
            );
            repo.save(saved);
            return saved;
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public Game.SerializedGame query(String gameID) {
        if (gameID != null && !gameID.isEmpty()) return repo.fetch(gameID);
        return null;
    }

    @NotNull
    private Game.SerializedGame queryNotNull(String gameID) throws GameException {
        if (gameID != null && !gameID.isEmpty()){
            Game.SerializedGame game = repo.fetch(gameID);
            if (game != null) return game;
        }
        throw new GameException("Games not found");
    }

    @NotNull
    public Game.SerializedGame change(String gameID) throws GameException {
        Game.SerializedGame game = queryNotNull(gameID);
        Game.SerializedGame update = new Game.SerializedGame(
                game.gameID(),
                game.width(),
                game.height(),
                game.sum(),
                game.units(),
                game.steps(),
                game.mines(),
                game.flags(),
                !game.currentStepFlag(),
                game.status(),
                game.typed(),
                game.start(),
                game.duration()
        );
        repo.save(update);
        return update;
    }

    @NotNull
    public Game.SerializedGame dig(String gameID, Step step) throws GameException {
        Game game = queryNotNull(gameID).deserialize();
        if (game.status() == IGame.Status.Init) {
            game = Game.initWithClick(game, step.x(), step.y());
        } else {
            game.onTyped(step.x(), step.y());
        }
        var update = game.serialized();
        repo.save(update);
        return update;
    }

    @NotNull
    public Game.SerializedGame flag(String gameID, Step step) throws GameException {
        Game game = queryNotNull(gameID).deserialize();
        game.onFlag(step.x(), step.y());
        var update = game.serialized();
        repo.save(update);
        return update;
    }

    public void quit(String gameID) throws GameException {
        if (gameID != null) {
            repo.remove(gameID);
        }
    }

}
