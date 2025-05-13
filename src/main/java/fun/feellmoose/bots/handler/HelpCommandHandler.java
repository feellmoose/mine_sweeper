package fun.feellmoose.bots.handler;

import fun.feellmoose.bots.TelegramBotGame;
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
            client.executeAsync(SendMessage.builder()
                    .chatId(chat.getId())
                    .messageThreadId(message.getMessageThreadId())
                    .text("""
                            @%s
                            Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                            Here's a list of commands to get you started:
                            
                            /mine [<width> <height> <mine>]
                            /mine_random
                            /mine_level [level]
                            /help
                            
                            > Mine Sweeper Bot Plus created By feellmoose.
                            > Version %s
                            > Last update at %s
                            """.formatted(from.getUserName(), TelegramBotGame.version, TelegramBotGame.updateAt.format(DateTimeFormatter.ISO_DATE_TIME)))
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error while sending message to Mine Sweeper Bot", e);
        }
    }
}
