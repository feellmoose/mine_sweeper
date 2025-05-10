package fun.feellmoose.gui.tgbot.handle.common;

import fun.feellmoose.core.*;
import fun.feellmoose.gui.tgbot.command.InnerBotCommand;
import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.command.data.ButtonQueryDataText;
import fun.feellmoose.muti.SinglePlayerGameManager;
import kotlin.random.Random;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

@Slf4j
public class SinglePlayerSweeperGameCommandHandler implements InnerBotCommandHandler {

    private final SinglePlayerGameManager gameManager;
    private final TelegramClient client;
    private final Displayer display;

    public SinglePlayerSweeperGameCommandHandler(SinglePlayerGameManager gameManager, TelegramClient client) {
        this.gameManager = gameManager;
        this.client = client;
        this.display = new Displayer(client);
    }

    @Override
    public void handle(InnerBotCommand command) {
        if (command instanceof SinglePlayerSweeperGameCommand(
                SinglePlayerSweeperGameCommand.Type type,
                String[] args,
                Message message,
                String gameID
        )) {
            log.debug("Received command: {}", command);

            User user = message.getFrom();
            String userID = user.getId().toString();
            String username = user.getUserName();
            String chatID = message.getChatId().toString();
            String messageID = message.getMessageId().toString();
            Integer threadID = message.getMessageThreadId();

            try {
                try {
                    switch (type) {
                        case create -> create(args, userID, username, chatID, messageID, threadID);
                        case dig -> dig(args, userID, username, chatID, messageID, gameID, threadID);
                        case flag -> flag(args, userID, username, chatID, messageID, gameID, threadID);
                        case quit -> quit(args, userID, username, chatID, messageID, gameID, threadID);
                        case help -> help(args, userID, username, chatID, threadID);
                    }
                } catch (GameException e) {
                    client.execute(
                            SendMessage.builder()
                                    .chatId(chatID)
                                    .messageThreadId(threadID)
                                    .text("""
                                            @%s Oops! Something went wrong!
                                            %s
                                            """.formatted(username, e.getMessage()))
                                    .build()
                    );
                }
            } catch (TelegramApiException apiException) {
                log.error("Error sending message", apiException);
            }

        }
    }

    private void create(String[] args, String userID, String username, String chatID, String messageID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Creating game... {}", Arrays.asList(args));
        if (gameManager.query(userID, chatID, null, null) != null) {
            client.execute(
                    SendMessage.builder()
                            .chatId(chatID)
                            .text("""
                                    @%s
                                    🎮 A game is currently active in this chat!
                                    To end it early, type '/quit' or '/admin quit'.
                                    """.formatted(username))
                            .build()
            );
        }
        switch (args.length) {
            case 0 -> {
                //send create guide for user
                var row = new InlineKeyboardRow();
                row.add(
                        InlineKeyboardButton.builder()
                                .text("Started With Button")
                                .callbackData(new ButtonQueryDataText(
                                        threadID, Long.valueOf(userID), null, "create", 8, 8, 10
                                ).getData())
                                .build()
                );
                client.executeAsync(
                        SendMessage.builder()
                                .chatId(chatID)
                                .messageThreadId(threadID)
                                .text("""
                                        @%s
                                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                        Ready to play? Just follow the steps below to start a new game.
                                        """.formatted(username))
                                .replyMarkup(InlineKeyboardMarkup.builder()
                                        .keyboard(List.of(
                                                row
                                        )).build())
                                .build()
                );
            }
            case 1 -> {
                if (args[0].equals("random")) {
                    int length = Random.Default.nextInt(1, 52);
                    double random = Random.Default.nextDouble(0, 1);
                    //filter to limit num smaller
                    int num = (int) (Math.pow(random, 10) * Math.pow(length, 2) / 2);
                    Game.SerializedGame game = gameManager.create(userID, chatID, messageID, length, length, num);
                    display.display(game, userID, username, chatID, messageID,threadID);
                }
            }
            case 2 -> {
                if (args[0].equals("level")) {
                    Game.SerializedGame game;
                    switch (args[1]) {
                        case "easy" -> game = gameManager.create(userID, chatID, messageID, 9, 9, 10);
                        case "normal" -> game = gameManager.create(userID, chatID, messageID, 16, 16, 40);
                        case "hard" -> game = gameManager.create(userID, chatID, messageID, 25, 25, 99);
                        default -> throw new GameException("Game level should be 'easy', 'normal', or 'hard'");
                    }
                    display.display(game, userID, username, chatID, messageID,threadID);
                }
            }
            case 3 -> {
                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int mine = Integer.parseInt(args[2]);
                    if (x < 0 || y < 0 || mine < 0 || x > 52 || y > 52 || mine > 52)
                        throw new GameException("x, y and mine num should be between 0 and 52.");
                    Game.SerializedGame game = gameManager.create(userID, chatID, messageID, x, y, mine);
                    display.display(game, userID, username, chatID, messageID,threadID);
                } catch (NumberFormatException e) {
                    throw new GameException("x, y and mine num should be a number.");
                }
            }
            default -> throw new GameException("Command args too long.");
        }
    }

    private void dig(String[] args, String userID, String username, String chatID, String messageID, String gameID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Dig option... {}", Arrays.asList(args));
        if (args.length != 2) throw new GameException("Command args length has to be 2.");
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Game.SerializedGame game = gameManager.dig(userID, chatID, messageID, gameID, new Step(x, y));
            display.display(game, userID, username, chatID, messageID,threadID);
            if (game.isWin()) gameManager.quit(userID, chatID, messageID, gameID);
        } catch (NumberFormatException e) {
            throw new GameException("x, y and mine num should be a number.");
        }
    }

    private void flag(String[] args, String userID, String username, String chatID, String messageID, String gameID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Flag option... {}", Arrays.asList(args));
        if (args.length != 2) throw new GameException("Command args length has to be 2.");
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Game.SerializedGame game = gameManager.flag(userID, chatID, messageID, gameID, new Step(x, y));
            display.display(game, userID, username, chatID, messageID,threadID);
        } catch (NumberFormatException e) {
            throw new GameException("x, y and mine num should be a number.");
        }
    }

    private void quit(String[] args, String userID, String username, String chatID, String messageID, String gameID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Quit option... {}", Arrays.asList(args));
        gameManager.quit(userID, chatID, messageID, gameID);
        String str = "@" + username + " quit game success!";
        client.executeAsync(SendMessage.builder()
                .chatId(chatID)
                .messageThreadId(threadID)
                .text(str)
                .build());
    }

    private void help(String[] args, String userID, String username, String chatID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Help option... {}", Arrays.asList(args));
        client.executeAsync(SendMessage.builder()
                .chatId(chatID)
                .messageThreadId(threadID)
                .text("""
                        @%s
                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                        Here's a list of commands to get you started:
                        
                        /create Create a new single person game with guide.
                        /create <width> <height> <mine> Create width × height single person game, witch could be controlled by button(if size smaller than 8 × 8).
                        /help List commands.
                        
                        Mine Sweeper Bot Plus
                        version v0.0.1
                        """.formatted(username))
                .build());
        //@%s
        //                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
        //                        Here's a list of commands to get you started:
        //
        //                        /create Create a new single person game with guide.
        //                        /create level <level> Create a classic single person game.
        //                        /create button [mine] Create 8 × 8 single person game easily controlled by inline-button.
        //                        /create <width> <height> <mine> Create width × height single person game, witch could be controlled by button(if size smaller than 8 × 8) or commands.
        //                        /dig <x> <y> Dig hole if you think there is no mine here.
        //                        /flag <x> <y> Plant a flag if you think there is mine here(which can help you think and better control).
        //                        /quit Quit your single person game in this chat.
        //                        /stop Stop listening letter number options.
        //                        /continue Continue listening letter number options.
        //                        /admin quit Force stop and quit game in this chat.
        //                        /help List commands.
        //
        //                        Mine Sweeper Bot Plus
        //                        version v0.0.1
    }


    @Slf4j
    public static class Displayer {
        private final TelegramClient client;

        public Displayer(TelegramClient client) {
            this.client = client;
        }

        private void pictureView(Game.SerializedGame game, String userID, String username, String chatID) throws TelegramApiException {

        }

        private void buttonView(Game.SerializedGame game, String userID, String username, String chatID, String messageID, Integer threadID) throws TelegramApiException {

            if (messageID == null || messageID.isEmpty()) {
                Message message = client.execute(
                        SendMessage.builder()
                                .chatId(chatID)
                                .messageThreadId(threadID)
                                .text("""
                                        @%s
                                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                        Ready to play? Just follow the steps below to start a new game.
                                        """.formatted(username))
                                .build()
                );
                messageID = message.getMessageId().toString();
            }

            if (game == null) {
                client.executeAsync(
                        EditMessageReplyMarkup.builder()
                                .chatId(chatID)
                                .messageId(Integer.parseInt(messageID))
                                .replyMarkup(
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(Collections.emptyList())
                                                .build()
                                ).build()
                );
                return;
            }

            if (game.isWin()) {
                client.executeAsync(
                        EditMessageReplyMarkup.builder()
                                .chatId(chatID)
                                .messageId(Integer.parseInt(messageID))
                                .replyMarkup(
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(List.of())
                                                .build()
                                ).build()
                );
                client.executeAsync(
                        EditMessageText.builder()
                                .chatId(chatID)
                                .messageId(Integer.parseInt(messageID))
                                .text("""
                                        @%s
                                        Congratulations! 🎉
                                        You've successfully completed the game in %d seconds.
                                        Map Dimensions: %d × %d
                                        Number of Mines: %d
                                        Well done on your achievement!
                                        """.formatted(
                                        username, game.time().toSeconds(),
                                        game.width(), game.height(), game.mines().length)
                                ).build()
                );
                return;
            }

            String command = game.currentStepFlag() ? "flag" : "dig";
            List<InlineKeyboardRow> keyboard = new ArrayList<>();

            if (Objects.requireNonNull(game.status()) == IGame.Status.End) {
                //boom!
                client.execute(
                        EditMessageText.builder()
                                .chatId(chatID)
                                .messageId(Integer.parseInt(messageID))
                                .text("""
                                        @%s
                                        Boom! 💣
                                        Unfortunately, you hit a mine and the game has ended.
                                        Time Elapsed: %d seconds.
                                        Map Dimensions: %d × %d
                                        Number of Mines: %d
                                        Better luck next time!
                                        """.formatted(
                                        username, game.time().toSeconds(),
                                        game.width(), game.height(), game.mines().length)
                                ).build()
                );

                IUnit[][] units = game.units();
                Step[] mines = game.mines();
                Step boom = game.steps()[game.steps().length - 1];
                for (int i = 0; i < units.length; i++) {
                    InlineKeyboardRow row = new InlineKeyboardRow();
                    for (int j = 0; j < units[i].length; j++) {
                        boolean contains = false;
                        Step step = new Step(i, j);
                        for (Step mine : mines) {
                            if (mine.equals(step)) {
                                contains = true;
                                break;
                            }
                        }
                        if (boom.equals(step)) {
                            row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDCA5")
                                    .callbackData("empty:%s:%s".formatted(i, j))
                                    .build());
                        } else if (contains) {
                            row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDCA3")
                                    .callbackData("empty:%s:%s".formatted(i, j))
                                    .build());
                        } else {
                            int num = units[i][j].getFilteredNum();
                            switch (num) {
                                case -2 -> row.add(InlineKeyboardButton.builder()
                                        .text("\uD83D\uDEA9")
                                        .callbackData("empty:%s:%s".formatted(i, j))
                                        .build());
                                case -1 -> row.add(InlineKeyboardButton.builder()
                                        .text("ㅤ")
                                        .callbackData("empty:%s:%s".formatted(i, j))
                                        .build());
                                default -> row.add(InlineKeyboardButton.builder()
                                        .text(String.valueOf(num))
                                        .callbackData("empty:%s:%s".formatted(i, j))
                                        .build());
                            }
                        }
                    }
                    keyboard.add(row);
                }
                InlineKeyboardRow row = new InlineKeyboardRow();
                row.add(
                        InlineKeyboardButton.builder()
                                .text("Try again?")
                                .callbackData("create:8:8:10")
                                .build()
                );
                keyboard.add(row);
            } else {
                for (int i = 0; i < game.units().length; i++) {
                    InlineKeyboardRow row = new InlineKeyboardRow();
                    for (int j = 0; j < game.units()[i].length; j++) {
                        int num = game.units()[i][j].getFilteredNum();
                        //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
                        String data = "spsg:%s:%s:%d:%d".formatted(game.gameID(), command, i, j);
                        switch (num) {
                            case -2 -> row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDEA9")
                                    .callbackData(data)
                                    .build());
                            case -1 -> row.add(InlineKeyboardButton.builder()
                                    .text("ㅤ")
                                    .callbackData(data)
                                    .build());
                            default -> row.add(InlineKeyboardButton.builder()
                                    .text(String.valueOf(num))
                                    .callbackData(data)
                                    .build());
                        }
                    }
                    keyboard.add(row);
                }
                InlineKeyboardRow row = new InlineKeyboardRow();
                //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
                String change = "spsg:%s:change:0:0".formatted(game.gameID());
                String quit = "spsg:%s:quit:0:0".formatted(game.gameID());
                row.add(
                        InlineKeyboardButton.builder()
                                .text(game.currentStepFlag() ? "Dig" : "Flag")
                                .callbackData(change)
                                .build()
                );
                row.add(
                        InlineKeyboardButton.builder()
                                .text("Quit")
                                .callbackData(quit)
                                .build()
                );
                keyboard.add(row);
            }

            client.execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(chatID)
                            .messageId(Integer.parseInt(messageID))
                            .replyMarkup(
                                    InlineKeyboardMarkup.builder()
                                            .keyboard(keyboard)
                                            .build()
                            ).build()
            );

        }

        private void classicView(Game.SerializedGame game, String userID, String username, String chatID,Integer threadID) throws TelegramApiException {
            try {
                if (game.isWin()) {
                    client.executeAsync(SendMessage.builder()
                            .chatId(chatID)
                            .messageThreadId(threadID)
                            .text("\uD83C\uDF89")
                            .build());
                }

                StringBuilder str = new StringBuilder();

                str.append("@").append(username).append("\n");
                str.append("Game start time: ").append(game.start()).append("\n");
                str.append("Game state: ").append(game.status()).append("\n");
                str.append("\n");

                if (Objects.requireNonNull(game.status()) == IGame.Status.End) {
                    IUnit[][] units = game.units();
                    Step[] mines = game.mines();
                    for (int i = 0; i < units.length; i++) {
                        for (int j = 0; j < units[i].length; j++) {
                            boolean contains = false;
                            Step step = new Step(i, j);
                            for (Step mine : mines) {
                                if (mine.equals(step)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (contains) {
                                str.append("* ");
                            } else {
                                int num = units[i][j].getFilteredNum();
                                switch (num) {
                                    case -2 -> str.append("F ");
                                    case -1 -> str.append(". ");
                                    default -> str.append(num).append(" ");
                                }
                            }
                        }
                        str.append("\n");
                    }
                } else {
                    for (IUnit[] unit : game.units()) {
                        for (IUnit iUnit : unit) {
                            int num = iUnit.getFilteredNum();
                            switch (num) {
                                case -2 -> str.append("F ");
                                case -1 -> str.append(". ");
                                default -> str.append(num).append(" ");
                            }
                        }
                        str.append("\n");
                    }
                }

                client.executeAsync(SendMessage.builder()
                        .chatId(chatID)
                        .messageThreadId(threadID)
                        .text(str.toString())
                        .build());
            } catch (TelegramApiException e) {
                log.error("Error display game for user@{} in chat@{}", username, chatID, e);
            }
        }

        public void display(Game.SerializedGame game, String userID, String username, String chatID, String messageID, Integer threadID) throws TelegramApiException {
            if (game.width() < 9 && game.height() < 9) buttonView(game, userID, username, chatID, messageID, threadID);
//        else buttonView(game, userID, username, chatID);
            else classicView(game, userID, username, chatID, threadID);
        }


    }
}
