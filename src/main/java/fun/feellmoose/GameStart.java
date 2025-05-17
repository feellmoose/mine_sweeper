package fun.feellmoose;

import fun.feellmoose.bots.TelegramBotGame;
import fun.feellmoose.bots.game.mine.TelegramBotMineGameApp;
import fun.feellmoose.bots.handler.HelpCommandHandler;
import fun.feellmoose.bots.handler.menu.TelegramBotMenuCallbackQueryHandler;
import fun.feellmoose.bots.handler.mine.*;
import fun.feellmoose.repo.MemoryRepo;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class GameStart {

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(GameStart.class);
        log.info("Prepare resource..");
        String botToken = System.getenv("BOT_TOKEN");

        log.debug("Loaded telegram bot token: [{}]", botToken);

        OkHttpClient client = new OkHttpClient();
        TelegramClient telegramClient = new OkHttpTelegramClient(client, botToken);
        TelegramBotMineGameApp app = new TelegramBotMineGameApp(new MemoryRepo<>());

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .client(client)
                .register(new TelegramBotMineGameCallbackQueryHandler(app, telegramClient))
                .register(new TelegramBotMenuCallbackQueryHandler(telegramClient))
                .register(new MineCommandHandler(telegramClient))
                .register(new MineLevelCommandHandler(telegramClient))
                .register(new MineRandomCommandHandler(telegramClient))
                .register(new HelpCommandHandler(telegramClient))
                .register(new CXGCommandHandler(telegramClient))
                .build();
        try {
            game.start();
            log.info("TelegramBot Game started successfully!");
        } catch (TelegramApiException e) {
            log.error("TelegramBot Game started failed", e);
            System.exit(1);
        }
    }

}