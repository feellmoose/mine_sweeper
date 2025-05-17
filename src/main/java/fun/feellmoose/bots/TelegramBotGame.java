package fun.feellmoose.bots;

import fun.feellmoose.bots.handler.CallbackQueryHandler;
import fun.feellmoose.bots.handler.ChatHandler;
import fun.feellmoose.bots.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.webhook.TelegramBotsWebhookApplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class TelegramBotGame {
    public static final String version = "v0.1.0";
    public static final LocalDateTime updateAt = LocalDateTime.now();
    private final TelegramBotsWebhookApplication application;


    private TelegramBotGame(
            @NotNull String botToken,
            Collection<CommandHandler> commands,
            Collection<CallbackQueryHandler> callbacks,
            Collection<ChatHandler> chats
    ) throws TelegramApiException {
        var application = new TelegramBotsWebhookApplication();
        application.registerBot(new MineGameTelegramWebhookBot(botToken, commands, callbacks, chats));
        this.application = application;
    }

    public static TelegramBotGameBuilder builder() {
        return new TelegramBotGameBuilder();
    }

    public static class TelegramBotGameBuilder {
        private String botToken;
        private final Collection<CommandHandler> commands = new ArrayList<>();
        private final Collection<CallbackQueryHandler> callbacks = new ArrayList<>();
        private final Collection<ChatHandler> chats = new ArrayList<>();

        private TelegramBotGameBuilder() {
        }

        public TelegramBotGame build() throws TelegramApiException {
            String botToken = this.botToken != null ? this.botToken : System.getenv("BOT_TOKEN");
            return new TelegramBotGame(
                    botToken, commands, callbacks, chats
            );
        }

        public TelegramBotGameBuilder register(CommandHandler command) {
            commands.add(command);
            return this;
        }

        public TelegramBotGameBuilder remove(CommandHandler command) {
            commands.remove(command);
            return this;
        }

        public TelegramBotGameBuilder register(CallbackQueryHandler callback) {
            callbacks.add(callback);
            return this;
        }

        public TelegramBotGameBuilder remove(CallbackQueryHandler callback) {
            callbacks.remove(callback);
            return this;
        }

        public TelegramBotGameBuilder register(ChatHandler chat) {
            chats.add(chat);
            return this;
        }

        public TelegramBotGameBuilder remove(ChatHandler chat) {
            chats.remove(chat);
            return this;
        }

        public TelegramBotGameBuilder botToken(String botToken) {
            this.botToken = botToken;
            return this;
        }

    }

    public void start() throws TelegramApiException {
        synchronized (this.application) {
            if (!this.application.isRunning()) {
                this.application.start();
            }
        }
    }
}
