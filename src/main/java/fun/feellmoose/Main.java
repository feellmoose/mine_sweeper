package fun.feellmoose;

import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.TelegramBotGame;
import fun.feellmoose.gui.tgbot.handle.common.InnerBotCommandHandlers;
import fun.feellmoose.gui.tgbot.handle.common.SinglePlayerSweeperGameCommandHandler;
import fun.feellmoose.gui.tgbot.handle.telegram.SingleGameCommandHandlers;
import fun.feellmoose.gui.tgbot.handle.telegram.SinglePlayerSweeperGameCallbackHandler;
import fun.feellmoose.muti.GameRepo;
import fun.feellmoose.muti.MemoryGameRepo;
import fun.feellmoose.muti.SinglePlayerGameManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Main {

    public static void main(String[] args) throws GameException {
        log.info("Prepare resource..");
        String botToken = System.getenv("BOT_TOKEN");

        log.debug("Loaded telegram bot token: [{}]", botToken);

        GameRepo repo = new MemoryGameRepo();
        SinglePlayerGameManager gameManager = new SinglePlayerGameManager(repo);
        OkHttpTelegramClient client = new OkHttpTelegramClient(botToken);


        InnerBotCommandHandlers innerBotCommandHandlers = new InnerBotCommandHandlers()
                .register(new SinglePlayerSweeperGameCommandHandler(gameManager, client));


        SingleGameCommandHandlers singleGameCommandHandlers = new SingleGameCommandHandlers(innerBotCommandHandlers);
        SinglePlayerSweeperGameCallbackHandler singlePlayerSweeperGameCallbackHandler = new SinglePlayerSweeperGameCallbackHandler(innerBotCommandHandlers);

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .registerCallbackQueryHandler(singlePlayerSweeperGameCallbackHandler)
                .registerCommandHandler(singleGameCommandHandlers.create())
                .registerCommandHandler(singleGameCommandHandlers.flag())
                .registerCommandHandler(singleGameCommandHandlers.dig())
                .registerCommandHandler(singleGameCommandHandlers.quit())
                .build();
        try {
            game.start();
        }catch (TelegramApiException e){
            log.error("TelegramBot Game started failed",e);
            System.exit(1);
        }
        log.info("TelegramBot Game started successfully!");
    }

}