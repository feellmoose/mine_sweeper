package fun.feellmoose.user.tgbot.handle.telegram;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler {
    void handle(CallbackQuery query);
}
