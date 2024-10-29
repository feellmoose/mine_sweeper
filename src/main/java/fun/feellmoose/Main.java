package fun.feellmoose;

import fun.feellmoose.cli.GameCli;
import fun.feellmoose.core.Game;

public class Main {

    public static void main(String[] args) {
        GameCli.start(Game.init(100, 100, 1));
    }
}