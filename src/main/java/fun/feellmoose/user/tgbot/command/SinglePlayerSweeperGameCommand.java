package fun.feellmoose.user.tgbot.command;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public record SinglePlayerSweeperGameCommand(
        Type type,
        String[] args,
        Message message,
        String gameID
) implements InnerBotCommand {

    @Override public String title() {
        return "spsg";
    }

    @Override
    public String command() {
        return type.command;
    }

    public enum Type{
        create("/create"),
        dig("/dig"),
        flag("/flag"),
        quit("/quit"),
        help("/help");
        private final String command;

        Type(String command) {
            this.command = command;
        }
    }

}