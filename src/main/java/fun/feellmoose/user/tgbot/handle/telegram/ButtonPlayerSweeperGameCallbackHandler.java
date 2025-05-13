package fun.feellmoose.user.tgbot.handle.telegram;

import fun.feellmoose.user.tgbot.command.ButtonPlayerSweeperGameCommand;
import fun.feellmoose.user.tgbot.command.data.ButtonQueryDataText;
import fun.feellmoose.user.tgbot.handle.common.InnerBotCommandHandlers;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;

@Slf4j
public class ButtonPlayerSweeperGameCallbackHandler implements CallbackQueryHandler {

    private final InnerBotCommandHandlers handlers;
    private final TelegramClient client;

    public ButtonPlayerSweeperGameCallbackHandler(InnerBotCommandHandlers handlers, TelegramClient client) {
        this.handlers = handlers;
        this.client = client;
    }

    @Override
    public void handle(CallbackQuery query) {
        String queryData = query.getData();

        if (queryData == null) return;
        if (!queryData.startsWith("bsg")) return;
        String[] args = queryData.split(":");
        String[] arguments;
        if (args.length == 1) arguments = new String[0];
        else arguments = Arrays.copyOfRange(args, 1, args.length);

        ButtonQueryDataText data = ButtonQueryDataText.fromData(queryData);
        if (data != null) {
            handlers.handle(new ButtonPlayerSweeperGameCommand(
                    data,
                    arguments,
                    query
            ));
        }

    }
}
