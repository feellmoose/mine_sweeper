package fun.feellmoose.bots.handler.mine;

import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryCommand;
import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.bots.game.mine.BotMineGame;
import fun.feellmoose.bots.game.mine.TelegramBotMineGame;
import fun.feellmoose.bots.game.mine.TelegramBotMineGameApp;
import fun.feellmoose.game.mine.core.GameException;
import fun.feellmoose.bots.handler.CallbackQueryHandler;
import fun.feellmoose.i18n.Messages;
import fun.feellmoose.utils.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TelegramBotMineGameCallbackQueryHandler implements CallbackQueryHandler {
    private final TelegramBotMineGameApp app;
    private final ClassicDisplayer displayer;

    public TelegramBotMineGameCallbackQueryHandler(TelegramBotMineGameApp app, TelegramClient client) {
        this.app = app;
        this.displayer = new ClassicDisplayer(client);
    }

    @Override
    public void handle(CallbackQuery query) {
        var command = TelegramBotMineGameCallbackQueryCommand.of(query);
        if (command == null) return;
        var data = command.data();
        TelegramBotMineGame game = null;
        try {
            if (!Objects.equals(command.data().userID(), query.getFrom().getId())) return;
            game = switch (data.action()) {
                case create -> app.create(
                        query.getFrom().getId(),
                        new TelegramBotMineGameApp.Additional(
                                TelegramBotMineGameApp.Additional.GameType.Classic_Bottom,
                                TelegramBotMineGameApp.Additional.Button.Click,
                                LocaleUtils.fromString(query.getFrom().getLanguageCode()),
                                data.topicID(),
                                query.getMessage().getChatId(),
                                query.getMessage().getMessageId()
                        ), data.x(),data.y(),data.m());
                case cxg -> app.create(
                        query.getFrom().getId(),
                        new TelegramBotMineGameApp.Additional(
                                TelegramBotMineGameApp.Additional.GameType.Classic_Bottom,
                                TelegramBotMineGameApp.Additional.Button.Click,
                                Messages.LOCALE_CN_CXG,
                                data.topicID(),
                                query.getMessage().getChatId(),
                                query.getMessage().getMessageId()
                        ), data.x(),data.y(),data.m());
                case quit -> {
                    app.quit(data.gameID());
                    yield null;
                }
                case dig -> app.dig(data.gameID(), new BotMineGame.Position(data.x(),data.y()));
                case flag -> app.flag(data.gameID(), new BotMineGame.Position(data.x(),data.y()));
                case change -> app.change(data.gameID());
                case rollback -> app.rollback(data.gameID(),1);
                default -> app.query(data.gameID());
            };
            displayer.display(game,command);
        } catch (Exception e) {
            displayer.handle(e,game,query);
        }
    }

    @Slf4j
    private static class ClassicDisplayer {
        private final TelegramClient client;

        public ClassicDisplayer(TelegramClient client) {
            this.client = client;
        }

        public void display(@Nullable TelegramBotMineGame game, TelegramBotMineGameCallbackQueryCommand command) throws TelegramApiException, GameException {
            var query = command.callbackQuery();
            if (game == null) {
                var message = query.getMessage();
                client.execute(
                        EditMessageReplyMarkup.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .replyMarkup(
                                        InlineKeyboardMarkup.builder()
                                                .keyboard(List.of())
                                                .build()
                                ).build()
                );
                return;
            }
            if (game.width() > 8 || game.height() > 8) {
                throw new GameException("Classic View only support width and height between 0 and 8");
            }
            TelegramBotMineGameApp.Additional additional = TelegramBotMineGameApp.Additional.fromMap(game.serialize().infos());
            switch (additional.type()) {
                case Classic_Bottom, Level_Bottom, Random_Bottom -> game.display(client,command);
            }
        }

        public void handle(Exception ex, TelegramBotMineGame game, CallbackQuery query) {
            log.error("Ex handle:", ex);
            if (game == null) {
                log.error("Ex handle: Game in null, cant sent message for user");
                return;
            }
            TelegramBotMineGameApp.Additional additional = TelegramBotMineGameApp.Additional.fromMap(game.serialize().infos());
            switch (ex) {
                case GameException exception -> {
                    log.error("Handle Game Ex", exception);
                    try {
                        client.execute(
                                SendMessage.builder()
                                        .chatId(additional.chat())
                                        .messageThreadId(additional.topic())
                                        .text(Messages.load("game.mine.error", additional.locale())
                                                .formatted(query.getFrom().getUserName(), exception.getMessage()))
                                        .build()
                        );
                    } catch (TelegramApiException e) {
                        log.error("Handle Api Ex, Error sending message", exception);
                    }
                }
                case TelegramApiException exception -> log.error("Handle Api Ex, Error sending message", exception);
                default -> log.error("Handle Common Ex", ex);
            }
        }
    }

}
