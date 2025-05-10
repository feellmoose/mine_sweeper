package fun.feellmoose.gui.tgbot.command;


import fun.feellmoose.gui.tgbot.command.data.ButtonQueryDataText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public record ButtonPlayerSweeperGameCommand(
        ButtonQueryDataText text,
        String[] args,
        CallbackQuery callbackQuery
) implements InnerBotCommand {

    @Override public String title() {
        return "spsg";
    }

    @Override
    public String command() {
        return text.getType().command;
    }

    public enum Type{
        create("/create"),
        dig("/dig"),
        flag("/flag"),
        quit("/quit"),
        change("/change");
        private final String command;

        Type(String command) {
            this.command = command;
        }
    }



}
