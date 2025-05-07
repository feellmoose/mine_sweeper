package fun.feellmoose;

import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.TelegramBotGame;
import fun.feellmoose.gui.tgbot.handle.SingleGameCommands;
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

        GameRepo repo = new MemoryGameRepo();
        SinglePlayerGameManager gameManager = new SinglePlayerGameManager(repo);
        OkHttpTelegramClient client = new OkHttpTelegramClient(botToken);

        SingleGameCommands singleGameCommands = new SingleGameCommands(gameManager, client);

        log.info("Starting TelegramBot Game..");

        TelegramBotGame game = TelegramBotGame.builder()
                .botToken(botToken)
                .registerCommand(singleGameCommands.create())
                .registerCommand(singleGameCommands.flag())
                .registerCommand(singleGameCommands.dig())
                .registerCommand(singleGameCommands.quit())
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