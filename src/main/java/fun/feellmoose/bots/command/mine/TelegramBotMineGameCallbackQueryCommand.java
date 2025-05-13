package fun.feellmoose.bots.command.mine;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public record TelegramBotMineGameCallbackQueryCommand(
        TelegramBotMineGameCallbackQueryData data,
        CallbackQuery callbackQuery
) {
    @Nullable
    public static TelegramBotMineGameCallbackQueryCommand of(@Nullable CallbackQuery callbackQuery) {
        if (callbackQuery == null) return null;
        var data = TelegramBotMineGameCallbackQueryData.fromData(callbackQuery.getData());
        if (data == null) return null;
        return new TelegramBotMineGameCallbackQueryCommand(
                data,
                callbackQuery
        );
    }

}
