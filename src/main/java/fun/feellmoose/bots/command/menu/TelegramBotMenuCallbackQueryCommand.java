package fun.feellmoose.bots.command.menu;

import fun.feellmoose.bots.command.CallbackQueryData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public record TelegramBotMenuCallbackQueryCommand(
        TelegramBotMenuCallbackQueryData data,
        CallbackQuery callbackQuery
) {
    @Nullable
    public static TelegramBotMenuCallbackQueryCommand of(@Nullable CallbackQuery callbackQuery) {
        if (callbackQuery == null) return null;
        var data = TelegramBotMenuCallbackQueryData.fromData(callbackQuery.getData());
        if (data == null) return null;
        return new TelegramBotMenuCallbackQueryCommand(
                data,
                callbackQuery
        );
    }

}