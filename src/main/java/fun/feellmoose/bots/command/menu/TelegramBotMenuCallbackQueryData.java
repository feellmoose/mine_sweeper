package fun.feellmoose.bots.command.menu;

import fun.feellmoose.bots.command.CallbackQueryData;
import org.jetbrains.annotations.Nullable;

public record TelegramBotMenuCallbackQueryData(
        Action action,
        Integer topicID,
        Long userID,
        String menuID
) implements CallbackQueryData {
    public enum Action {
        create, jump, none
    }
    /**
     * schema
     * bm:jump:{topicID}:{userID}:{menuID}
     * bm:create:{topicID}:{userID}:{menuID}
     */
    private static final String MENU_DATA_FORMAT = "bm:%s:%s:%s:%s";

    @Override
    public String data() {
        String topicStrID = topicID == null ? "" : topicID.toString();
        String userStrID = userID == null ? "" : userID.toString();
        return MENU_DATA_FORMAT.formatted(action.name(), topicStrID, userStrID, menuID);
    }

    @Nullable
    public static TelegramBotMenuCallbackQueryData fromData(@Nullable String data) {
        if (data == null || data.isEmpty()) return null;
        String[] args = data.split(":");
        if (args.length == 5 && args[0].equals("bm")) {
            try {
                return new TelegramBotMenuCallbackQueryData(
                        Action.valueOf(args[1]),
                        args[2].isEmpty() ? null : Integer.valueOf(args[2]),
                        args[3].isEmpty() ? null : Long.valueOf(args[3]),
                        args[4]
                );
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}