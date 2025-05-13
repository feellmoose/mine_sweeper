package fun.feellmoose.bots.handler.mine;

import fun.feellmoose.bots.command.mine.TelegramBotMineGameCallbackQueryData;
import fun.feellmoose.bots.handler.CommandHandler;
import fun.feellmoose.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
public class MineRandomCommandHandler implements CommandHandler {

    private final TelegramClient client;

    public MineRandomCommandHandler(TelegramClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "/mine_random";
    }

    @Override
    public void handle(Message message, Chat chat, User from, String[] args) {
        int width = ThreadLocalRandom.current().nextInt(3, 8);
        int height = ThreadLocalRandom.current().nextInt(3, 8);
        int total = width * height;
        Function<Double,Double> refactor = (raw) -> {
            if (raw > 0.7) return Math.pow(raw, 3.0);
            if (raw < 0.2) return Math.pow(raw, 0.5);
            return raw;
        };
        double density = RandomUtils.randomDensity(0.10, 0.25, refactor);
        int mines = (int) (density * total);
        try {
            startClassic(message,chat,from,width,height,mines);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to Mine Sweeper Bot", e);
        }
    }

    private void startClassic(Message message, Chat chat, User from, int x, int y, int mine) throws TelegramApiException {
        var row = new InlineKeyboardRow();
        row.add(
                InlineKeyboardButton.builder()
                        .text("Classic")
                        .callbackData(new TelegramBotMineGameCallbackQueryData(
                                message.getMessageThreadId(),
                                null,
                                from.getId(),
                                TelegramBotMineGameCallbackQueryData.Action.create,
                                x,y,mine
                        ).data())
                        .build()
        );
        client.executeAsync(
                SendMessage.builder()
                        .chatId(chat.getId())
                        .messageThreadId(message.getMessageThreadId())
                        .text("""
                                        @%s
                                        Hey there! 👋 Thanks for choosing Mine Sweeper Bot Plus!
                                        You have started a new %d × %d game with %d mines.
                                        """.formatted(from.getUserName(),x,y,mine))
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboard(List.of(
                                        row
                                )).build())
                        .build()
        );
    }
}
