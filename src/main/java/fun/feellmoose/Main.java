package fun.feellmoose;

import fun.feellmoose.bots.game.mine.TelegramBotMineGameApp;
import fun.feellmoose.bots.handler.HelpCommandHandler;
import fun.feellmoose.bots.handler.mine.*;
import fun.feellmoose.game.mine.core.GameException;
import fun.feellmoose.repo.MemoryRepo;
import fun.feellmoose.bots.TelegramBotGame;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Main {

    public static void main(String[] args) throws GameException {
        log.info("Prepare resource..");
        String botToken = System.getenv("BOT_TOKEN");

        log.debug("Loaded telegram bot token: [{}]", botToken);

        OkHttpTelegramClient client = new OkHttpTelegramClient(botToken);
        TelegramBotMineGameApp app = new TelegramBotMineGameApp(new MemoryRepo<>());

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .client(client)
                .registerCallbackQueryHandler(new TelegramBotMineGameCallbackQueryHandler(app, client))
                .registerCommandHandler(new MineCommandHandler(client))
                .registerCommandHandler(new MineLevelCommandHandler(client))
                .registerCommandHandler(new MineRandomCommandHandler(client))
                .registerCommandHandler(new HelpCommandHandler(client))
                .registerCommandHandler(new CXGCommandHandler(client))
                .build();
        try {
            game.start();
        } catch (TelegramApiException e) {
            log.error("TelegramBot Game started failed", e);
            System.exit(1);
        }
        log.info("TelegramBot Game started successfully!");
    }

}