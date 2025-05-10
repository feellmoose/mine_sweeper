package fun.feellmoose.gui.tgbot.handle.common;

import fun.feellmoose.core.*;
import fun.feellmoose.gui.tgbot.command.ButtonPlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.command.InnerBotCommand;
import fun.feellmoose.gui.tgbot.command.data.ButtonQueryDataText;
import fun.feellmoose.muti.ButtonPlayerGameManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ButtonPlayerSweeperGameCommandHandler implements InnerBotCommandHandler {

    private final ButtonPlayerGameManager gameManager;
    private final TelegramClient client;
    private final Displayer display;

    public ButtonPlayerSweeperGameCommandHandler(ButtonPlayerGameManager gameManager, TelegramClient client) {
        this.gameManager = gameManager;
        this.client = client;
        this.display = new Displayer(client);
    }

    @Override
    public void handle(InnerBotCommand command) {
        if (command instanceof ButtonPlayerSweeperGameCommand(
                ButtonQueryDataText data,
                String[] args,
                CallbackQuery query
        )) {
            if (data == null) return;
            if (!query.getFrom().getId().equals(data.userID())) return;

            try {
                try {
                    switch (data.getType()) {
                        case create -> create(query, data);
                        case dig -> dig(query, data);
                        case flag -> flag(query, data);
                        case quit -> quit(query, data);
                        case change -> change(query, data);
                    }
                } catch (GameException e) {
                    client.execute(
                            SendMessage.builder()
                                    .chatId(query.getMessage().getChatId())
                                    .messageThreadId(data.topicID())
                                    .text("""
                                            @%s Oops! Something went wrong!
                                            %s
                                            """.formatted(query.getFrom().getUserName(), e.getMessage()))
                                    .build()
                    );
                }
            } catch (TelegramApiException apiException) {
                log.error("Error sending message", apiException);
            }

        }
    }

    private void create(CallbackQuery query, ButtonQueryDataText data) throws GameException, TelegramApiException {
        if (gameManager.query(data.gameID()) != null) {
            client.execute(
                    SendMessage.builder()
                            .chatId(query.getMessage().getChatId())
                            .messageThreadId(data.topicID())
                            .text("""
                                    @%s 🎮 A game is currently active in this chat!
                                    """.formatted(query.getFrom().getUserName()))
                            .build()
            );
        }
        if (data.x() == 0 && data.y() == 0 && data.m() == 0) {
            //send create guide for user
            var row = new InlineKeyboardRow();
            row.add(
                    InlineKeyboardButton.builder()
                            .text("Started With Button")
                            .callbackData(new ButtonQueryDataText(data.topicID(), data.userID(), data.gameID(), "create", 8, 8, 10).getData())
                            .build()
            );
            client.executeAsync(
                    SendMessage.builder()
                            .chatId(query.getMessage().getChatId())
                            .messageThreadId(data.topicID())
                            .text("""
                                    @%s
                                    Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                    Ready to play? Just follow the steps below to start a new game.
                                    """.formatted(query.getFrom().getUserName()))
                            .replyMarkup(InlineKeyboardMarkup.builder()
                                    .keyboard(List.of(
                                            row
                                    )).build())
                            .build()
            );
        } else {
            int x = data.x();
            int y = data.y();
            int mines = data.m();
            if (x < 0 || y < 0 || mines < 0 || x > 8 || y > 8)
                throw new GameException("x, y should be between 0 and 8.");
            Game.SerializedGame game = gameManager.create(x, y, mines);
            display.display(game, query, data);
        }
    }

    private void dig(CallbackQuery query, ButtonQueryDataText data) throws GameException, TelegramApiException {
        Game.SerializedGame game = gameManager.dig(data.gameID(), new Step(data.x(), data.y()));
        display.display(game, query, data);
        if (game.isWin()) gameManager.quit(data.gameID());
    }

    private void flag(CallbackQuery query, ButtonQueryDataText data) throws GameException, TelegramApiException {
        Game.SerializedGame game = gameManager.flag(data.gameID(), new Step(data.x(), data.y()));
        display.display(game, query, data);
    }

    private void change(CallbackQuery query, ButtonQueryDataText data) throws GameException, TelegramApiException {
        Game.SerializedGame game = gameManager.change(data.gameID());
        display.display(game, query, data);
    }

    private void quit(CallbackQuery query, ButtonQueryDataText data) throws GameException, TelegramApiException {
        gameManager.quit(data.gameID());
        String str = "@" + query.getFrom().getUserName() + " Quit game success!";
        client.executeAsync(EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text(str)
                .build());
        display.display(null, query, data);
    }

    @Slf4j
    private static class Displayer {
        private final TelegramClient client;

        public Displayer(TelegramClient client) {
            this.client = client;
        }

        public void display(Game.SerializedGame game, CallbackQuery query, ButtonQueryDataText data) throws TelegramApiException, GameException {
            if (game == null) {
                client.executeAsync(
                        EditMessageReplyMarkup.builder()
                                .chatId(query.getMessage().getChatId())
                                .messageId(query.getMessage().getMessageId())
                                .replyMarkup(
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(Collections.emptyList())
                                                .build()
                                ).build()
                );
                return;
            }
            if (game.width() < 9 && game.height() < 9) buttonView(game, query, data);
            else throw new GameException("ButtonPlayerSweeperGameDisplay: invalid width or height");
        }

        private void buttonView(Game.SerializedGame game, CallbackQuery query, ButtonQueryDataText data) throws TelegramApiException {

            var message = query.getMessage();

            if (game.isWin()) {
                client.executeAsync(
                        EditMessageReplyMarkup.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .replyMarkup(
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(List.of())
                                                .build()
                                ).build()
                );
                client.executeAsync(
                        EditMessageText.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .text("""
                                        @%s
                                        Congratulations! 🎉
                                        You've successfully completed the game in %d seconds.
                                        Map Dimensions: %d × %d
                                        Number of Mines: %d
                                        Well done on your achievement!
                                        """.formatted(
                                        query.getFrom().getUserName(),
                                        game.time().toSeconds(),
                                        game.width(), game.height(),
                                        game.mines().length
                                )).build()
                );
                return;
            }

            String command = game.currentStepFlag() ? "flag" : "dig";
            List<InlineKeyboardRow> keyboard = new ArrayList<>();

            if (Objects.requireNonNull(game.status()) == IGame.Status.End) {
                //boom!
                client.execute(
                        EditMessageText.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .text("""
                                        @%s
                                        Boom! 💣
                                        Unfortunately, you hit a mine and the game has ended.
                                        Time Elapsed: %d seconds.
                                        Map Dimensions: %d × %d
                                        Number of Mines: %d
                                        Better luck next time!
                                        """.formatted(
                                        query.getFrom().getUserName(),
                                        game.time().toSeconds(),
                                        game.width(), game.height(),
                                        game.mines().length
                                )).build()
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
                        String str = new ButtonQueryDataText(data.topicID(), data.userID(), data.gameID(), "empty", i, j, 0).getData();
                        if (boom.equals(step)) {
                            row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDCA5")
                                    .callbackData(str)
                                    .build());
                        } else if (contains) {
                            row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDCA3")
                                    .callbackData(str)
                                    .build());
                        } else {
                            int num = units[i][j].getFilteredNum();
                            switch (num) {
                                case -2 -> row.add(InlineKeyboardButton.builder()
                                        .text("\uD83D\uDEA9")
                                        .callbackData(str)
                                        .build());
                                case -1 -> row.add(InlineKeyboardButton.builder()
                                        .text("ㅤ")
                                        .callbackData(str)
                                        .build());
                                default -> row.add(InlineKeyboardButton.builder()
                                        .text(String.valueOf(num))
                                        .callbackData(str)
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
                                .callbackData(new ButtonQueryDataText(data.topicID(), data.userID(), data.gameID(), "create", 8, 8, 10).getData())
                                .build()
                );
                keyboard.add(row);
            } else {
                for (int i = 0; i < game.units().length; i++) {
                    InlineKeyboardRow row = new InlineKeyboardRow();
                    for (int j = 0; j < game.units()[i].length; j++) {
                        int num = game.units()[i][j].getFilteredNum();
                        String str = new ButtonQueryDataText(data.topicID(), data.userID(), data.gameID(), command, i, j, 0).getData();
                        switch (num) {
                            case -2 -> row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDEA9")
                                    .callbackData(str)
                                    .build());
                            case -1 -> row.add(InlineKeyboardButton.builder()
                                    .text("ㅤ")
                                    .callbackData(str)
                                    .build());
                            default -> row.add(InlineKeyboardButton.builder()
                                    .text(String.valueOf(num))
                                    .callbackData(str)
                                    .build());
                        }
                    }
                    keyboard.add(row);
                }
                InlineKeyboardRow row = new InlineKeyboardRow();
                //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
                String change = new ButtonQueryDataText(data.topicID(), data.userID(), data.gameID(), "change", 0, 0, 0).getData();
                String quit = new ButtonQueryDataText(data.topicID(), data.userID(), data.gameID(), "quit", 0, 0, 0).getData();
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
