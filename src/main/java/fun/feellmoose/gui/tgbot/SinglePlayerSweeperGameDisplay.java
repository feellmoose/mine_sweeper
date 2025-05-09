package fun.feellmoose.gui.tgbot;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.IGame;
import fun.feellmoose.core.IUnit;
import fun.feellmoose.core.Step;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

@Slf4j
public class SinglePlayerSweeperGameDisplay {
    private final TelegramClient client;

    public SinglePlayerSweeperGameDisplay(TelegramClient client) {
        this.client = client;
    }

    public void display(Game.SerializedGame game, String userID, String username, String chatID, String messageID) throws TelegramApiException {
        if (game.width() < 9 && game.height() < 9) buttonView(game, userID, username, chatID, messageID);
//        else buttonView(game, userID, username, chatID);
        else classicView(game, userID, username, chatID);
    }

    private void pictureView(Game.SerializedGame game, String userID, String username, String chatID) throws TelegramApiException {

    }

    private void buttonView(Game.SerializedGame game, String userID, String username, String chatID, String messageID) throws TelegramApiException {

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
                                    You've successfully completed the game in %s seconds.
                                    Map Dimensions: %d × %d
                                    Number of Mines: %d
                                    Well done on your achievement!
                                    """.formatted(
                                            username, game.duration(),
                                    game.width(), game.height(), game.mines().length)
                            ).build()
            );
        }

        String command = game.currentStepFlag()? "flag" : "dig";
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        if (Objects.requireNonNull(game.status()) == IGame.Status.End) {
            IUnit[][] units = game.units();
            Step[] mines = game.mines();
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
                    if (contains) {
                        row.add(InlineKeyboardButton.builder()
                                .text("\uD83D\uDCA3")
                                .build());
                    } else {
                        int num = units[i][j].getFilteredNum();
                        switch (num) {
                            case -2 -> row.add(InlineKeyboardButton.builder()
                                    .text("\uD83D\uDEA9")
                                    .build());
                            case -1 -> row.add(InlineKeyboardButton.builder()
                                    .text(" ")
                                    .build());
                            default -> row.add(InlineKeyboardButton.builder()
                                    .text(String.valueOf(num))
                                    .build());
                        }
                    }
                }
                keyboard.add(row);
            }
        } else {
            for (int i = 0; i < game.units().length; i++) {
                InlineKeyboardRow row = new InlineKeyboardRow();
                for (int j = 0; j < game.units()[i].length; j++) {
                    int num = game.units()[i][j].getFilteredNum();
                    //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
                    String data = "spsg:%s:%s:%d:%d".formatted(game.gameID(),command,i,j);
                    switch (num) {
                        case -2 -> row.add(InlineKeyboardButton.builder()
                                .text("\uD83D\uDEA9")
                                .callbackData(data)
                                .build());
                        case -1 -> row.add(InlineKeyboardButton.builder()
                                .text(" ")
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
        }


        InlineKeyboardRow row = new InlineKeyboardRow();
        //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
        String change = "spsg:%s:change:0:0".formatted(game.gameID());
        String quit = "spsg:%s:quit:0:0".formatted(game.gameID());
        row.add(
                InlineKeyboardButton.builder()
                        .text(game.currentStepFlag()?"Dig":"Flag")
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

    private void classicView(Game.SerializedGame game, String userID, String username, String chatID) throws TelegramApiException {
        try {
            if (game.isWin()) {
                client.execute(SendMessage.builder()
                        .chatId(chatID)
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
                    .text(str.toString())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error display game for user@{} in chat@{}", username, chatID, e);
        }
    }


}
