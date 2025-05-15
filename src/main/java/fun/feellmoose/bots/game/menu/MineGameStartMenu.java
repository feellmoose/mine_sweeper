package fun.feellmoose.bots.game.menu;

import fun.feellmoose.bots.TelegramBotGame;
import fun.feellmoose.bots.command.menu.TelegramBotMenuCallbackQueryCommand;
import fun.feellmoose.bots.command.menu.TelegramBotMenuCallbackQueryData;
import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.i18n.Messages;
import fun.feellmoose.utils.LocaleUtils;
import fun.feellmoose.utils.RandomUtils;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public record MineGameStartMenu(
        String id,
        String message,
        Locale locale,
        List<List<Button>> rows
) implements Menu {
    public static final String GUIDE_MENU_NAME = "game-mine-start-guide";
    public static final String CLASSIC_MENU_NAME = "game-mine-start-classic";
    public static final String RANDOM_MENU_NAME = "game-mine-start-random";
    public static final String LEVEL_MENU_NAME = "game-mine-start-level";
    public static final String LEVEL_EASY_MENU_NAME = "game-mine-start-level-easy";
    public static final String LEVEL_NORMAL_MENU_NAME = "game-mine-start-level-normal";
    public static final String LEVEL_HARD_MENU_NAME = "game-mine-start-level-hard";

    @Nullable
    public static MineGameStartMenu of(TelegramBotMenuCallbackQueryCommand command, Locale locale){
        return switch (command.data().menuID()) {
            case MineGameStartMenu.CLASSIC_MENU_NAME -> classic(command, locale);
            case MineGameStartMenu.RANDOM_MENU_NAME -> random(command, locale);
            case MineGameStartMenu.LEVEL_MENU_NAME -> level(command, locale);
            case MineGameStartMenu.LEVEL_EASY_MENU_NAME -> easy(command, locale);
            case MineGameStartMenu.LEVEL_NORMAL_MENU_NAME -> normal(command, locale);
            case MineGameStartMenu.LEVEL_HARD_MENU_NAME -> hard(command, locale);
            case GUIDE_MENU_NAME -> guide(command, locale);
            default -> null;
        };
    }

    public static MineGameStartMenu guide(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        var query = command.callbackQuery();
        var data = command.data();
        return new MineGameStartMenu(
                GUIDE_MENU_NAME,
                Messages.load("game.mine.menu.guide.note", locale).formatted(
                        query.getFrom().getUserName(),
                        TelegramBotGame.version
                ),
                locale,
                List.of(List.of(Button.of("game.mine.menu.classic.button",
                                new TelegramBotMenuCallbackQueryData(
                                        TelegramBotMenuCallbackQueryData.Action.jump,
                                        data.topicID(),
                                        data.userID(),
                                        CLASSIC_MENU_NAME
                                ))),
                        List.of(Button.of("game.mine.menu.level.button",
                                new TelegramBotMenuCallbackQueryData(
                                        TelegramBotMenuCallbackQueryData.Action.jump,
                                        data.topicID(),
                                        data.userID(),
                                        LEVEL_MENU_NAME
                                ))),
                        List.of(Button.of("game.mine.menu.random.button",
                                new TelegramBotMenuCallbackQueryData(
                                        TelegramBotMenuCallbackQueryData.Action.jump,
                                        data.topicID(),
                                        data.userID(),
                                        RANDOM_MENU_NAME
                                )))
                )
        );
    }

    public static MineGameStartMenu guide(Message message, Locale locale) {
        Integer topicID = message.getMessageThreadId();
        Long userID = message.getFrom().getId();
        return new MineGameStartMenu(
                GUIDE_MENU_NAME,
                Messages.load("game.mine.menu.guide.note", locale).formatted(
                        message.getFrom().getUserName(),
                        TelegramBotGame.version
                ),
                locale,
                List.of(List.of(Button.of("game.mine.menu.classic.button",
                                new TelegramBotMenuCallbackQueryData(
                                        TelegramBotMenuCallbackQueryData.Action.jump,
                                        topicID,
                                        userID,
                                        CLASSIC_MENU_NAME
                                ))),
                        List.of(Button.of("game.mine.menu.level.button",
                                new TelegramBotMenuCallbackQueryData(
                                        TelegramBotMenuCallbackQueryData.Action.jump,
                                        topicID,
                                        userID,
                                        LEVEL_MENU_NAME
                                ))),
                        List.of(Button.of("game.mine.menu.random.button",
                                new TelegramBotMenuCallbackQueryData(
                                        TelegramBotMenuCallbackQueryData.Action.jump,
                                        topicID,
                                        userID,
                                        RANDOM_MENU_NAME
                                )))
                )
        );
    }

    public static MineGameStartMenu view(int width, int height, int mine, TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        var query = command.callbackQuery();
        return new MineGameStartMenu(
                CLASSIC_MENU_NAME,
                Messages.load("game.mine.start.note", locale).formatted(query.getFrom().getUserName(), TelegramBotGame.version,width,height,mine),
                locale,
                List.of(
                        List.of(
                                Button.of("game.mine.start.button",
                                        new TelegramBotMineGameCallbackQueryData(
                                                command.data().topicID(),
                                                null,
                                                query.getFrom().getId(),
                                                TelegramBotMineGameCallbackQueryData.Action.create,
                                                width,height,mine
                                        ))
                        )
                )
        );
    }

    public static MineGameStartMenu easy(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        return view(6,6,5,command,locale);
    }

    public static MineGameStartMenu easy(Message message, Locale locale) {
        return view(6,6,5,message,locale);
    }

    public static MineGameStartMenu normal(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        return view(8,8,10,command,locale);
    }

    public static MineGameStartMenu normal(Message message, Locale locale) {
        return view(8,8,10,message,locale);
    }

    public static MineGameStartMenu hard(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        return view(8,8,13,command,locale);
    }

    public static MineGameStartMenu hard(Message message, Locale locale) {
        return view(8,8,13,message,locale);
    }


    public static MineGameStartMenu classic(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        return view(8,8,10,command,locale);
    }

    public static MineGameStartMenu classic(Message message, Locale locale) {
        return view(8,8,10,message,locale);
    }

    public static MineGameStartMenu random(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        int width = ThreadLocalRandom.current().nextInt(3, 8);
        int height = ThreadLocalRandom.current().nextInt(3, 8);
        int total = width * height;
        Function<Double,Double> refactor = (raw) -> {
            if (raw > 0.7) return Math.pow(raw, 3.0);
            if (raw < 0.2) return Math.pow(raw, 0.5);
            return raw;
        };
        double density = RandomUtils.randomDensity(0.10, 0.25, refactor);
        int mines = (int) (density * total);
        return view(width,height,mines,command,locale);
    }

    public static MineGameStartMenu random(Message message, Locale locale) {
        int width = ThreadLocalRandom.current().nextInt(3, 8);
        int height = ThreadLocalRandom.current().nextInt(3, 8);
        int total = width * height;
        Function<Double,Double> refactor = (raw) -> {
            if (raw > 0.7) return Math.pow(raw, 3.0);
            if (raw < 0.2) return Math.pow(raw, 0.5);
            return raw;
        };
        double density = RandomUtils.randomDensity(0.10, 0.25, refactor);
        int mines = (int) (density * total);
        return view(width,height,mines,message,locale);
    }

    public static MineGameStartMenu view(int width, int height, int mine, Message message, Locale locale) {
        return new MineGameStartMenu(
                CLASSIC_MENU_NAME,
                Messages.load("game.mine.start.note", locale).formatted(message.getFrom().getUserName(), TelegramBotGame.version,width,height,mine),
                locale,
                List.of(
                        List.of(
                                Button.of("game.mine.start.button",
                                        new TelegramBotMineGameCallbackQueryData(
                                                message.getMessageThreadId(),
                                                null,
                                                message.getFrom().getId(),
                                                TelegramBotMineGameCallbackQueryData.Action.create,
                                                width,height,mine
                                        ))
                        )
                )
        );
    }

    public static MineGameStartMenu level(TelegramBotMenuCallbackQueryCommand command, Locale locale) {
        var query = command.callbackQuery();
        var data = command.data();
        return new MineGameStartMenu(
                LEVEL_MENU_NAME,
                Messages.load("game.mine.start.level.note", locale).formatted(query.getFrom().getUserName(),TelegramBotGame.version),
                locale,
                List.of(
                        List.of(
                                Button.of("game.mine.menu.random.button",
                                        new TelegramBotMenuCallbackQueryData(
                                                TelegramBotMenuCallbackQueryData.Action.jump,
                                                data.topicID(),
                                                data.userID(),
                                                LEVEL_EASY_MENU_NAME
                                        )),
                                Button.of("game.mine.start.level.normal",
                                        new TelegramBotMenuCallbackQueryData(
                                                TelegramBotMenuCallbackQueryData.Action.jump,
                                                data.topicID(),
                                                data.userID(),
                                                LEVEL_NORMAL_MENU_NAME
                                        )),
                                Button.of("game.mine.start.level.hard",
                                        new TelegramBotMenuCallbackQueryData(
                                                TelegramBotMenuCallbackQueryData.Action.jump,
                                                data.topicID(),
                                                data.userID(),
                                                LEVEL_HARD_MENU_NAME
                                        ))
                        )
                )
        );
    }

    public static MineGameStartMenu level(Message message, Locale locale) {
        Integer topicID = message.getMessageThreadId();
        Long userID = message.getFrom().getId();
        return new MineGameStartMenu(
                LEVEL_MENU_NAME,
                Messages.load("game.mine.start.level.note", locale).formatted(message.getFrom().getUserName(),TelegramBotGame.version),
                locale,
                List.of(
                        List.of(
                                Button.of("game.mine.menu.random.button",
                                        new TelegramBotMenuCallbackQueryData(
                                                TelegramBotMenuCallbackQueryData.Action.jump,
                                                topicID,
                                                userID,
                                                LEVEL_EASY_MENU_NAME
                                        )),
                                Button.of("game.mine.start.level.normal",
                                        new TelegramBotMenuCallbackQueryData(
                                                TelegramBotMenuCallbackQueryData.Action.jump,
                                                topicID,
                                                userID,
                                                LEVEL_NORMAL_MENU_NAME
                                        )),
                                Button.of("game.mine.start.level.hard",
                                        new TelegramBotMenuCallbackQueryData(
                                                TelegramBotMenuCallbackQueryData.Action.jump,
                                                topicID,
                                                userID,
                                                LEVEL_HARD_MENU_NAME
                                        ))
                        )
                )
        );
    }


}
