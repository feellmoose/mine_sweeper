package fun.feellmoose.bots.handler.mine;

import fun.feellmoose.bots.TelegramBotGame;
import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.bots.game.menu.MineGameStartMenu;
import fun.feellmoose.bots.handler.CommandHandler;
import fun.feellmoose.i18n.Messages;
import fun.feellmoose.utils.LocaleUtils;
import fun.feellmoose.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
public class MineRandomCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public MineRandomCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/mine_random";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        Locale locale = LocaleUtils.fromString(message.getFrom().getLanguageCode());
        try {
            MineGameStartMenu.random(message,locale).display(client,message);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to Mine Sweeper Bot", e);
        }
    }
}
