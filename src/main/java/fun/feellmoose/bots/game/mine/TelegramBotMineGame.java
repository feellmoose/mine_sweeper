package fun.feellmoose.bots.game.mine;

import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryCommand;
import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.bots.game.GenID;
import fun.feellmoose.exception.GameException;
import fun.feellmoose.i18n.Messages;
import fun.feellmoose.repo.Repo;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor
public class TelegramBotMineGame implements BotMineGame<TelegramBotMineGame>, BotMineGameFunction<TelegramBotMineGame> {
    private final SerializedTelegramBotGame data;

    @Override
    public String id() {
        return data.id();
    }

    @Override
    public int steps() {
        return data.steps();
    }

    @Override
    public int mines() {
        return data.mines();
    }

    @Override
    public int width() {
        return data.width();
    }

    @Override
    public int height() {
        return data.height();
    }

    @Override
    public Box[][] boxes() {
        return data.boxes();
    }

    @Override
    public History[] history() {
        return data.history();
    }

    @Override
    public GameStatus status() {
        return data.status();
    }

    @Override
    public Duration duration() {
        return data.duration();
    }

    @Override
    public boolean win() {
        return data.win();
    }

    public TelegramBotMineGame onAdditionalChanged(Map<String, String> additional) {
        return new TelegramBotMineGame(
                new SerializedTelegramBotGame(
                        data.id, data.userID, additional, data.steps, data.mines, data.width, data.height, data.boxes, data.history, data.win, data.status, data.create, LocalDateTime.now(), data.start, data.end
                )
        );
    }

    @Override
    public TelegramBotMineGame onClicked(Position position) {
        if (!position.check(this.width(), this.height())) return this;
        if (data instanceof SerializedTelegramBotGame(
                String id,
                Long userID,
                Map<String, String> infos,
                int steps,
                int mines,
                int width,
                int height,
                Box[][] boxes,
                History[] history,
                boolean win,
                GameStatus status,
                @NotNull LocalDateTime create,
                @Nullable LocalDateTime update,
                @Nullable LocalDateTime start,
                @Nullable LocalDateTime end
        )) {
            if (status == BotMineGame.GameStatus.UnInit) return this;
            Box box = boxes[position.x()][position.y()];
            if (win || box.isClicked() || box.isFlagged() || status == GameStatus.End) return this;
            History[] current = new History[history.length + 1];
            Box[][] nBoxes = new Box[width][height];
            for (int i = 0; i < width; i++) {
                System.arraycopy(boxes[i], 0, nBoxes[i], 0, height);
            }

            int clicked = 1;
            nBoxes[position.x()][position.y()] = box.clicked();

            System.arraycopy(history, 0, current, 0, history.length);
            LocalDateTime now = LocalDateTime.now();
            if (box.isMine()) {
                current[history.length] = new History(position, GameOption.Boom, now, null);
                return new TelegramBotMineGame(
                        new SerializedTelegramBotGame(
                                id, userID, infos, steps + clicked, mines, width, height, nBoxes, current, false, GameStatus.End, create, now, Optional.ofNullable(start).orElse(now), now
                        )
                );
            } else {
                if (box.num() == 0) {
                    History[] related = clickedZero(width, height, position, boxes);
                    clicked += related.length;
                    current[history.length] = new History(position, GameOption.Click, now, related);
                    for (History h : related) {
                        int x = h.position().x();
                        int y = h.position().y();
                        nBoxes[x][y] = boxes[x][y].clicked();
                    }
                } else {
                    current[history.length] = new History(position, GameOption.Click, now, null);
                }

                if (steps + mines + clicked == width * height) {
                    return new TelegramBotMineGame(
                            new SerializedTelegramBotGame(
                                    id, userID, infos, steps + clicked, mines, width, height, nBoxes, current, true, GameStatus.End, create, now, Optional.ofNullable(start).orElse(now), now
                            )
                    );
                } else {
                    return new TelegramBotMineGame(
                            new SerializedTelegramBotGame(
                                    id, userID, infos, steps + clicked, mines, width, height, nBoxes, current, false, GameStatus.Running, create, now, Optional.ofNullable(start).orElse(now), null
                            )
                    );
                }
            }
        }
        return this;
    }

    private History[] clickedZero(int width, int height, Position from, Box[][] boxes) {
        Set<Position> visited = new HashSet<>();
        List<History> history = new LinkedList<>();
        visited.add(from);
        int x = from.x();
        int y = from.y();
        List<Position> require = Stream.of(
                        new Position(x - 1, y - 1),
                        new Position(x + 1, y - 1),
                        new Position(x - 1, y + 1),
                        new Position(x + 1, y + 1),
                        new Position(x - 1, y),
                        new Position(x, y - 1),
                        new Position(x + 1, y),
                        new Position(x, y + 1)
                ).filter(position -> position.check(width, height))
                .toList();
        while (!require.isEmpty()) {
            List<Position> next = new LinkedList<>();
            for (Position p : require) {
                if (visited.contains(p)) continue;
                Box box = boxes[p.x()][p.y()];
                if (box.isClicked() || box.isFlagged() || box.isMine()) continue;
                history.add(new History(p, GameOption.Click, null, null));
                visited.add(p);
                if (box.num() == 0) {
                    int px = p.x();
                    int py = p.y();
                    next.add(new Position(px - 1, py - 1));
                    next.add(new Position(px + 1, py - 1));
                    next.add(new Position(px - 1, py + 1));
                    next.add(new Position(px + 1, py + 1));
                    next.add(new Position(px - 1, py));
                    next.add(new Position(px, py - 1));
                    next.add(new Position(px + 1, py));
                    next.add(new Position(px, py + 1));
                }
            }
            require = next.stream()
                    .filter(position -> position.check(width, height) && !visited.contains(position))
                    .distinct()
                    .toList();
        }

        return history.toArray(new History[0]);
    }

    @Override
    public TelegramBotMineGame onFlagged(Position position) {
        if (!position.check(this.width(), this.height())) return this;
        if (data instanceof SerializedTelegramBotGame(
                String id,
                Long userID,
                Map<String, String> infos,
                int steps,
                int mines,
                int width,
                int height,
                Box[][] boxes,
                History[] history,
                boolean win,
                GameStatus status,
                @Nullable LocalDateTime create,
                @Nullable LocalDateTime update,
                @Nullable LocalDateTime start,
                @Nullable LocalDateTime end
        )) {
            Box box = boxes[position.x()][position.y()];
            if (status == BotMineGame.GameStatus.UnInit) return this;
            if (win || box.isClicked() || status != GameStatus.Running) return this;

            History[] current = new History[history.length + 1];
            System.arraycopy(history, 0, current, 0, history.length);
            current[history.length] = new History(position, GameOption.Flag, LocalDateTime.now(), null);

            LocalDateTime now = LocalDateTime.now();

            Box[][] nBoxes = new Box[width][height];
            for (int i = 0; i < width; i++) {
                System.arraycopy(boxes[i], 0, nBoxes[i], 0, height);
            }

            nBoxes[position.x()][position.y()] = box.flagged();

            return new TelegramBotMineGame(
                    new SerializedTelegramBotGame(
                            id, userID, infos, steps, mines, width, height, nBoxes, current, false, GameStatus.Running, create, now, start, null
                    )
            );
        }
        return this;
    }

    @Override
    public TelegramBotMineGame onOptioned(History history) {
        return switch (history.option()) {
            case Click -> onClicked(history.position());
            case Flag -> onFlagged(history.position());
            case Boom -> {
                if (data instanceof SerializedTelegramBotGame(
                        String id,
                        Long userID,
                        Map<String, String> infos,
                        int steps,
                        int mines,
                        int width,
                        int height,
                        Box[][] boxes,
                        History[] histories,
                        boolean win,
                        GameStatus status,
                        @Nullable LocalDateTime create,
                        @Nullable LocalDateTime update,
                        @Nullable LocalDateTime start,
                        @Nullable LocalDateTime end
                )) {
                    if (status == BotMineGame.GameStatus.UnInit) yield this;
                    Position position = history.position();
                    Box box = boxes[position.x()][position.y()];
                    if (win || box.isClicked() || status == GameStatus.End) yield this;
                    if (!box.isMine()) yield this;

                    History[] current = new History[histories.length + 1];
                    System.arraycopy(histories, 0, current, 0, histories.length);
                    current[histories.length] = new History(position, GameOption.Boom, LocalDateTime.now(), null);

                    LocalDateTime now = LocalDateTime.now();

                    Box[][] nBoxes = new Box[width][height];
                    for (int i = 0; i < width; i++) {
                        System.arraycopy(boxes[i], 0, nBoxes[i], 0, height);
                    }
                    yield new TelegramBotMineGame(
                            new SerializedTelegramBotGame(
                                    id, userID, infos, steps + 1, mines, width, height, nBoxes, current, false, GameStatus.End, create, now, Optional.ofNullable(start).orElse(now), now
                            )
                    );
                }
                yield this;
            }
        };
    }

    @Override
    public TelegramBotMineGame onRollback(int s) {
        if (data instanceof SerializedTelegramBotGame(
                String id,
                Long userID,
                Map<String, String> infos,
                int steps,
                int mines,
                int width,
                int height,
                Box[][] boxes,
                History[] history,
                boolean win,
                GameStatus status,
                @NotNull LocalDateTime create,
                @Nullable LocalDateTime update,
                @Nullable LocalDateTime start,
                @Nullable LocalDateTime end
        )) {
            if (status == BotMineGame.GameStatus.UnInit) return this;
            LocalDateTime nUpdate;
            History[] current;
            if (history.length > s) {
                current = new History[history.length - s];
                System.arraycopy(history, 0, current, 0, history.length - s);
                nUpdate = history[history.length - s - 1].update();

            } else {
                current = new History[0];
                nUpdate = null;
            }

            Box[][] nBoxes = new Box[width][height];
            for (int i = 0; i < width; i++) {
                System.arraycopy(boxes[i], 0, nBoxes[i], 0, height);
            }

            for (int i = history.length - 1; i >= history.length - s && i > 0; i--) {
                History rollback = history[i];
                Position position = rollback.position();
                switch (rollback.option()) {
                    case Flag -> {
                        Box box = nBoxes[position.x()][position.y()];
                        nBoxes[position.x()][position.y()] = box.flagged();
                    }
                    case Click -> {
                        Box box = nBoxes[position.x()][position.y()];
                        nBoxes[position.x()][position.y()] = Box.num(box.num());
                        if (rollback.related() != null) {
                            for (History h : rollback.related()) {
                                Position related = h.position();
                                Box b = nBoxes[related.x()][related.y()];
                                nBoxes[related.x()][related.y()] = Box.num(b.num());
                            }
                        }
                    }
                    case Boom -> nBoxes[position.x()][position.y()] = Box.mine();
                }
            }

            return new TelegramBotMineGame(
                    new SerializedTelegramBotGame(
                            id, userID, infos, steps, mines, width, height, nBoxes, current, false, GameStatus.Running, create, nUpdate, start, null
                    )
            );
        }
        return this;
    }

    @Override
    public TelegramBotMineGame.SerializedTelegramBotGame serialize() {
        return this.data;
    }


    public record SerializedTelegramBotGame(
            String id,
            Long userID,
            Map<String, String> infos,
            int steps,
            int mines,
            int width,
            int height,
            Box[][] boxes,
            History[] history,
            boolean win,
            GameStatus status,
            @Nullable LocalDateTime create,
            @Nullable LocalDateTime update,
            @Nullable LocalDateTime start,
            @Nullable LocalDateTime end
    ) implements Serialized<TelegramBotMineGame>, Repo.Identified<SerializedTelegramBotGame>, BotMineGame<TelegramBotMineGame> {

        @Override
        public TelegramBotMineGame deserialize() {
            return new TelegramBotMineGame(this);
        }

        @Override
        public Duration duration() {
            return switch (status) {
                case UnInit, Init -> Duration.ZERO;
                case Running -> Duration.between(
                        Optional.ofNullable(start).orElse(LocalDateTime.now()),
                        LocalDateTime.now()
                );
                case End -> Duration.between(
                        Optional.ofNullable(start).orElse(LocalDateTime.now()),
                        Optional.ofNullable(end).orElse(LocalDateTime.now())
                );
            };
        }

    }


    @AllArgsConstructor
    public static class Factory {

        private final GenID genID;


        public TelegramBotMineGame create(int width, int height, int mines, Long userID, Map<String, String> info) throws GameException {
            return start(empty(width, height, mines, userID, info));
        }

        public TelegramBotMineGame empty(int width, int height, int mines, Long userID, Map<String, String> info) throws GameException {
            if (width * height <= mines) throw new GameException("width * height <= mines");
            if (width <= 0 || height <= 0) throw new GameException("width <= 0 || height <= 0");
            if (mines < 0) throw new GameException("mines < 0");
            return new TelegramBotMineGame(new TelegramBotMineGame.SerializedTelegramBotGame(
                    genID.nextID(),
                    userID,
                    info,
                    0,
                    mines,
                    width,
                    height,
                    new Box[0][0],
                    new History[0],
                    false,
                    GameStatus.UnInit,
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            ));
        }


        public TelegramBotMineGame start(TelegramBotMineGame empty) {
            if (empty.status() != GameStatus.UnInit) return empty;
            if (empty.boxes().length == 0) {
                var serialized = empty.data;
                int width = serialized.width;
                int height = serialized.height;

                Box[][] boxes = new Box[width][height];

                List<Integer> nums = IntStream.range(0, width * height)
                        .boxed()
                        .collect(Collectors.toList());
                Collections.shuffle(nums);
                for (int i = 0; i < serialized.mines; i++) {
                    int mine = nums.get(i);
                    boxes[mine / height][mine % height] = Box.mine();
                }

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (boxes[i][j] != null && boxes[i][j].isMine()) continue;
                        int num = (int) Stream.of(
                                        new Position(i + 1, j + 1),
                                        new Position(i + 1, j - 1),
                                        new Position(i - 1, j - 1),
                                        new Position(i - 1, j + 1),
                                        new Position(i + 1, j),
                                        new Position(i - 1, j),
                                        new Position(i, j + 1),
                                        new Position(i, j - 1))
                                .filter(p -> p.check(width, height))
                                .filter(p -> {
                                    Box box = boxes[p.x()][p.y()];
                                    return box != null && box.isMine();
                                }).count();
                        boxes[i][j] = Box.num(num);
                    }
                }

                return new TelegramBotMineGame(new TelegramBotMineGame.SerializedTelegramBotGame(
                        serialized.id,
                        serialized.userID,
                        serialized.infos,
                        0,
                        serialized.mines,
                        serialized.width,
                        serialized.height,
                        boxes,
                        serialized.history,
                        false,
                        BotMineGame.GameStatus.Init,
                        serialized.create,
                        null,
                        LocalDateTime.now(),
                        null
                ));
            }
            return empty;
        }


        public TelegramBotMineGame start(TelegramBotMineGame empty, BotMineGame.Position position) {
            if (empty.status() != GameStatus.UnInit) return empty;
            if (empty.boxes().length == 0) {
                var serialized = empty.data;
                int width = serialized.width;
                int height = serialized.height;

                Box[][] boxes = new Box[width][height];

                List<Integer> nums = IntStream.range(0, width * height)
                        .boxed()
                        .collect(Collectors.toList());

                int x = position.x();
                int y = position.y();

                List<Integer> listA = Stream.of(position,
                                new Position(x + 1, y),
                                new Position(x - 1, y),
                                new Position(x, y + 1),
                                new Position(x, y - 1))
                        .filter(p -> p.check(width, height))
                        .map(p -> p.x() * height + p.y())
                        .toList();
                List<Integer> listB = Stream.of(
                                new Position(x + 1, y + 1),
                                new Position(x + 1, y - 1),
                                new Position(x - 1, y - 1),
                                new Position(x - 1, y + 1))
                        .filter(p -> p.check(width, height))
                        .map(p -> p.x() * height + p.y())
                        .toList();

                if (nums.size() - listA.size() - listB.size() >= serialized.mines) {
                    nums.removeAll(listA);
                    nums.removeAll(listB);
                } else if (nums.size() - listA.size() >= serialized.mines) {
                    nums.removeAll(listA);
                } else {
                    return start(empty).onClicked(position);
                }

                Collections.shuffle(nums);
                for (int i = 0; i < serialized.mines; i++) {
                    int mine = nums.get(i);
                    boxes[mine / height][mine % height] = Box.mine();
                }

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (boxes[i][j] != null && boxes[i][j].isMine()) continue;
                        int num = (int) Stream.of(
                                        new Position(i + 1, j + 1),
                                        new Position(i + 1, j - 1),
                                        new Position(i - 1, j - 1),
                                        new Position(i - 1, j + 1),
                                        new Position(i + 1, j),
                                        new Position(i - 1, j),
                                        new Position(i, j + 1),
                                        new Position(i, j - 1))
                                .filter(p -> p.check(width, height))
                                .filter(p -> {
                                    Box box = boxes[p.x()][p.y()];
                                    return box != null && box.isMine();
                                }).count();
                        boxes[i][j] = Box.num(num);
                    }
                }

                return new TelegramBotMineGame(new TelegramBotMineGame.SerializedTelegramBotGame(
                        serialized.id,
                        serialized.userID,
                        serialized.infos,
                        0,
                        serialized.mines,
                        serialized.width,
                        serialized.height,
                        boxes,
                        serialized.history,
                        false,
                        BotMineGame.GameStatus.Init,
                        serialized.create,
                        null,
                        LocalDateTime.now(),
                        null
                )).onClicked(position);
            }
            return empty;
        }
    }

    public void display(TelegramClient client, TelegramBotMineGameCallbackQueryCommand command) throws TelegramApiException {
        var info = this.serialize();
        var query = command.callbackQuery();
        var data = command.data();
        var additional = TelegramBotMineGameApp.Additional.fromMap(info.infos());
        var message = query.getMessage();

        if (this.win()) {
            client.execute(
                    EditMessageText.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .text(Messages.load("game.mine.win.note", additional.locale()).formatted(
                                    query.getFrom().getUserName(),
                                    this.duration().toSeconds(),
                                    this.duration().toMillisPart(),
                                    this.width(),
                                    this.height(),
                                    this.mines()
                            )).build()
            );
            client.execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboard(List.of())
                                            .build()
                            ).build()
            );
        } else if (this.status() == BotMineGame.GameStatus.End) {
            client.execute(
                    EditMessageText.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .text(Messages.load("game.mine.lose.note", additional.locale()).formatted(
                                    query.getFrom().getUserName(),
                                    this.duration().toSeconds(),
                                    this.duration().toMillisPart(),
                                    this.width(),
                                    this.height(),
                                    this.mines()
                            )).build()
            );
            List<InlineKeyboardRow> keyboard = new ArrayList<>();
            BotMineGame.Box[][] boxes = this.boxes();
            for (int i = 0; i < this.width(); i++) {
                InlineKeyboardRow row = new InlineKeyboardRow();
                for (int j = 0; j < this.height(); j++) {
                    BotMineGame.Box box = boxes[i][j];
                    String callback = new TelegramBotMineGameCallbackQueryData(
                            data.topicID(),
                            this.id(),
                            data.userID(),
                            TelegramBotMineGameCallbackQueryData.Action.none,
                            i, j, 0
                    ).data();
                    if (box.isMine() && box.isClicked()) {
                        row.add(InlineKeyboardButton.builder()
                                .text("\uD83D\uDCA5")
                                .callbackData(callback)
                                .build());
                    } else if (box.isMine()) {
                        row.add(InlineKeyboardButton.builder()
                                .text("\uD83D\uDCA3")
                                .callbackData(callback)
                                .build());
                    } else if (box.isFlagged()) {
                        row.add(InlineKeyboardButton.builder()
                                .text("\uD83D\uDEA9")
                                .callbackData(callback)
                                .build());
                    } else if (box.isClicked()) {
                        row.add(InlineKeyboardButton.builder()
                                .text(String.valueOf(box.num()))
                                .callbackData(callback)
                                .build());
                    } else {
                        row.add(InlineKeyboardButton.builder()
                                .text("ㅤ")
                                .callbackData(callback)
                                .build());
                    }
                }
                keyboard.add(row);
            }
            InlineKeyboardRow row = new InlineKeyboardRow();
            String retry = new TelegramBotMineGameCallbackQueryData(
                    data.topicID(),
                    null,
                    data.userID(),
                    additional.locale().equals(Messages.LOCALE_CN_CXG) ?
                            TelegramBotMineGameCallbackQueryData.Action.cxg :
                            TelegramBotMineGameCallbackQueryData.Action.create,
                    this.width(), this.height(), this.mines()
            ).data();
            row.add(
                    InlineKeyboardButton.builder()
                            .text(Messages.load("game.mine.lose.button", additional.locale()).formatted())
                            .callbackData(retry)
                            .build()
            );
            keyboard.add(row);
            client.execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboard(keyboard)
                                            .build()
                            ).build()
            );
        } else if (this.status() == BotMineGame.GameStatus.UnInit) {
            List<InlineKeyboardRow> keyboard = new ArrayList<>();
            TelegramBotMineGameCallbackQueryData.Action action = switch (additional.button()) {
                case Click -> TelegramBotMineGameCallbackQueryData.Action.dig;
                case Flag -> TelegramBotMineGameCallbackQueryData.Action.flag;
            };
            for (int i = 0; i < this.width(); i++) {
                InlineKeyboardRow row = new InlineKeyboardRow();
                for (int j = 0; j < this.height(); j++) {
                    String callback = new TelegramBotMineGameCallbackQueryData(
                            data.topicID(),
                            this.id(),
                            data.userID(),
                            action,
                            i, j, 0
                    ).data();
                    row.add(InlineKeyboardButton.builder()
                            .text("ㅤ")
                            .callbackData(callback)
                            .build());
                }
                keyboard.add(row);
            }
            InlineKeyboardRow row = new InlineKeyboardRow();
            Messages.MessageString next = switch (additional.button()) {
                case Click -> Messages.load("game.mine.opt.flag", additional.locale());
                case Flag -> Messages.load("game.mine.opt.click", additional.locale());
            };
            String change = new TelegramBotMineGameCallbackQueryData(
                    data.topicID(),
                    this.id(),
                    data.userID(),
                    TelegramBotMineGameCallbackQueryData.Action.change,
                    0, 0, 0
            ).data();
            String quit = new TelegramBotMineGameCallbackQueryData(
                    data.topicID(),
                    this.id(),
                    data.userID(),
                    TelegramBotMineGameCallbackQueryData.Action.quit,
                    0, 0, 0
            ).data();
            row.add(
                    InlineKeyboardButton.builder()
                            .text(next.formatted())
                            .callbackData(change)
                            .build()
            );
            row.add(
                    InlineKeyboardButton.builder()
                            .text(Messages.load("game.mine.opt.quit", additional.locale()).formatted())
                            .callbackData(quit)
                            .build()
            );
            keyboard.add(row);
            client.execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboard(keyboard)
                                            .build()
                            ).build()
            );
        } else {
            List<InlineKeyboardRow> keyboard = new ArrayList<>();
            BotMineGame.Box[][] boxes = this.boxes();
            TelegramBotMineGameCallbackQueryData.Action action = switch (additional.button()) {
                case Click -> TelegramBotMineGameCallbackQueryData.Action.dig;
                case Flag -> TelegramBotMineGameCallbackQueryData.Action.flag;
            };
            for (int i = 0; i < this.width(); i++) {
                InlineKeyboardRow row = new InlineKeyboardRow();
                for (int j = 0; j < this.height(); j++) {
                    BotMineGame.Box box = boxes[i][j];
                    String callback = new TelegramBotMineGameCallbackQueryData(
                            data.topicID(),
                            this.id(),
                            data.userID(),
                            action,
                            i, j, 0
                    ).data();
                    if (box.isFlagged()) {
                        row.add(InlineKeyboardButton.builder()
                                .text("\uD83D\uDEA9")
                                .callbackData(callback)
                                .build());
                    } else if (box.isClicked()) {
                        row.add(InlineKeyboardButton.builder()
                                .text(String.valueOf(box.num()))
                                .callbackData(callback)
                                .build());
                    } else {
                        row.add(InlineKeyboardButton.builder()
                                .text("ㅤ")
                                .callbackData(callback)
                                .build());
                    }
                }
                keyboard.add(row);
            }
            InlineKeyboardRow row = new InlineKeyboardRow();
            Messages.MessageString next = switch (additional.button()) {
                case Click -> Messages.load("game.mine.opt.flag", additional.locale());
                case Flag -> Messages.load("game.mine.opt.click", additional.locale());
            };
            String change = new TelegramBotMineGameCallbackQueryData(
                    data.topicID(),
                    this.id(),
                    data.userID(),
                    TelegramBotMineGameCallbackQueryData.Action.change,
                    0, 0, 0
            ).data();
            String quit = new TelegramBotMineGameCallbackQueryData(
                    data.topicID(),
                    this.id(),
                    data.userID(),
                    TelegramBotMineGameCallbackQueryData.Action.quit,
                    0, 0, 0
            ).data();
            row.add(
                    InlineKeyboardButton.builder()
                            .text(next.formatted())
                            .callbackData(change)
                            .build()
            );
            row.add(
                    InlineKeyboardButton.builder()
                            .text(Messages.load("game.mine.opt.quit", additional.locale()).formatted())
                            .callbackData(quit)
                            .build()
            );
            keyboard.add(row);
            client.execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboard(keyboard)
                                            .build()
                            ).build()
            );
        }
    }

}
