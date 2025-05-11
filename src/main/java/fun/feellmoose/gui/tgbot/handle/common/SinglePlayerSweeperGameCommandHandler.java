package fun.feellmoose.gui.tgbot.handle.common;

import fun.feellmoose.core.*;
import fun.feellmoose.gui.tgbot.TelegramBotGame;
import fun.feellmoose.gui.tgbot.command.InnerBotCommand;
import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.command.data.ButtonQueryDataText;
import fun.feellmoose.muti.SinglePlayerGameManager;
import fun.feellmoose.utils.RandomUtils;
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

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
public class SinglePlayerSweeperGameCommandHandler implements InnerBotCommandHandler {

    private final TelegramClient client;
    private final Displayer display;

    public SinglePlayerSweeperGameCommandHandler(TelegramClient client) {
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
            Long userID = user.getId();
            String username = user.getUserName();
            String chatID = message.getChatId().toString();
            String messageID = message.getMessageId().toString();
            Integer threadID = message.getMessageThreadId();

            try {
                try {
                    switch (type) {
                        case create -> create(args, userID, username, chatID, messageID, threadID);
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

    private void create(String[] args, Long userID, String username, String chatID, String messageID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Creating game... {}", Arrays.asList(args));
        switch (args.length) {
            case 0 -> {
                //send create guide for user
                var row = new InlineKeyboardRow();
                row.add(
                        InlineKeyboardButton.builder()
                                .text("Classic")
                                .callbackData(new ButtonQueryDataText(
                                        threadID, userID, null, "create", 8, 8, 10
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
                switch (args[0]) {
                    case "random" -> {
                        int length = ThreadLocalRandom.current().nextInt(3, 8);
                        int total = length * length;

                        Function<Double,Double> refactor = (raw) -> {
                            if (raw > 0.8) return Math.pow(raw, 3.0);
                            if (raw < 0.2) return Math.pow(raw, 0.5);
                            return raw;
                        };

                        double density = RandomUtils.randomDensity(0.15, 0.35, refactor);

                        int mines = (int) (density * total);

                        startButton(threadID, userID, chatID, username,length,length,mines);
                    }
                    case "level" -> {
                        var row = new InlineKeyboardRow();
                        row.add(
                                InlineKeyboardButton.builder()
                                        .text("Easy")
                                        .callbackData(new ButtonQueryDataText(
                                                threadID, userID, null, "create", 6, 6, 5
                                        ).getData())
                                        .build()
                        );
                        row.add(
                                InlineKeyboardButton.builder()
                                        .text("Normal")
                                        .callbackData(new ButtonQueryDataText(
                                                threadID, userID, null, "create",8, 8, 10
                                        ).getData())
                                        .build()
                        );
                        row.add(
                                InlineKeyboardButton.builder()
                                        .text("Hard")
                                        .callbackData(new ButtonQueryDataText(
                                                threadID, userID, null, "create", 8, 8, 14
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
                                        Please choose level to start a new game.
                                        """.formatted(username))
                                        .replyMarkup(InlineKeyboardMarkup.builder()
                                                .keyboard(List.of(
                                                        row
                                                )).build())
                                        .build()
                        );

                    }
                }
            }
            case 2 -> {
                if (args[0].equals("level")) {
                    switch (args[1]) {
                        case "easy" -> startButton(threadID, userID, chatID, username, 6, 6, 5);
                        case "normal" -> startButton(threadID, userID, chatID, username, 8, 8, 9);
                        case "hard" -> startButton(threadID, userID, chatID, username, 8, 8, 12);
                        default -> throw new GameException("Game level should be 'easy', 'normal', or 'hard'");
                    }
                }
            }
            case 3 -> {
                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int mine = Integer.parseInt(args[2]);
                    if (x < 0 || y < 0 || mine < 0 || x > 8 || y > 8 || mine > 32)
                        throw new GameException("x, y and mine num should be between 0 and 8.");
                    startButton(threadID, userID, chatID, username, x, y, mine);
                } catch (NumberFormatException e) {
                    throw new GameException("x, y and mine num should be a number.");
                }
            }
            default -> throw new GameException("Command args too long.");
        }
    }

    private void startButton(Integer threadID, Long userID, String chatID, String username, int x, int y, int mine) throws GameException, TelegramApiException {
        var row = new InlineKeyboardRow();
        row.add(
                InlineKeyboardButton.builder()
                        .text("Started With Button")
                        .callbackData(new ButtonQueryDataText(
                                threadID, userID, null, "create", x, y, mine
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
                                        You have started a new %d × %d game with %d mines.
                                        """.formatted(username,x,y,mine))
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboard(List.of(
                                        row
                                )).build())
                        .build()
        );
    }

    private void help(String[] args, Long userID, String username, String chatID, Integer threadID) throws GameException, TelegramApiException {
        log.debug("Help option... {}", Arrays.asList(args));
        client.executeAsync(SendMessage.builder()
                .chatId(chatID)
                .messageThreadId(threadID)
                .text("""
                        @%s
                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                        Here's a list of commands to get you started:
                        
                        /create Create a default game.
                        /create random Create random n × n game with random mines.
                        /create level [level] Create diff level game.
                        /create <width> <height> <mine> Create width × height game with mines.
                        /help List commands.
                        
                        Mine Sweeper Bot Plus created By feellmoose.
                        version %s last update at %s
                        """.formatted(username, TelegramBotGame.version, TelegramBotGame.updateAt.format(DateTimeFormatter.ISO_DATE_TIME)))
                .build());
    }


    @Slf4j
    public static class Displayer {
        private final TelegramClient client;

        public Displayer(TelegramClient client) {
            this.client = client;
        }

    }
}
