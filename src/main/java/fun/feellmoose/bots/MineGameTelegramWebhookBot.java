package fun.feellmoose.bots;

import fun.feellmoose.bots.handler.CallbackQueryHandler;
import fun.feellmoose.bots.handler.ChatHandler;
import fun.feellmoose.bots.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MineGameTelegramWebhookBot implements LongPollingUpdateConsumer {
    private final Collection<CommandHandler> commands;
    private final Collection<CallbackQueryHandler> callbacks;
    private final Collection<ChatHandler> chats;
    private final TelegramClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public MineGameTelegramWebhookBot(Collection<CommandHandler> commands, Collection<CallbackQueryHandler> callbacks, Collection<ChatHandler> chats, TelegramClient client) {
        this.commands = commands;
        this.callbacks = callbacks;
        this.chats = chats;
        this.client = client;
    }

    @Override
    public void consume(List<Update> list) {
        executor.submit(() -> {
            try {
                for (Update update : list) {
                    if (update.hasCallbackQuery()) {
                        CallbackQuery callbackQuery = update.getCallbackQuery();
                        log.debug("received callback: {}", callbackQuery.getData());
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
                            log.debug("received command: {}", text);
                            String[] args = Arrays.stream(text.trim().split(" "))
                                    .filter(s -> !s.isBlank())
                                    .toArray(String[]::new);
                            for (CommandHandler command : commands) {
                                if (command.getName().equals(args[0])
                                        || (command.getName() + "@mine_sweeper_plus_bot").equals(args[0])
                                ) command.handle(message, chat, from, args);
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
            } catch (Throwable e) {
                log.error("Bot execute failed", e);
            }
        });
    }

}
