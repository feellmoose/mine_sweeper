package fun.feellmoose.bots.game.mine.handle;

import fun.feellmoose.user.tgbot.command.InnerBotCommand;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public record TelegramBotMineGameCallbackQueryCommand(
        TelegramBotMineGameCallbackQueryData data,
        CallbackQuery callbackQuery
) implements InnerBotCommand {

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

    @Override public String title() {
        return "v2bsg";
    }

    @Override
    public String command() {
        return "/" + data.action().name();
    }

    @Override
    public String[] args() {
        return callbackQuery.getData().split(":");
    }

}
