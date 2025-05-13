package fun.feellmoose.user.tgbot.handle.telegram;

import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface ChatHandler {
    void handle(Message message, Chat chat, User from, String[] words);
}
