package com.qingyou.cli;

import com.qingyou.core.IGame;
import com.qingyou.core.IUnit;
import com.qingyou.core.Step;

import java.util.List;
import java.util.Scanner;

public class GameCli {
    public static void start(IGame game) {
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("--------------------");
            for (IUnit[] unit : game.units()) {
                for (IUnit iUnit : unit) {
                    int num = iUnit.getFilteredNum();
                    switch (num) {
                        case -2 -> System.out.print("F ");
                        case -1 -> System.out.print(". ");
                        default -> System.out.print(num + " ");
                    }

                }
                System.out.print("\n");
            }
            System.out.println("--------------------");
            System.out.println("1. Type 'd' to dig hole.");
            System.out.println("2. Type 'f' add flag.");
            System.out.println("3. Type 'q' to quit.");
            switch (scanner.nextLine()) {
                case "q" -> {
                    System.out.println("See you next time!");
                    System.exit(0);
                }
                case "d" -> {
                    System.out.println("type x");
                    int x = scanner.nextInt();
                    System.out.println("type y");
                    int y = scanner.nextInt();
                    game.onTyped(x, y);
                }
                case "f" -> {
                    System.out.println("type x");
                    int x = scanner.nextInt();
                    System.out.println("type y");
                    int y = scanner.nextInt();
                    game.onFlag(x, y);
                }
            }
        } while (game.status() != IGame.Status.End);

        System.out.println("--------------------");
        IUnit[][] units = game.units();
        List<Step> mines = game.mines();
        for (int i = 0; i < units.length; i++) {
            for (int j = 0; j < units[i].length; j++) {
                if (mines.contains(new Step(i,j))) {
                    System.out.print("* ");
                } else {
                    int num = units[i][j].getFilteredNum();
                    switch (num) {
                        case -2 -> System.out.print("F ");
                        case -1 -> System.out.print(". ");
                        default -> System.out.print(num + " ");
                    }
                }
            }
            System.out.print("\n");
        }
        System.out.println("--------------------");

        if (game.isWin()) System.out.println("You win!");
        else System.out.println("You lose!");
    }
}
