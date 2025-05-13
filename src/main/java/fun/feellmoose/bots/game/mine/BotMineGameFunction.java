package fun.feellmoose.bots.game.mine;

public interface BotMineGameFunction<T extends BotMineGame<T>> {
    T onClicked(BotMineGame.Position position);

    T onFlagged(BotMineGame.Position position);

    T onOptioned(BotMineGame.History history);

    T onRollback(int steps);

    BotMineGame.Serialized<T> serialize();
}
