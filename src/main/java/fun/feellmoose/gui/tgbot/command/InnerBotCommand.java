package fun.feellmoose.gui.tgbot.command;

public interface InnerBotCommand {
    String title();
    String command();
    String[] args();
    String userID();
    String username();
    String chatID();
    String chatName();
}
