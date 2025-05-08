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
                String chatName
        )) {
            try {
                try {
                    switch (type) {
                        case create -> create(args, userID, username, chatID);
                        case dig -> dig(args, userID, username, chatID);
                        case flag -> flag(args, userID, username, chatID);
                        case quit -> quit(args, userID, chatID);
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

    private void create(String[] args, String userID, String username, String chatID) throws GameException, TelegramApiException {
        if (gameManager.query(chatID) != null) {
            SendMessage.builder()
                    .chatId(chatID)
                    .text("""
                            @%s
                            🎮 A game is currently active in this chat!
                            To end it early, type '/quit' or '/admin quit'.
                            """.formatted(username))
                    .build();
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
                    Game.SerializedGame game = gameManager.create(chatID, length, length, num);
                    display.display(game, userID, username, chatID);
                }
            }
            case 2 -> {
                if (args[0].equals("level")) {
                    Game.SerializedGame game;
                    switch (args[1]) {
                        case "easy" -> game = gameManager.create(chatID, 9, 9, 10);
                        case "normal" -> game = gameManager.create(chatID, 16, 16, 40);
                        case "hard" -> game = gameManager.create(chatID, 25, 25, 99);
                        default -> throw new GameException("Game level should be 'easy', 'normal', or 'hard'");
                    }
                    display.display(game, userID, username, chatID);
                }
            }
            case 3 -> {
                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int mine = Integer.parseInt(args[2]);
                    if (x < 0 || y < 0 || mine < 0 || x > 52 || y > 52 || mine > 52)
                        throw new GameException("x, y and mine num should be between 0 and 52.");
                    Game.SerializedGame game = gameManager.create(chatID, x, y, mine);
                    display.display(game, userID, username, chatID);
                } catch (NumberFormatException e) {
                    throw new GameException("x, y and mine num should be a number.");
                }
            }
            default -> throw new GameException("Command args too long.");
        }
    }

    private void dig(String[] args, String userID, String username, String chatID) throws GameException, TelegramApiException {
        if (args.length != 2) throw new GameException("Command args length has to be 2.");
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Game.SerializedGame game = gameManager.dig(chatID, new Step(x, y));
            display.display(game, userID, username, chatID);
        } catch (NumberFormatException e) {
            throw new GameException("x, y and mine num should be a number.");
        }
    }

    private void flag(String[] args, String userID, String username, String chatID) throws GameException, TelegramApiException {
        if (args.length != 2) throw new GameException("Command args length has to be 2.");
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            Game.SerializedGame game = gameManager.flag(chatID, new Step(x, y));
            display.display(game, userID, username, chatID);
        } catch (NumberFormatException e) {
            throw new GameException("x, y and mine num should be a number.");
        }
    }

    private void quit(String[] args, String username, String chatID) throws GameException, TelegramApiException {
        gameManager.quit(chatID);
        String str = "@" + username + " quit game success!";
        client.execute(SendMessage.builder()
                .chatId(chatID)
                .text(str)
                .build());
    }
}
