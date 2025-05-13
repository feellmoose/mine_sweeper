package fun.feellmoose.user.tgbot.command.data;

import fun.feellmoose.user.tgbot.command.ButtonPlayerSweeperGameCommand;

public record ButtonQueryDataText(
            Integer topicID,
            Long userID,
            String gameID,
            String action,
            int x,
            int y,
            int m
    ) {
        /**
         * schema
         * bsg:{topic}:{userID}:{gameID}:{action}:{x}:{y}
         * bsg:{topic}:{userID}:create:{x}:{y}:{m}
         */
        private static final String CREATE_DATA_FORMAT = "bsg:%s:%s:create:%d:%d:%d";
        private static final String COMMON_DATA_FORMAT = "bsg:%s:%s:%s:%s:%d:%d";

        public ButtonPlayerSweeperGameCommand.Type getType(){
            return switch(action){
                case "create" -> ButtonPlayerSweeperGameCommand.Type.create;
                case "dig" -> ButtonPlayerSweeperGameCommand.Type.dig;
                case "flag" -> ButtonPlayerSweeperGameCommand.Type.flag;
                case "quit" -> ButtonPlayerSweeperGameCommand.Type.quit;
                case "change" -> ButtonPlayerSweeperGameCommand.Type.change;
                default -> throw new IllegalStateException("Unexpected value: " + action);
            };
        }

        public String getData() {
            String topicStrID = topicID == null ? "" : topicID.toString();
            String userStrID = userID == null ? "" : userID.toString();

            return switch (action) {
                case "create" -> CREATE_DATA_FORMAT.formatted(topicStrID, userStrID, x, y, m);
                default -> COMMON_DATA_FORMAT.formatted(topicStrID, userStrID, gameID, action, x, y);
            };
        }

        public static ButtonQueryDataText fromData(String data) {
            String[] args = data.split(":");
            if (args.length == 7 && args[0].equals("bsg")) {
                try {
                    Integer topicID = args[1].isEmpty() ? null : Integer.valueOf(args[1]);
                    Long userID = args[2].isEmpty() ? null : Long.valueOf(args[2]);
                    if (args[3].equals("create")) {
                        return new ButtonQueryDataText(
                                topicID,
                                userID,
                                null,
                                args[3],
                                Integer.parseInt(args[4]),
                                Integer.parseInt(args[5]),
                                Integer.parseInt(args[6])
                        );
                    }

                    return new ButtonQueryDataText(
                            topicID,
                            userID,
                            args[3],
                            args[4],
                            Integer.parseInt(args[5]),
                            Integer.parseInt(args[6]),
                            0
                    );
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
    }