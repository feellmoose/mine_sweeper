package fun.feellmoose.gui.tgbot.handle.telegram;

import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.handle.common.InnerBotCommandHandlers;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;

import java.util.Arrays;

@Slf4j
public class SinglePlayerSweeperGameCallbackHandler implements CallbackQueryHandler {

    private final InnerBotCommandHandlers handlers;

    public SinglePlayerSweeperGameCallbackHandler(InnerBotCommandHandlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(CallbackQuery query) {
        String queryData = query.getData();
        User user = query.getFrom();
        MaybeInaccessibleMessage message = query.getMessage();
        Chat chat = message.getChat();
        String chatID = chat.getId().toString();

        if (queryData == null) return;
        if (queryData.startsWith("create")) {
            String[] args = queryData.split(":");
            String[] arguments;
            if (args.length == 1) arguments = new String[0];
            else arguments = Arrays.copyOfRange(args, 1, args.length);
            handlers.handle(new SinglePlayerSweeperGameCommand(
                    SinglePlayerSweeperGameCommand.Type.create,
                    arguments,
                    user.getId().toString(),
                    user.getUserName(),
                    chat.getId().toString(),
                    chat.getTitle(),
                    message.getMessageId().toString(),
                    null
            ));
        }

        //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
        //schema: spsg:<gameID>:<option>:<x>:<y>
        String[] data = queryData.split(":");
        if (data.length != 5) return;
        if (!data[0].equals("spsg")) return;
        String gameID = data[1];
        String command = data[2];

        SinglePlayerSweeperGameCommand.Type type = switch (command) {
            case "dig" -> SinglePlayerSweeperGameCommand.Type.dig;
            case "flag" -> SinglePlayerSweeperGameCommand.Type.flag;
            case "quit" -> SinglePlayerSweeperGameCommand.Type.quit;
            case "change" -> SinglePlayerSweeperGameCommand.Type.change;
            default -> null;
        };
        if (type == null) return;
        String x = data[3];
        String y = data[4];
        handlers.handle(new SinglePlayerSweeperGameCommand(
                type,
                new String[]{x, y},
                user.getId().toString(),
                user.getUserName(),
                chatID,
                chat.getTitle(),
                message.getMessageId().toString(),
                gameID
        ));

    }
}
