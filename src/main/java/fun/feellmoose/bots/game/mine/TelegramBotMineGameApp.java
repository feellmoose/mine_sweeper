package fun.feellmoose.bots.game.mine;


import fun.feellmoose.bots.game.GenID;
import fun.feellmoose.game.mine.core.GameException;
import fun.feellmoose.repo.Repo;
import fun.feellmoose.utils.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class TelegramBotMineGameApp {
    private final ReentrantLock lock = new ReentrantLock();
    private final Repo<TelegramBotMineGame.SerializedTelegramBotGame> repo;
    private final TelegramBotMineGame.Factory factory;

    public TelegramBotMineGameApp(Repo<TelegramBotMineGame.SerializedTelegramBotGame> repo) {
        this.repo = repo;
        this.factory = new TelegramBotMineGame.Factory(new GenID.RepoShort(repo));
    }

    public record Additional(
            @NotNull GameType type,
            @NotNull Button button,
            Locale locale,
            Integer topic,
            Long chat,
            Integer message
    ) {
        public enum GameType {
            Classic_Bottom,
            Random_Bottom,
            Level_Bottom,
        }

        public enum Button {
            Click, Flag
        }

        private Map<String, String> toMap() {
            Map<String, String> res = new HashMap<>();
            res.put("type", type.name());
            res.put("button", button.name());
            res.put("locale", LocaleUtils.toString(locale));
            if (topic != null) res.put("topic", topic.toString());
            if (chat != null) res.put("chat", chat.toString());
            if (message != null) res.put("message", message.toString());
            return res;
        }

        public static Additional fromMap(Map<String, String> map) {
            String topic = map.get("topic");
            String chat = map.get("chat");
            String message = map.get("message");
            return new Additional(
                    GameType.valueOf(map.get("type")),
                    Button.valueOf(map.get("button")),
                    LocaleUtils.fromString(map.get("locale")),
                    message == null ? null : Integer.parseInt(message),
                    chat == null ? null : Long.parseLong(chat),
                    topic == null ? null : Integer.parseInt(topic)
            );
        }
    }

    @NotNull
    public TelegramBotMineGame create(Long userID, Additional additional, int width, int height, int num) throws GameException {
        lock.lock();
        try {
            var game = factory.empty(width, height, num, userID, additional.toMap());
            repo.save(game.serialize());
            return game;
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public TelegramBotMineGame query(String gameID) {
        if (gameID != null && !gameID.isEmpty()) {
            var serialized = repo.fetch(gameID);
            if (serialized != null) return serialized.deserialize();
        }
        return null;
    }

    @NotNull
    private TelegramBotMineGame queryNotNull(String gameID) throws GameException {
        if (gameID != null && !gameID.isEmpty()) {
            var serialized = repo.fetch(gameID);
            if (serialized != null) return serialized.deserialize();
        }
        throw new GameException("Games not found");
    }

    @NotNull
    public TelegramBotMineGame change(String gameID) throws GameException {
        var game = queryNotNull(gameID);
        Map<String, String> infos = game.serialize().infos();
        String next = switch (infos.get("button")) {
            case "Click" -> "Flag";
            case null, default -> "Click";
        };
        infos.put("button", next);
        var saved = game.onAdditionalChanged(infos);
        repo.save(saved.serialize());
        return saved;
    }

    @NotNull
    public TelegramBotMineGame dig(String gameID, BotMineGame.Position position) throws GameException {
        TelegramBotMineGame game = queryNotNull(gameID);
        var saved = switch (game.status()) {
            case UnInit -> factory.start(game, position);
            default -> game.onClicked(position);
        };
        repo.save(saved.serialize());
        return saved;
    }

    @NotNull
    public TelegramBotMineGame flag(String gameID, BotMineGame.Position position) throws GameException {
        TelegramBotMineGame game = queryNotNull(gameID);
        if (game.status() == BotMineGame.GameStatus.UnInit) return game;
        var saved = game.onFlagged(position);
        repo.save(saved.serialize());
        return saved;
    }

    @NotNull
    public TelegramBotMineGame rollback(String gameID, int n) throws GameException {
        TelegramBotMineGame game = queryNotNull(gameID);
        if (game.status() == BotMineGame.GameStatus.UnInit) return game;
        var saved = game.onRollback(n);
        repo.save(saved.serialize());
        return saved;
    }

    public void quit(String gameID) throws GameException {
        TelegramBotMineGame game =query(gameID);
        if (game != null) repo.remove(game.id());
    }
}
