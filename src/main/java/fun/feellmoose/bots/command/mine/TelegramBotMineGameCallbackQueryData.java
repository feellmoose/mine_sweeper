package fun.feellmoose.bots.command.mine;

import org.jetbrains.annotations.Nullable;

public record TelegramBotMineGameCallbackQueryData(
        Integer topicID,
        String gameID,
        Long userID,
        Action action,
        int x,
        int y,
        int m
) {
    public enum Action {
        create, dig, flag, quit, change, rollback, none
    }
    /**
     * schema
     * bsg:{gameID}:{userID}:{action}:{x}:{y}:{m}
     * bsg:{topicID}:{userID}:create:{x}:{y}:{m}
     */
    private static final String CREATE_DATA_FORMAT = "v2bsg:%s:%s:create:%d:%d:%d";
    private static final String ROLLBACK_DATA_FORMAT = "v2bsg:%s:%s:rollback:0:0:%d";
    private static final String COMMON_DATA_FORMAT = "v2bsg:%s:%s:%s:%d:%d:0";

    public String data() {
        String topicStrID = topicID == null ? "" : topicID.toString();
        String userStrID = userID == null ? "" : userID.toString();
        return switch (action) {
            case create -> CREATE_DATA_FORMAT.formatted(topicStrID, userStrID, x, y, m);
            case rollback -> ROLLBACK_DATA_FORMAT.formatted(gameID,userStrID, m);
            default -> COMMON_DATA_FORMAT.formatted(gameID, userStrID, action, x, y);
        };
    }

    @Nullable
    public static TelegramBotMineGameCallbackQueryData fromData(@Nullable String data) {
        if (data == null || data.isEmpty()) return null;
        String[] args = data.split(":");
        if (args.length == 7 && args[0].equals("v2bsg")) {
            try {
                Long userID = args[2].isEmpty() ? null : Long.valueOf(args[2]);
                return switch (args[3]){
                    case "create" -> new TelegramBotMineGameCallbackQueryData(
                            args[1].isEmpty() ? null : Integer.valueOf(args[1]),
                            null,
                            userID,
                            Action.create,
                            Integer.parseInt(args[4]),
                            Integer.parseInt(args[5]),
                            Integer.parseInt(args[6])
                    );
                    case "rollback" -> new TelegramBotMineGameCallbackQueryData(
                            null,
                            args[1],
                            userID,
                            Action.rollback,
                            0,
                            0,
                            Integer.parseInt(args[6])
                    );
                    default -> new TelegramBotMineGameCallbackQueryData(
                            null,
                            args[1],
                            userID,
                            Action.valueOf(args[3]),
                            Integer.parseInt(args[4]),
                            Integer.parseInt(args[5]),
                            0
                    );
                };
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}