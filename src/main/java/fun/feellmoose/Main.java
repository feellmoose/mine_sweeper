package fun.feellmoose;


import fun.feellmoose.core.Game;
import fun.feellmoose.gui.swing.GUI;

public class Main {

    public static void main(String[] args) {
        Game game = Game.init(9,9,10);
        GUI.start(game);
//        AlgoGame.getInstance()
//                .registerAlgo(new DefaultAlgo())
//                .registerAlgo(new HighProbabilityAlgo())
//                .registerAlgo(new RandomAlgo())
//                .start(game);
    }

}