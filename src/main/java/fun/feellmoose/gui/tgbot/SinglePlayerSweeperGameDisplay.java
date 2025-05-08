package fun.feellmoose.gui.tgbot;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.IGame;
import fun.feellmoose.core.IUnit;
import fun.feellmoose.core.Step;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Objects;

@Slf4j
public class SinglePlayerSweeperGameDisplay {
    private final TelegramClient client;

    public SinglePlayerSweeperGameDisplay(TelegramClient client) {
        this.client = client;
    }

    public void display(Game.SerializedGame game, String userID, String username, String chatID) throws TelegramApiException {
//        if (game.width() < 7 && game.height() < 7) pictureView(game, userID, username, chatID);
//        else buttonView(game, userID, username, chatID);
        classicView(game, userID, username, chatID);
    }

    private void pictureView(Game.SerializedGame game, String userID, String username, String chatID) throws TelegramApiException {

    }

    private void buttonView(Game.SerializedGame game, String userID, String username, String chatID) throws TelegramApiException {

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

            client.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text(str.toString())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error display game for user@{} in chat@{}", username, chatID, e);
        }
    }


}
