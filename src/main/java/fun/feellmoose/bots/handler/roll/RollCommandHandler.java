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
            case 1 -> {
                int random = ThreadLocalRandom.current().nextInt(10);
                try {
                    client.executeAsync(SendMessage.builder()
                            .chatId(chat.getId())
                            .messageThreadId(message.getMessageThreadId())
                            .text(String.valueOf(random))
                            .replyToMessageId(message.getMessageId())
                            .build());
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
            case 2 -> {
                long max = Long.parseLong(args[1]) + 1;
                long random;
                if (max > 0) random = ThreadLocalRandom.current().nextLong(max + 1);
                else random = 0;
                try {
                    client.executeAsync(SendMessage.builder()
                            .chatId(chat.getId())
                            .messageThreadId(message.getMessageThreadId())
                            .text(String.valueOf(random))
                            .replyToMessageId(message.getMessageId())
                            .build());
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
            case 3 -> {
                long max = Long.parseLong(args[2]) + 1;
                long min = Long.parseLong(args[1]);
                long random;
                if (max > min) random = ThreadLocalRandom.current().nextLong(min, max + 1);
                else random = min;
                try {
                    client.executeAsync(SendMessage.builder()
                            .chatId(chat.getId())
                            .messageThreadId(message.getMessageThreadId())
                            .text(String.valueOf(random))
                            .replyToMessageId(message.getMessageId())
                            .build());
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
            default -> {
            }
        }

    }
}
