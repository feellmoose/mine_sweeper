package fun.feellmoose.gui.tgbot.handle;

import fun.feellmoose.core.*;
import fun.feellmoose.muti.SinglePlayerGameManager;
import kotlin.random.Random;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Objects;

@Slf4j
public class SingleGameCommands {

    private final SinglePlayerGameManager gameManager;
    private final TelegramClient client;

    public SingleGameCommands(SinglePlayerGameManager gameManager, TelegramClient client) {
        this.gameManager = gameManager;
        this.client = client;
    }

    public Create create() {
        return new Create(gameManager, client);
    }

    public Dig dig() {
        return new Dig(gameManager, client);
    }

    public Flag flag() {
        return new Flag(gameManager, client);
    }

    public Quit quit() {
        return new Quit(gameManager, client);
    }

    public static class Create implements GameCommand {

        private final SinglePlayerGameManager gameManager;
        private final TelegramClient client;

        private Create(SinglePlayerGameManager gameManager, TelegramClient client) {
            this.gameManager = gameManager;
            this.client = client;
        }

        @Override
        public String getName() {
            return "/create";
        }

        @Override
        public void handle(Chat chat, User from, String[] args) throws GameException {
            String id = genID(chat, from);
            switch (args.length) {
                case 1 -> {
                    try {
                        client.execute(
                                SendMessage.builder()
                                        .chatId(chat.getId())
                                        .text("Hello world!")
                                        .build()
                        );
                    } catch (TelegramApiException e) {
                        log.error("Telegram Api hello world error", e);
                    }
                    //TODO create with panel
                }
                case 2 -> {
                    if (!args[1].equals("random")) throwFormatException();
                    int length = Random.Default.nextInt(1, 40);
                    int num = Random.Default.nextInt(1, length * length);
                    Game.SerializedGame game = gameManager.create(id, length, length, num);
                    displayGame(client, chat, from, game);
                }
                case 3 -> {
                    if (!args[1].equals("level")) throwFormatException();
                    Game.SerializedGame game;
                    switch (args[2]) {
                        case "1", "easy", "simple" -> game = gameManager.create(id, 9, 9, 10);
                        case "2", "normal", "common" -> game = gameManager.create(id, 16, 16, 40);
                        case "3", "hard", "complex" -> game = gameManager.create(id, 25, 25, 99);
                        default -> throw new GameException("Game level should be 'easy', 'normal', or 'hard'");
                    }
                    displayGame(client, chat, from, game);
                }
                case 4 -> {
                    try {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int mine = Integer.parseInt(args[3]);
                        Game.SerializedGame game = gameManager.create(id, x, y, mine);
                        displayGame(client, chat, from, game);
                    } catch (NumberFormatException e) {
                        throwFormatException();
                    }
                }
                default -> throwFormatException();
            }
        }

        private void throwFormatException() throws GameException {
            throw new GameException("Command format should be like: '/create', '/create random', '/create level $level' or '/create $width:number $height:number $mine:number'");
        }

    }


    public static class Dig implements GameCommand {

        private final SinglePlayerGameManager gameManager;
        private final TelegramClient client;

        private Dig(SinglePlayerGameManager gameManager, TelegramClient client) {
            this.gameManager = gameManager;
            this.client = client;
        }

        @Override
        public String getName() {
            return "/dig";
        }

        @Override
        public void handle(Chat chat, User from, String[] args) throws GameException {
            String id = genID(chat, from);
            if (args.length != 3) throwFormatException();
            try {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                Game.SerializedGame game = gameManager.dig(id, new Step(x, y));
                displayGame(client, chat, from, game);
            } catch (NumberFormatException e) {
                throwFormatException();
            }
        }

        private void throwFormatException() throws GameException {
            throw new GameException("Command format should be like: '/dig $x:number $y:number'");
        }
    }

    public static class Flag implements GameCommand {

        private final SinglePlayerGameManager gameManager;
        private final TelegramClient client;

        private Flag(SinglePlayerGameManager gameManager, TelegramClient client) {
            this.gameManager = gameManager;
            this.client = client;
        }


        @Override
        public String getName() {
            return "/flag";
        }

        @Override
        public void handle(Chat chat, User from, String[] args) throws GameException {
            String id = genID(chat, from);
            if (args.length != 3) throwFormatException();
            try {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                Game.SerializedGame game = gameManager.flag(id, new Step(x, y));
                displayGame(client, chat, from, game);
            } catch (NumberFormatException e) {
                throwFormatException();
            }
        }

        private void throwFormatException() throws GameException {
            throw new GameException("Command format should be like: '/flag $x:number $y:number'");
        }
    }

    public static class Quit implements GameCommand {

        private final SinglePlayerGameManager gameManager;
        private final TelegramClient client;

        private Quit(SinglePlayerGameManager gameManager, TelegramClient client) {
            this.gameManager = gameManager;
            this.client = client;
        }


        @Override
        public String getName() {
            return "/quit";
        }

        @Override
        public void handle(Chat chat, User from, String[] args) throws GameException {
            String id = genID(chat, from);
            gameManager.quit(id);
            displayQuitGame(client, chat, from);
        }
    }


    private static String genID(Chat chat, User from) {
        Long chatID = chat.getId();
        Long userID = from.getId();
        return "C" + chatID + "_U" + userID;
    }

    private static void displayQuitGame(TelegramClient client, Chat chat, User from) {
        try {
            String str = "@" + from.getUserName() + " quit game success";
            client.execute(SendMessage.builder()
                    .chatId(chat.getId())
                    .text(str)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error display quit game for user@{} in chat@{}", from.getUserName(), chat.getTitle(), e);
        }
    }

    private static void displayGame(TelegramClient client, Chat chat, User from, Game.SerializedGame game) {
        try {
            StringBuilder str = new StringBuilder();

            str.append("@").append(from.getUserName()).append("\n");
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
                    .chatId(chat.getId())
                    .text(str.toString())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error display game for user@{} in chat@{}", from.getUserName(), chat.getTitle(), e);
        }
    }

}
