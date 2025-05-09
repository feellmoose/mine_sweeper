package fun.feellmoose.gui.tgbot.handle.telegram;

import fun.feellmoose.core.GameException;
import fun.feellmoose.gui.tgbot.command.SinglePlayerSweeperGameCommand;
import fun.feellmoose.gui.tgbot.handle.common.InnerBotCommandHandlers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Arrays;

@Slf4j
public class SingleGameCommandHandlers {

    private final InnerBotCommandHandlers handlers;

    public SingleGameCommandHandlers(InnerBotCommandHandlers handlers) {
        this.handlers = handlers;
    }

    public Create create() {
        return new Create(handlers);
    }

    public Dig dig() {
        return new Dig(handlers);
    }

    public Flag flag() {
        return new Flag(handlers);
    }

    public Quit quit() {
        return new Quit(handlers);
    }

    public Help help() {
        return new Help(handlers);
    }

    @AllArgsConstructor
    public static class Create implements CommandHandler {

        private final InnerBotCommandHandlers handlers;

        @Override
        public String getName() {
            return "/create";
        }

        @Override
        public void handle(Message message, Chat chat, User from, String[] args) {
            String[] arguments;
            if (args.length == 1) arguments = new String[0];
            else arguments = Arrays.copyOfRange(args, 1, args.length);
            handlers.handle(new SinglePlayerSweeperGameCommand(
                    SinglePlayerSweeperGameCommand.Type.create,
                    arguments,
                    from.getId().toString(),
                    from.getUserName(),
                    chat.getId().toString(),
                    chat.getTitle(),
                    null,
                    null
            ));
        }
    }

    @AllArgsConstructor
    public static class Dig implements CommandHandler {

        private final InnerBotCommandHandlers handlers;

        @Override
        public String getName() {
            return "/dig";
        }

        @Override
        public void handle(Message message, Chat chat, User from, String[] args) {
            String[] arguments;
            if (args.length == 1) arguments = new String[0];
            else arguments = Arrays.copyOfRange(args, 1, args.length);
            handlers.handle(new SinglePlayerSweeperGameCommand(
                    SinglePlayerSweeperGameCommand.Type.dig,
                    arguments,
                    from.getId().toString(),
                    from.getUserName(),
                    chat.getId().toString(),
                    chat.getTitle(),
                    null,
                    null
            ));
        }

    }

    @AllArgsConstructor
    public static class Flag implements CommandHandler {

        private final InnerBotCommandHandlers handlers;

        @Override
        public String getName() {
            return "/flag";
        }

        @Override
        public void handle(Message message, Chat chat, User from, String[] args) {
            String[] arguments;
            if (args.length == 1) arguments = new String[0];
            else arguments = Arrays.copyOfRange(args, 1, args.length);
            handlers.handle(new SinglePlayerSweeperGameCommand(
                    SinglePlayerSweeperGameCommand.Type.flag,
                    arguments,
                    from.getId().toString(),
                    from.getUserName(),
                    chat.getId().toString(),
                    chat.getTitle(),
                    null,
                    null
            ));
        }

    }

    @AllArgsConstructor
    public static class Quit implements CommandHandler {

        private final InnerBotCommandHandlers handlers;

        @Override
        public String getName() {
            return "/quit";
        }

        @Override
        public void handle(Message message, Chat chat, User from, String[] args) {
            String[] arguments;
            if (args.length == 1) arguments = new String[0];
            else arguments = Arrays.copyOfRange(args, 1, args.length);
            handlers.handle(new SinglePlayerSweeperGameCommand(
                    SinglePlayerSweeperGameCommand.Type.quit,
                    arguments,
                    from.getId().toString(),
                    from.getUserName(),
                    chat.getId().toString(),
                    chat.getTitle(),
                    null,
                    null
            ));
        }
    }

    @AllArgsConstructor
    public static class Help implements CommandHandler {

        private final InnerBotCommandHandlers handlers;

        @Override
        public String getName() {
            return "/help";
        }

        @Override
        public void handle(Message message, Chat chat, User from, String[] args) {
            String[] arguments;
            if (args.length == 1) arguments = new String[0];
            else arguments = Arrays.copyOfRange(args, 1, args.length);
            handlers.handle(new SinglePlayerSweeperGameCommand(
                    SinglePlayerSweeperGameCommand.Type.help,
                    arguments,
                    from.getId().toString(),
                    from.getUserName(),
                    chat.getId().toString(),
                    chat.getTitle(),
                    null,
                    null
            ));
        }

    }

}
