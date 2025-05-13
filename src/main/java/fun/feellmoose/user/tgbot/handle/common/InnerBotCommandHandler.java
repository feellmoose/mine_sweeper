package fun.feellmoose.user.tgbot.handle.common;

import fun.feellmoose.user.tgbot.command.InnerBotCommand;

public interface InnerBotCommandHandler {
    void handle(InnerBotCommand command);
}
