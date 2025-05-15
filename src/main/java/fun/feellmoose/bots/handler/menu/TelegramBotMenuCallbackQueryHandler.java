package fun.feellmoose.bots.handler.menu;

import fun.feellmoose.bots.command.menu.TelegramBotMenuCallbackQueryCommand;
import fun.feellmoose.bots.game.menu.Menu;
import fun.feellmoose.bots.game.menu.MineGameStartMenu;
import fun.feellmoose.bots.handler.CallbackQueryHandler;
import fun.feellmoose.utils.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Locale;

@Slf4j
public class TelegramBotMenuCallbackQueryHandler implements CallbackQueryHandler {

    private final TelegramClient client;

    public TelegramBotMenuCallbackQueryHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(CallbackQuery query) {
        var command = TelegramBotMenuCallbackQueryCommand.of(query);
        if (command == null) return;
        Locale locale = LocaleUtils.fromString(query.getFrom().getLanguageCode());
        Menu menu = MineGameStartMenu.of(command,locale);
        if (menu == null) return;
        try {
            menu.display(client,command);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to Mine Sweeper Bot", e);
        }
    }

}
