package fun.feellmoose.gui.tgbot.handle.telegram;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler {
    void handle(CallbackQuery query);
}
