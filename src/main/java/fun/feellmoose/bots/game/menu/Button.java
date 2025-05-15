package fun.feellmoose.bots.game.menu;

import fun.feellmoose.bots.command.CallbackQueryData;
import fun.feellmoose.i18n.Messages;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Locale;

public record Button(
            String description,
            CallbackQueryData data
){
    public static Button of(String description, CallbackQueryData data) {
        return new Button(description, data);
    }

    public InlineKeyboardButton toKeyboardButton(Locale locale){
        return InlineKeyboardButton.builder()
                .text(Messages.load(description,locale).template())
                .callbackData(data.data())
                .build();
    }
}