package fun.feellmoose;

import fun.feellmoose.cli.GameCli;
import fun.feellmoose.computer.*;
import fun.feellmoose.core.Game;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        algoTest();
    }

    public static void startCli(){
        Game game = Game.init(10, 10, 10);
        GameCli.start(game);
    }

    public static void algoTest(){
        int win = 0;
        int sum = 10;
        int width = 100;
        int height = 100;
        int mineNum = 50;
        LocalDateTime startTime = LocalDateTime.now();
        for (int i = 0; i < sum; i++) {
            Game game = Game.init(width, height, mineNum);
            AlgoGame.getInstance()
                    .registerAlgo(new DefaultAlgo())
                    .registerAlgo(new HighProbabilityAlgo())
                    .registerAlgo(new RandomAlgo())
                    .startWithUsage(game).printSummary();
            if (game.isWin()) win++;
        }
        System.out.println("Win:" + win + "/" + sum);
        System.out.println("Time:" + Duration.between(startTime, LocalDateTime.now()).toMillis() + " ms");
    }
}