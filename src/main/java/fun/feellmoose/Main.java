package fun.feellmoose;

import fun.feellmoose.cli.GameCli;
import fun.feellmoose.core.Game;
import fun.feellmoose.gui.swing.MainWindow;

public class Main {

    public static void main(String[] args) {
        MainWindow.start(Game.init(9, 9, 10));
    }
}