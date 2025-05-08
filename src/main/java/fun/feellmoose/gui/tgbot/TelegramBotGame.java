package fun.feellmoose.gui.tgbot;

import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.handle.GameChatHandler;
import fun.feellmoose.gui.tgbot.handle.GameCommand;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class TelegramBotGame {

    private final String botToken;
    private final TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication();
    private final TelegramGameConsumer gameConsumer;

    private TelegramBotGame(
            String botToken,
            Collection<GameCommand> commands,
            Collection<GameChatHandler> chatHandlers
    ) {
        this.botToken = botToken != null ? botToken: System.getenv("TELEGRAM_BOT_TOKEN");
        this.gameConsumer = new TelegramGameConsumer(commands, chatHandlers);
    }

    public static TelegramBotGameBuilder builder(){
        return new TelegramBotGameBuilder();
    }

    public static class TelegramBotGameBuilder {
        private String botToken;
        private final Collection<GameCommand> commands = new ArrayList<>();
        private final Collection<GameChatHandler> chatHandlers = new ArrayList<>();
        private TelegramBotGameBuilder() {}

        public TelegramBotGame build() {
            return new TelegramBotGame(
                    botToken, commands, chatHandlers
            );
        }

        public TelegramBotGameBuilder registerCommand(GameCommand command){
            commands.add(command);
            return this;
        }

        public TelegramBotGameBuilder removeCommand(GameCommand command){
            commands.remove(command);
            return this;
        }

        public TelegramBotGameBuilder registerChatHandler(GameChatHandler chatHandler){
            chatHandlers.add(chatHandler);
            return this;
        }

        public TelegramBotGameBuilder removerChatHandler(GameChatHandler chatHandler){
            chatHandlers.remove(chatHandler);
            return this;
        }

        public TelegramBotGameBuilder botToken(String botToken) {
            this.botToken = botToken;
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
