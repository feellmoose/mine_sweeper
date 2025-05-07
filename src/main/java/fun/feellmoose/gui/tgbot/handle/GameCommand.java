package fun.feellmoose.gui.tgbot.handle;

import fun.feellmoose.core.GameException;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;

public interface GameCommand {
    String getName();

    void handle(Chat chat, User from, String[] args) throws GameException;
}
