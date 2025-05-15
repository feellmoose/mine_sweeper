package fun.feellmoose.bots.handler.mine;

import fun.feellmoose.bots.game.menu.MineGameStartMenu;
import fun.feellmoose.bots.handler.CommandHandler;
import fun.feellmoose.utils.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Locale;

@Slf4j
public class MineLevelCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public MineLevelCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/mine_level";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        Locale locale = LocaleUtils.fromString(from.getLanguageCode());
        switch (args.length) {
            case 2 -> {
                try {
                    switch (args[1].toLowerCase()) {
                        case "easy" -> MineGameStartMenu.easy(message, locale).display(client, message);
                        case "normal" -> MineGameStartMenu.normal(message, locale).display(client, message);
                        case "hard" -> MineGameStartMenu.hard(message, locale).display(client, message);
                        default -> client.execute(
                                SendMessage.builder()
                                        .text("@%s [level]: 'easy', 'normal', or 'hard'".formatted(from.getUserName()))
                                        .chatId(chat.getId())
                                        .messageThreadId(message.getMessageThreadId())
                                        .build()
                        );
                    }
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
            case 1 -> {
                try {
                    MineGameStartMenu.level(message, locale).display(client, message);
                } catch (TelegramApiException e) {
                    log.error("Error while sending message to Mine Sweeper Bot", e);
                }
            }
        }
    }

}
