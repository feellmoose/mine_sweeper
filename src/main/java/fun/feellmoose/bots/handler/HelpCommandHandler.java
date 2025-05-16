package fun.feellmoose.bots.handler;

import fun.feellmoose.bots.TelegramBotGame;
import fun.feellmoose.i18n.Messages;
import fun.feellmoose.utils.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.format.DateTimeFormatter;

@Slf4j
public class HelpCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public HelpCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        try {
            client.execute(SendMessage.builder()
                    .chatId(chat.getId())
                    .messageThreadId(message.getMessageThreadId())
                    .text(Messages.load("game.help", LocaleUtils.fromString(from.getLanguageCode()))
                            .formatted(from.getUserName(), TelegramBotGame.version, TelegramBotGame.updateAt.format(DateTimeFormatter.ISO_DATE_TIME)))
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error while sending message to Mine Sweeper Bot", e);
        }
    }
}
