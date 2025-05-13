package fun.feellmoose.bots.handler;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler {
    void handle(CallbackQuery query);
}
