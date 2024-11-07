package fun.feellmoose.computer;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.IUnit;
import fun.feellmoose.core.Step;

import java.util.ArrayList;
import java.util.List;

public class DefaultAlgo implements Algo {
    @Override
    public Step resolve(Game game) {
        int width = game.width();
        int height = game.height();
        IUnit[][] units = game.units();
        int[][] temp = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                temp[i][j] = units[i][j].getFilteredNum();
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Step step = around(width,height,i,j,temp,game);
                if (step != null) return step;
            }
        }
        return null;
    }

    private Step around(int width, int height, int x, int y, int[][] temp, Game game) {
        int num = temp[x][y];
        if (num == -1 || num == -2) return null;
        List<Step> nones = new ArrayList<>();
        List<Step> flags = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i != x || j != y) {
                    switch (check(width, height, i, j, temp)) {
                        case -1 -> nones.add(new Step(i, j));
                        case -2 -> flags.add(new Step(i, j));
                        default -> {}
                    }
                }
            }
        }
        if (nones.isEmpty() || num <= 0) return null;
        if (num == nones.size() + flags.size()) {
            for (Step step : nones) {
                game.onFlag(step.x(), step.y());
                temp[step.x()][step.y()] = -2;
            }
            return null;
        }
        if (num == flags.size()) {
            Step step = nones.getFirst();
            game.onTyped(step.x(), step.y());
            return step;
        }
        return null;
    }

    private int check(int width, int height, int x, int y, int[][] temp) {
        if( x >= 0 && y >= 0 && x < width && y < height) return temp[x][y];
        return Integer.MIN_VALUE;
    }


}
