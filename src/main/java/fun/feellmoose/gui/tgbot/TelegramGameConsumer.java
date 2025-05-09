package fun.feellmoose.gui.tgbot;

import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.handle.telegram.CallbackQueryHandler;
import fun.feellmoose.gui.tgbot.handle.telegram.CommandHandler;
import fun.feellmoose.gui.tgbot.handle.telegram.ChatHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
    private final Collection<CommandHandler> commands;
    private final Collection<CallbackQueryHandler> callbacks;
    private final Collection<ChatHandler> chats;

    @Override
    public void consume(List<Update> list) {
        for (Update update : list) {
            if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                for (CallbackQueryHandler handler : callbacks) {
                    handler.handle(callbackQuery);
                }
            } else if (update.hasMessage()) {
                Message message = update.getMessage();
                User from = message.getFrom();
                Chat chat = message.getChat();
                if (message.isCommand() && message.hasText()) {
                    //Command here
                    String text = message.getText();
                    String[] args = Arrays.stream(text.trim().split(" "))
                            .filter(s -> !s.isBlank())
                            .toArray(String[]::new);
                    for (CommandHandler command : commands) {
                        if (command.getName().equals(args[0]))
                            command.handle(message, chat, from, args);
                    }
                } else if ((message.isTopicMessage()
                        || message.isGroupMessage()
                        || message.isUserMessage()) && message.hasText()
                ) {
                    log.debug("received chat message: {}", message.getText());
                    //Line words here
                    String text = message.getText();
                    String[] words = Arrays.stream(text.trim().split(" "))
                            .filter(s -> !s.isBlank())
                            .toArray(String[]::new);
                    for (ChatHandler chatHandler : chats) {
                        chatHandler.handle(message, chat, from, words);
                    }
                }
            }
        }
    }


}