package fun.feellmoose.gui.tgbot.handle.common;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.GameException;
import fun.feellmoose.core.Step;
import fun.feellmoose.gui.tgbot.SinglePlayerSweeperGameDisplay;
import fun.feellmoose.gui.tgbot.command.InnerBotCommand;
import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.muti.SinglePlayerGameManager;
import kotlin.random.Random;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
public class SinglePlayerSweeperGameCommandHandler implements InnerBotCommandHandler {

    private final SinglePlayerGameManager gameManager;
    private final TelegramClient client;
    private final SinglePlayerSweeperGameDisplay display;

    public SinglePlayerSweeperGameCommandHandler(SinglePlayerGameManager gameManager, TelegramClient client) {
        this.gameManager = gameManager;
        this.client = client;
        this.display = new SinglePlayerSweeperGameDisplay(client);
    }

    @Override
    public void handle(InnerBotCommand command) {
        if (command instanceof SinglePlayerSweeperGameCommand(
                SinglePlayerSweeperGameCommand.Type type,
                String[] args,
                String userID,
                String username,
                String chatID,
                String chatName,
                String messageID,
                String gameID
        )) {
            try {
                try {
                    switch (type) {
                        case create -> create(args, userID, username, chatID, messageID);
                        case dig -> dig(args, userID, username, chatID, messageID, gameID);
                        case flag -> flag(args, userID, username, chatID, messageID, gameID);
                        case quit -> quit(args, userID, username, chatID, messageID,gameID);
                        case help -> help(args, userID, username, chatID);
                    }
                } catch (GameException e) {
                    client.execute(
                            SendMessage.builder()
                                    .chatId(chatID)
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

    private void create(String[] args, String userID, String username, String chatID, String messageID) throws GameException, TelegramApiException {
        if (gameManager.query(userID,chatID,messageID,null) != null) {
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
                client.execute(
                        SendMessage.builder()
                                .chatId(chatID)
                                .text("""
                                        @%s
                                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                        Ready to play? Just follow the steps below to start a new game.
                                        """.formatted(username))
                                .build()
                );

            }
            case 1 -> {
                if (args[0].equals("random")) {
                    int length = Random.Default.nextInt(1, 52);
                    double random = Random.Default.nextDouble(0, 1);
                    //filter to limit num smaller
                    int num = (int) (Math.pow(random, 10) * Math.pow(length, 2));
                    Game.SerializedGame game = gameManager.create(userID,chatID,messageID, length, length, num);
                    display.display(game, userID, username, chatID, messageID);
                }
            }
            case 2 -> {
                if (args[0].equals("level")) {
                    Game.SerializedGame game;
                    switch (args[1]) {
                        case "easy" -> game = gameManager.create(userID,chatID,messageID, 9, 9, 10);
                        case "normal" -> game = gameManager.create(userID,chatID,messageID, 16, 16, 40);
                        case "hard" -> game = gameManager.create(userID,chatID,messageID, 25, 25, 99);
                        default -> throw new GameException("Game level should be 'easy', 'normal', or 'hard'");
                    }
                    display.display(game, userID, username, chatID, messageID);
                }
            }
            case 3 -> {
                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int mine = Integer.parseInt(args[2]);
                    if (x < 0 || y < 0 || mine < 0 || x > 52 || y > 52 || mine > 52)
                        throw new GameException("x, y and mine num should be between 0 and 52.");
                    Game.SerializedGame game = gameManager.create(userID,chatID,messageID, x, y, mine);
                    display.display(game, userID, username, chatID, messageID);
                } catch (NumberFormatException e) {
                    throw new GameException("x, y and mine num should be a number.");
                }
            }
            default -> throw new GameException("Command args too long.");
        }
    }

    private void dig(String[] args, String userID, String username, String chatID, String messageID,String gameID) throws GameException, TelegramApiException {
        if (args.length != 2) throw new GameException("Command args length has to be 2.");
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Game.SerializedGame game = gameManager.dig(userID,chatID,messageID,gameID, new Step(x, y));
            display.display(game, userID, username, chatID, messageID);
            if (game.isWin()) gameManager.quit(userID,chatID,messageID,gameID);
        } catch (NumberFormatException e) {
            throw new GameException("x, y and mine num should be a number.");
        }
    }

    private void flag(String[] args, String userID, String username, String chatID, String messageID,String gameID) throws GameException, TelegramApiException {
        if (args.length != 2) throw new GameException("Command args length has to be 2.");
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Game.SerializedGame game = gameManager.flag(userID,chatID,messageID,gameID, new Step(x, y));
            display.display(game, userID, username, chatID, messageID);
        } catch (NumberFormatException e) {
            throw new GameException("x, y and mine num should be a number.");
        }
    }

    private void quit(String[] args, String userID, String username, String chatID, String messageID,String gameID) throws GameException, TelegramApiException {
        gameManager.quit(userID, chatID, messageID, gameID);
        String str = "@" + username + " quit game success!";
        client.execute(SendMessage.builder()
                .chatId(chatID)
                .text(str)
                .build());
    }

    private void help(String[] args, String userID, String username, String chatID) throws GameException, TelegramApiException {
        client.execute(SendMessage.builder()
                .chatId(chatID)
                .text("""
                        @%s
                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                        Here's a list of commands to get you started:
                        
                        /create Create a new single person game with guide.
                        /create level <level> Create a classic single person game.
                        /create button [mine] Create 8 × 8 single person game easily controlled by inline-button.
                        /create <width> <height> <mine> Create width × height single person game, witch could be controlled by button(if size smaller than 8 × 8) or commands.
                        /dig <x> <y> Dig hole if you think there is no mine here.
                        /flag <x> <y> Plant a flag if you think there is mine here(which can help you think and better control).
                        /quit Quit your single person game in this chat.
                        /stop Stop listening letter number options.
                        /continue Continue listening letter number options.
                        /admin quit Force stop and quit game in this chat.
                        /help List commands.
                        
                        Mine Sweeper Bot Plus
                        version v0.0.1
                        """.formatted(username))
                .build());
    }
}
