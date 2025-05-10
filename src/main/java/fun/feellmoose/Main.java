package fun.feellmoose;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.TelegramBotGame;
import fun.feellmoose.gui.tgbot.handle.common.ButtonPlayerSweeperGameCommandHandler;
import fun.feellmoose.gui.tgbot.handle.common.InnerBotCommandHandlers;
import fun.feellmoose.gui.tgbot.handle.common.SinglePlayerSweeperGameCommandHandler;
import fun.feellmoose.gui.tgbot.handle.telegram.SingleGameCommandHandlers;
import fun.feellmoose.gui.tgbot.handle.telegram.ButtonPlayerSweeperGameCallbackHandler;
import fun.feellmoose.muti.MemoryRepo;
import fun.feellmoose.muti.Repo;
import fun.feellmoose.muti.ButtonPlayerGameManager;
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

        Repo<Game.SerializedGame> repo = new MemoryRepo<>();
        Repo<SinglePlayerGameManager.AdditionalGameInfo> additional = new MemoryRepo<>();

        SinglePlayerGameManager singlePlayerGameManager = new SinglePlayerGameManager(repo,additional);
        ButtonPlayerGameManager buttonPlayerGameManager = new ButtonPlayerGameManager(repo);
        OkHttpTelegramClient client = new OkHttpTelegramClient(botToken);


        InnerBotCommandHandlers innerBotCommandHandlers = new InnerBotCommandHandlers()
                .register(new ButtonPlayerSweeperGameCommandHandler(buttonPlayerGameManager, client))
                .register(new SinglePlayerSweeperGameCommandHandler(singlePlayerGameManager, client));


        SingleGameCommandHandlers singleGameCommandHandlers = new SingleGameCommandHandlers(innerBotCommandHandlers);
        ButtonPlayerSweeperGameCallbackHandler buttonPlayerSweeperGameCallbackHandler = new ButtonPlayerSweeperGameCallbackHandler(innerBotCommandHandlers,client);

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .client(client)
                .registerCallbackQueryHandler(buttonPlayerSweeperGameCallbackHandler)
                .registerCommandHandler(singleGameCommandHandlers.create())
                .registerCommandHandler(singleGameCommandHandlers.help())
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