package fun.feellmoose.gui.tgbot.handle.telegram;

import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.handle.common.InnerBotCommandHandlers;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SinglePlayerSweeperGameCallbackHandler implements CallbackQueryHandler {

    private final InnerBotCommandHandlers handlers;

    public SinglePlayerSweeperGameCallbackHandler(InnerBotCommandHandlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(CallbackQuery query) {

        //schema: single-player-sweeper-game:<gameID>:<option>(<x>,<y>)
        String[] data = query.getData().split(":");
        if (data.length != 3) return;
        if (!data[0].equals("single-player-sweeper-game")) return;

        User user = query.getFrom();
        MaybeInaccessibleMessage message = query.getMessage();
        Chat chat = message.getChat();
        String chatID = chat.getId().toString();
        String gameID = data[1];

        Pattern pattern = Pattern.compile("(\\w+)\\((\\d+),\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(data[2]);

        if (matcher.matches()) {
            String command = matcher.group(1);
            SinglePlayerSweeperGameCommand.Type type = switch (command) {
                case "dig" -> SinglePlayerSweeperGameCommand.Type.dig;
                case "flag" -> SinglePlayerSweeperGameCommand.Type.flag;
                case "quit" -> SinglePlayerSweeperGameCommand.Type.quit;
                case "change" -> SinglePlayerSweeperGameCommand.Type.change;
                default -> null;
            };
            if (type == null) return;
            String x = matcher.group(2);
            String y = matcher.group(3);
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
        } else {
            log.error("Invalid format. Please enter coordinates in (x, y) format, with non-negative integers.");
        }
    }
}
