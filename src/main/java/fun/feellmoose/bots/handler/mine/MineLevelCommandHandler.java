package fun.feellmoose.bots.handler.mine;

import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.bots.handler.CommandHandler;
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

@Slf4j
public class MineLevelCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public MineLevelCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/mine-level";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        switch (args.length) {
            case 2 -> {
                try {
                    switch (args[1].toLowerCase()) {
                        case "easy" -> startClassic(message,chat,from,6,6,5);
                        case "normal" -> startClassic(message,chat,from,8,8,10);
                        case "hard" -> startClassic(message,chat,from,8,8,13);
                        default -> client.execute(
                                SendMessage.builder()
                                        .text("@%s Game level should be 'easy', 'normal', or 'hard'".formatted(from.getUserName()))
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
                                .text("Easy")
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
                                .text("Normal")
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
                                .text("Hard")
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
                                    .text("""
                                            @%s
                                            Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                            Please choose level to start a new game.
                                            """.formatted(from.getUserName()))
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
        row.add(
                InlineKeyboardButton.builder()
                        .text("Classic")
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
                        .text("""
                                        @%s
                                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                        You have started a new %d × %d game with %d mines.
                                        """.formatted(from.getUserName(),x,y,mine))
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboard(List.of(
                                        row
                                )).build())
                        .build()
        );
    }

}
