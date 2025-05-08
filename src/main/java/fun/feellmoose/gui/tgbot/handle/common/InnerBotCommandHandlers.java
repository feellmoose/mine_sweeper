package fun.feellmoose.gui.tgbot.handle.common;

import fun.feellmoose.gui.tgbot.command.InnerBotCommand;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class InnerBotCommandHandlers implements InnerBotCommandHandler {

    private final Set<InnerBotCommandHandler> innerBotCommandHandlers = new HashSet<>();

    public InnerBotCommandHandlers register(InnerBotCommandHandler handler) {
        innerBotCommandHandlers.add(handler);
        return this;
    }

    public InnerBotCommandHandlers registerAll(Collection<InnerBotCommandHandler> handler) {
        innerBotCommandHandlers.addAll(handler);
        return this;
    }

    public InnerBotCommandHandlers remove(InnerBotCommandHandler handler) {
        innerBotCommandHandlers.remove(handler);
        return this;
    }

    @Override
    public void handle(InnerBotCommand command) {
        innerBotCommandHandlers.forEach(handler -> handler.handle(command));
    }
}
