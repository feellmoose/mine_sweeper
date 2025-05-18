package fun.feellmoose.bots.handler.roll;

import fun.feellmoose.bots.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class RollCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public RollCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/roll";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        switch (args.length) {
            case 3 -> {
                long max;
                long min;
                long random;
                try {
                    max = Long.parseLong(args[2]);
                    min = Long.parseLong(args[1]);
                    if (max> min) random = ThreadLocalRandom.current().nextLong(min, max + 1);
                    else random = min;
                    client.executeAsync(SendMessage.builder()
                            .chatId(chat.getId())
                            .messageThreadId(message.getMessageThreadId())
                            .text("""
                                    Random num from %d to %d is %d
                                    """.formatted(min,max,random))
                            .replyToMessageId(message.getMessageId())
                            .build());
                } catch (NumberFormatException e) {
                    try {
                        client.executeAsync(SendMessage.builder()
                                .chatId(chat.getId())
                                .messageThreadId(message.getMessageThreadId())
                                .text("Usage: /roll <min(inclusive)> <max(inclusive)>" )
                                .replyToMessageId(message.getMessageId())
                                .build());
                    } catch (TelegramApiException ex) {
                        log.error("Error while sending message to Mine Sweeper Bot", ex);
                    }
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
            default -> {
                try {
                    client.executeAsync(SendMessage.builder()
                            .chatId(chat.getId())
                            .messageThreadId(message.getMessageThreadId())
                            .text("Usage: /roll <min(inclusive)> <max(inclusive)>" )
                            .replyToMessageId(message.getMessageId())
                            .build());
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
        }

    }
}
