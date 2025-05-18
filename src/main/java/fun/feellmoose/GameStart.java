package fun.feellmoose;

import fun.feellmoose.bots.TelegramBotGame;
import fun.feellmoose.bots.game.mine.TelegramBotMineGameApp;
import fun.feellmoose.bots.handler.HelpCommandHandler;
import fun.feellmoose.bots.handler.menu.TelegramBotMenuCallbackQueryHandler;
import fun.feellmoose.bots.handler.mine.*;
import fun.feellmoose.bots.handler.roll.RollCommandHandler;
import fun.feellmoose.repo.MemoryRepo;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class GameStart {

    public static void main(String[] args) throws TelegramApiException {
        Logger log = LoggerFactory.getLogger(GameStart.class);
        log.info("Prepare resource..");
        String botToken = System.getenv("BOT_TOKEN");
        botToken = botToken == null ? "" : botToken;
        log.info("Loaded telegram bot token: [{}]", botToken);

        TelegramClient client = new OkHttpTelegramClient(new OkHttpClient(), botToken);
        TelegramBotMineGameApp app = new TelegramBotMineGameApp(new MemoryRepo<>());

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .client(client)
                .register(new TelegramBotMineGameCallbackQueryHandler(app, client))
                .register(new TelegramBotMenuCallbackQueryHandler(client))
                .register(new MineCommandHandler(client))
                .register(new MineLevelCommandHandler(client))
                .register(new MineRandomCommandHandler(client))
                .register(new HelpCommandHandler(client))
                .register(new CXGCommandHandler(client))
                .register(new RollCommandHandler(client))
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