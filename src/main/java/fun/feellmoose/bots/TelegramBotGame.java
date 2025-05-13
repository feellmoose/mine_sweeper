package fun.feellmoose.bots;

import fun.feellmoose.game.mine.core.GameException;
import fun.feellmoose.bots.handler.CallbackQueryHandler;
import fun.feellmoose.bots.handler.ChatHandler;
import fun.feellmoose.bots.handler.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Slf4j
public class TelegramBotGame {
    public static final String version = "v0.1.0";
    public static final LocalDateTime updateAt = LocalDateTime.now();

    private final String botToken;
    private final TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication();
    private final TelegramGameConsumer gameConsumer;


    private TelegramBotGame(
            String botToken,
            TelegramClient telegramClient,
            Collection<CommandHandler> commands,
            Collection<CallbackQueryHandler> callbacks,
            Collection<ChatHandler> chats
    ) {
        this.botToken = botToken != null ? botToken : System.getenv("BOT_TOKEN");
        TelegramClient client = telegramClient != null ? telegramClient : new OkHttpTelegramClient(Objects.requireNonNull(botToken));
        this.gameConsumer = new TelegramGameConsumer(client, commands, callbacks, chats);
    }

    public static TelegramBotGameBuilder builder() {
        return new TelegramBotGameBuilder();
    }

    public static class TelegramBotGameBuilder {
        private String botToken;
        private TelegramClient client;
        private final Collection<CommandHandler> commands = new ArrayList<>();
        private final Collection<CallbackQueryHandler> callbacks = new ArrayList<>();
        private final Collection<ChatHandler> chats = new ArrayList<>();

        private TelegramBotGameBuilder() {
        }

        public TelegramBotGame build() {
            return new TelegramBotGame(
                    botToken, client, commands, callbacks, chats
            );
        }

        public TelegramBotGameBuilder registerCommandHandler(CommandHandler command) {
            commands.add(command);
            return this;
        }

        public TelegramBotGameBuilder removeCommandHandler(CommandHandler command) {
            commands.remove(command);
            return this;
        }

        public TelegramBotGameBuilder registerCallbackQueryHandler(CallbackQueryHandler callbackQueryHandler) {
            callbacks.add(callbackQueryHandler);
            return this;
        }

        public TelegramBotGameBuilder removeCallbackQueryHandler(CallbackQueryHandler callbackQueryHandler) {
            callbacks.remove(callbackQueryHandler);
            return this;
        }


        public TelegramBotGameBuilder registerChatHandler(ChatHandler chatHandler) {
            chats.add(chatHandler);
            return this;
        }

        public TelegramBotGameBuilder removeChatHandler(ChatHandler chatHandler) {
            chats.remove(chatHandler);
            return this;
        }

        public TelegramBotGameBuilder botToken(String botToken) {
            this.botToken = botToken;
            return this;
        }

        public TelegramBotGameBuilder client(TelegramClient client) {
            this.client = client;
            return this;
        }
    }

    public void start() throws GameException, TelegramApiException {
        var app = this.application;
        app.registerBot(botToken, gameConsumer);
        if (!app.isRunning()) {
            app.start();
        }
    }
}
