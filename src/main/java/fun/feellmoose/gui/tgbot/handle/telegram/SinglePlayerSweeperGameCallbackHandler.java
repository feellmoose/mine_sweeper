package fun.feellmoose.gui.tgbot.handle.telegram;

import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.handle.common.InnerBotCommandHandlers;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class SinglePlayerSweeperGameCallbackHandler implements CallbackQueryHandler {

    private final InnerBotCommandHandlers handlers;
    private final TelegramClient client;

    public SinglePlayerSweeperGameCallbackHandler(InnerBotCommandHandlers handlers, TelegramClient client) {
        this.handlers = handlers;
        this.client = client;
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

        if (type == SinglePlayerSweeperGameCommand.Type.quit) {
            try {
                client.executeAsync(
                        EditMessageReplyMarkup.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .replyMarkup(InlineKeyboardMarkup.builder()
                                        .keyboard(List.of()).build()
                                ).build()
                );
            }catch (Exception e){
                log.error("Error while quit clean button", e);
            }
        }

    }
}
