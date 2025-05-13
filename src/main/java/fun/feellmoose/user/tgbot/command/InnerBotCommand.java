package fun.feellmoose.user.tgbot.command;

public interface InnerBotCommand {
    String title();
    String command();
    String[] args();
}
