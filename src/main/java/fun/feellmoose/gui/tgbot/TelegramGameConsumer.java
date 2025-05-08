package fun.feellmoose.gui.tgbot;

import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.handle.GameChatHandler;
import fun.feellmoose.gui.tgbot.handle.GameCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class TelegramGameConsumer implements LongPollingUpdateConsumer {
    private final TelegramClient client;
    private final Collection<GameCommand> commands;
    private final Collection<GameChatHandler> chatHandlers;

    @Override
    public void consume(List<Update> list) {
        for (Update update : list) {
            log.debug("Received update [{}]", update);
            if (update.hasMessage()) {
                Message message = update.getMessage();
                User from = message.getFrom();
                Chat chat = message.getChat();
                if (message.isCommand() && message.hasText()) {
                    //Command here
                    String text = message.getText();
                    String[] args = Arrays.stream(text.trim().split(" "))
                            .filter(s -> !s.isBlank())
                            .toArray(String[]::new);
                    for (GameCommand command : commands) {
                        try {
                            if (command.getName().equals(args[0]))
                                command.handle(chat, from, args);
                        } catch (GameException e) {
                            log.error("Game command execute failed",e);
                            try {
                                client.execute(SendMessage.builder()
                                        .chatId(chat.getId())
                                        .text(e.getMessage())
                                        .replyToMessageId(message.getMessageId())
                                        .build());
                            } catch (TelegramApiException ex) {
                                log.error("Sending error message failed",e);
                            }
                        }
                    }
                } else if ((message.isTopicMessage()
                        || message.isGroupMessage()
                        || message.isUserMessage()) && message.hasText()
                ) {
                    //Line words here
                    String text = message.getText();
                    String[] words = Arrays.stream(text.trim().split(" "))
                            .filter(s -> !s.isBlank())
                            .toArray(String[]::new);
                    for (GameChatHandler chatHandler : chatHandlers) {
                        try {
                            chatHandler.handle(chat, from, words);
                        } catch (GameException e) {
                            log.error("Game command execute failed",e);
                            try {
                                client.execute(SendMessage.builder()
                                        .chatId(chat.getId())
                                        .text(e.getMessage())
                                        .replyToMessageId(message.getMessageId())
                                        .build());
                            } catch (TelegramApiException ex) {
                                log.error("Sending error message failed",e);
                            }
                        }
                    }
                }
            }
        }
    }


}