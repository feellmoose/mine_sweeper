package fun.feellmoose.bots;

import fun.feellmoose.bots.handler.CallbackQueryHandler;
import fun.feellmoose.bots.handler.ChatHandler;
import fun.feellmoose.bots.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.webhook.TelegramWebhookBot;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
public class MineGameTelegramWebhookBot implements TelegramWebhookBot {
    private final Collection<CommandHandler> commands;
    private final Collection<CallbackQueryHandler> callbacks;
    private final Collection<ChatHandler> chats;
    private final String botToken;

    public MineGameTelegramWebhookBot(String botToken, Collection<CommandHandler> commands, Collection<CallbackQueryHandler> callbacks, Collection<ChatHandler> chats) {
        this.botToken = botToken;
        this.commands = commands;
        this.callbacks = callbacks;
        this.chats = chats;
    }

    @Override
    public String getBotPath() {
        return this.botToken;
    }

    @Override
    public void runDeleteWebhook() {
    }

    @Override
    public void runSetWebhook() {
    }

    @Override
    public BotApiMethod<?> consumeUpdate(Update update) {
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
        return null;
    }

}
