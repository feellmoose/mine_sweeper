package fun.feellmoose.bots.game.menu;

import fun.feellmoose.bots.command.menu.TelegramBotMenuCallbackQueryCommand;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger log = LoggerFactory.getLogger(Menu.class);

    String id();

    String message();

    Type type();

    Locale locale();

    List<List<Button>> rows();

    @Getter
    enum Type{
        TEXT(null),
        MARKDOWN("Markdown"),
        V2MARKDOWN("MarkdownV2"),
        HTML("HTML"),;
        @Nullable
        private final String mode;
        Type(@Nullable String mode) {
            this.mode = mode;
        }
    }

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
                .parseMode(this.type().getMode())
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
                        .parseMode(this.type().getMode())
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
                .parseMode(this.type().getMode())
                .chatId(message.getChatId())
                .messageThreadId(message.getMessageThreadId())
                .replyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(rows)
                                .build())
                .build());
    }
}
