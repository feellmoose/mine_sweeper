package fun.feellmoose.bots.handler.mine;

import fun.feellmoose.bots.TelegramBotGame;
import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.bots.handler.CommandHandler;
import fun.feellmoose.i18n.Messages;
import fun.feellmoose.utils.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Locale;

@Slf4j
public class MineLevelCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public MineLevelCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/mine_level";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        Locale locale = LocaleUtils.fromString(from.getLanguageCode());
        switch (args.length) {
            case 2 -> {
                try {
                    switch (args[1].toLowerCase()) {
                        case "easy" -> startClassic(message,chat,from,6,6,5);
                        case "normal" -> startClassic(message,chat,from,8,8,10);
                        case "hard" -> startClassic(message,chat,from,8,8,13);
                        default -> client.execute(
                                SendMessage.builder()
                                        .text("@%s [level]: 'easy', 'normal', or 'hard'".formatted(from.getUserName()))
                                        .chatId(chat.getId())
                                        .messageThreadId(message.getMessageThreadId())
                                        .build()
                        );
                    }
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
            case 1 -> {
                var row = new InlineKeyboardRow();
                row.add(
                        InlineKeyboardButton.builder()
                                .text(Messages.load("game.mine.start.level.easy", locale).formatted())
                                .callbackData(new TelegramBotMineGameCallbackQueryData(
                                        message.getMessageThreadId(),
                                        null,
                                        from.getId(),
                                        TelegramBotMineGameCallbackQueryData.Action.create,
                                        6,6,5
                                ).data())
                                .build()
                );
                row.add(
                        InlineKeyboardButton.builder()
                                .text(Messages.load("game.mine.start.level.normal", locale).formatted())
                                .callbackData(new TelegramBotMineGameCallbackQueryData(
                                        message.getMessageThreadId(),
                                        null,
                                        from.getId(),
                                        TelegramBotMineGameCallbackQueryData.Action.create,
                                        8,8,10
                                ).data())
                                .build()
                );
                row.add(
                        InlineKeyboardButton.builder()
                                .text(Messages.load("game.mine.start.level.hard", locale).formatted())
                                .callbackData(new TelegramBotMineGameCallbackQueryData(
                                        message.getMessageThreadId(),
                                        null,
                                        from.getId(),
                                        TelegramBotMineGameCallbackQueryData.Action.create,
                                        8,8,13
                                ).data())
                                .build()
                );
                try {
                    client.executeAsync(
                            SendMessage.builder()
                                    .chatId(message.getChatId())
                                    .messageThreadId(message.getMessageThreadId())
                                    .text(Messages.load("game.mine.menu", locale).formatted(TelegramBotGame.version,from.getUserName()))
                                    .replyMarkup(InlineKeyboardMarkup.builder()
                                            .keyboard(List.of(
                                                    row
                                            )).build())
                                    .build()
                    );
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
        }
    }

    private void startClassic(Message message, Chat chat, User from, int x, int y, int mine) throws TelegramApiException {
        var row = new InlineKeyboardRow();
        Locale locale = LocaleUtils.fromString(from.getLanguageCode());
        row.add(
                InlineKeyboardButton.builder()
                        .text(Messages.load("game.mine.start.button", locale).formatted())
                        .callbackData(new TelegramBotMineGameCallbackQueryData(
                                message.getMessageThreadId(),
                                null,
                                from.getId(),
                                TelegramBotMineGameCallbackQueryData.Action.create,
                                x,y,mine
                        ).data())
                        .build()
        );
        client.executeAsync(
                SendMessage.builder()
                        .chatId(chat.getId())
                        .messageThreadId(message.getMessageThreadId())
                        .text(Messages.load("game.mine.start.note", locale).formatted(TelegramBotGame.version,from.getUserName(),x,y,mine))
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboard(List.of(
                                        row
                                )).build())
                        .build()
        );
    }

}
