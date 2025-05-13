package fun.feellmoose;

import fun.feellmoose.bots.game.mine.TelegramBotMineGameApp;
import fun.feellmoose.bots.game.mine.handle.TelegramBotMineGameCallbackQueryHandler;
import fun.feellmoose.game.mine.core.Game;
import fun.feellmoose.game.mine.core.GameException;
import fun.feellmoose.user.tgbot.TelegramBotGame;
import fun.feellmoose.user.tgbot.handle.common.ButtonPlayerSweeperGameCommandHandler;
import fun.feellmoose.user.tgbot.handle.common.InnerBotCommandHandlers;
import fun.feellmoose.user.tgbot.handle.common.SinglePlayerSweeperGameCommandHandler;
import fun.feellmoose.user.tgbot.handle.telegram.MineCreateCommandHandler;
import fun.feellmoose.user.tgbot.handle.telegram.SingleGameCommandHandlers;
import fun.feellmoose.user.tgbot.handle.telegram.ButtonPlayerSweeperGameCallbackHandler;
import fun.feellmoose.muti.repo.MemoryRepo;
import fun.feellmoose.muti.repo.Repo;
import fun.feellmoose.muti.ButtonPlayerGameManager;
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

        ButtonPlayerGameManager buttonPlayerGameManager = new ButtonPlayerGameManager(repo);
        OkHttpTelegramClient client = new OkHttpTelegramClient(botToken);

        InnerBotCommandHandlers innerBotCommandHandlers = new InnerBotCommandHandlers()
                .register(new ButtonPlayerSweeperGameCommandHandler(buttonPlayerGameManager, client))
                .register(new SinglePlayerSweeperGameCommandHandler(client));

        SingleGameCommandHandlers singleGameCommandHandlers = new SingleGameCommandHandlers(innerBotCommandHandlers);
        ButtonPlayerSweeperGameCallbackHandler buttonPlayerSweeperGameCallbackHandler = new ButtonPlayerSweeperGameCallbackHandler(innerBotCommandHandlers,client);

        TelegramBotMineGameApp app = new TelegramBotMineGameApp(new MemoryRepo<>());
        TelegramBotMineGameCallbackQueryHandler handler = new TelegramBotMineGameCallbackQueryHandler(app,client);
        MineCreateCommandHandler commandHandler = new MineCreateCommandHandler(client);

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .client(client)
                .registerCallbackQueryHandler(buttonPlayerSweeperGameCallbackHandler)
                .removerCallbackQueryHandler(handler)
                .registerCommandHandler(singleGameCommandHandlers.create())
                .registerCommandHandler(singleGameCommandHandlers.help())
                .removeCommandHandler(commandHandler)
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