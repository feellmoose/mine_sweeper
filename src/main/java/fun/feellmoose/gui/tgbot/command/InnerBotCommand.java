package fun.feellmoose.gui.tgbot.command;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface InnerBotCommand {
    String title();
    String command();
    String[] args();
}
