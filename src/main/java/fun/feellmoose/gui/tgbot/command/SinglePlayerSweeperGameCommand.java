package fun.feellmoose.gui.tgbot.command;


public record SinglePlayerSweeperGameCommand(
        Type type,
        String[] args,
        String userID,
        String username,
        String chatID,
        String chatName,
        String messageID,
        String gameID
) implements InnerBotCommand {

    @Override public String title() {
        return "single-player-sweeper-game";
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
        change("/change"),
        help("/help");
        private final String command;

        Type(String command) {
            this.command = command;
        }
    }

}
