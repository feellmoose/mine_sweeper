package fun.feellmoose.muti;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.GameException;
import fun.feellmoose.core.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

public class SinglePlayerGameManager {
    private final ReentrantLock lock = new ReentrantLock();
    private final Repo<Game.SerializedGame> repo;
    private final Repo<AdditionalGameInfo> additionalRepo;

    public record AdditionalGameInfo(
            String gameID,
            GameType type,
            String userID,
            String chatID,
            String messageID) implements Repo.Identified<AdditionalGameInfo> {
        @Override
        public String getId() {
            return switch (type){
                case button -> "u%s_c%s_m%s".formatted(userID, chatID, messageID);
                case picture -> "u%s_c%s".formatted(userID, chatID);
            };
        }
    }

    enum GameType{
        button, picture
    }

    public SinglePlayerGameManager(Repo<Game.SerializedGame> repo, Repo<AdditionalGameInfo> additionalRepo) {
        this.repo = repo;
        this.additionalRepo = additionalRepo;
    }

    @NotNull
    public Game.SerializedGame create(String userID, String chatID, String messageID, int width, int height, int num) throws GameException {
        lock.lock();
        try {
            var game = Game.init(width, height, num).serialized();
            AdditionalGameInfo gameInfo = new AdditionalGameInfo(
                    game.gameID(),
                    game.width() > 8 || game.height() > 8 ? GameType.picture : GameType.button,
                    userID,
                    chatID,
                    messageID
            );
            repo.save(game);
            additionalRepo.save(gameInfo);
            return game;
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public Game.SerializedGame query(String userID, String chatID, String messageID, String gameID) {
        if (gameID != null) return repo.fetch(gameID);
        AdditionalGameInfo additionalGameInfo;
        if (userID != null && chatID != null && messageID != null) {
            additionalGameInfo = additionalRepo.fetch("u%s_c%s_m%s".formatted(userID, chatID, messageID));
            if (additionalGameInfo != null) return repo.fetch(additionalGameInfo.gameID);
        }
        if (userID != null && chatID != null) {
            additionalGameInfo = additionalRepo.fetch("u%s_c%s".formatted(userID, chatID));
            if (additionalGameInfo != null) return repo.fetch(additionalGameInfo.gameID);
        }
        return null;
    }

    @NotNull
    private Game.SerializedGame queryNotNull(String userID, String chatID, String messageID, String gameID) throws GameException {
        Game.SerializedGame game = null;
        if (gameID != null) {
            game = repo.fetch(gameID);
            if (game != null) return game;
        }
        AdditionalGameInfo additionalGameInfo;
        if (userID != null && chatID != null && messageID != null) {
            additionalGameInfo = additionalRepo.fetch("u%s_c%s_m%s".formatted(userID, chatID, messageID));
            if (additionalGameInfo != null) game = repo.fetch(additionalGameInfo.gameID);
            if (game != null) return game;
        }
        if (userID != null && chatID != null) {
            additionalGameInfo = additionalRepo.fetch("u%s_c%s".formatted(userID, chatID));
            if (additionalGameInfo != null)  game = repo.fetch(additionalGameInfo.gameID);
            if (game != null) return game;
        }
        throw new GameException("Games not found");
    }

    @NotNull
    public Game.SerializedGame change(String userID, String chatID, String messageID, String gameID) throws GameException {
        Game.SerializedGame game = queryNotNull(userID,chatID,messageID,gameID);
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
    public Game.SerializedGame dig(String userID, String chatID, String messageID, String gameID, Step step) throws GameException {
        Game game = queryNotNull(userID,chatID,messageID,gameID).deserialize();
        if (!game.onTyped(step.x(), step.y()))
            throw new GameException("(" + step.x() + "," + step.y() + ")" + " is not a valid step");
        var update = game.serialized();
        repo.save(update);
        return update;
    }

    @NotNull
    public Game.SerializedGame flag(String userID, String chatID, String messageID, String gameID, Step step) throws GameException {
        Game game = queryNotNull(userID,chatID,messageID,gameID).deserialize();
        if (!game.onFlag(step.x(), step.y()))
            throw new GameException("(" + step.x() + "," + step.y() + ")" + " is not valid for planting or cancelling a flag");
        var update = game.serialized();
        repo.save(update);
        return update;
    }

    public void quit(String userID, String chatID, String messageID, String gameID) throws GameException {
        if (userID != null && chatID != null && messageID != null) {
            additionalRepo.remove("u%s_c%s_m%s".formatted(userID, chatID, messageID));
        } else if (userID != null && chatID != null) {
            additionalRepo.remove("u%s_c%s".formatted(userID, chatID));
        } else if (gameID != null) {
            repo.remove(gameID);
        }
    }

}
