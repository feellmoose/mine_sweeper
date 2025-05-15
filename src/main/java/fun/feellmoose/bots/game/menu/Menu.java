package fun.feellmoose.bots.game.menu;

import fun.feellmoose.bots.command.menu.TelegramBotMenuCallbackQueryCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Locale;

public interface Menu {
    String id();

    String message();

    Locale locale();

    List<List<Button>> rows();

    default void display(TelegramClient client, Long chatID, Integer topicID) throws TelegramApiException {
        var rows = this.rows().stream()
                .map(list -> new InlineKeyboardRow(
                        list.stream()
                                .map(button -> button.toKeyboardButton(locale()))
                                .toList()
                        )
                ).toList();
        client.execute(SendMessage.builder()
                .text(this.message())
                .chatId(chatID)
                .messageThreadId(topicID)
                .replyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(rows)
                                .build())
                .build());
    }

    default void display(TelegramClient client, TelegramBotMenuCallbackQueryCommand command) throws TelegramApiException {
        var query = command.callbackQuery();
        var data = command.data();
        switch (data.action()) {
            case jump -> {
                var rows = this.rows().stream()
                        .map(list -> new InlineKeyboardRow(
                                        list.stream()
                                                .map(button -> button.toKeyboardButton(locale()))
                                                .toList()
                                )
                        ).toList();
                client.execute(EditMessageText.builder()
                        .chatId(query.getMessage().getChatId())
                        .messageId(query.getMessage().getMessageId())
                        .text(this.message())
                        .build());
                client.execute(EditMessageReplyMarkup.builder()
                        .chatId(query.getMessage().getChatId())
                        .messageId(query.getMessage().getMessageId())
                        .replyMarkup(
                                InlineKeyboardMarkup.builder()
                                        .keyboard(rows)
                                        .build())
                        .build());
            }
            case create -> display(client, query.getMessage().getChatId(), data.topicID());
            case none -> {}
        }
    }

    default void display(TelegramClient client, Message message) throws TelegramApiException {
        var rows = this.rows().stream()
                .map(list -> new InlineKeyboardRow(
                                list.stream()
                                        .map(button -> button.toKeyboardButton(locale()))
                                        .toList()
                        )
                ).toList();
        client.execute(SendMessage.builder()
                .text(this.message())
                .chatId(message.getChatId())
                .messageThreadId(message.getMessageThreadId())
                .replyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(rows)
                                .build())
                .build());
    }
}
