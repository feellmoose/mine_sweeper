package fun.feellmoose.computer;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.IGame;
import fun.feellmoose.core.IUnit;
import fun.feellmoose.core.Step;

import java.util.*;

public class HighProbabilityAlgo implements Algo {
    @Override
    public Step resolve(IGame game) {
        Map<Step, Double> stepProbability = new HashMap<>();
        Map<Step, Integer> stepProbabilityNum = new HashMap<>();
        Step none = null;
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
                around(width,height,i,j,temp,stepProbability,stepProbabilityNum);
                if (none == null && temp[i][j] == -1) none = new Step(i,j);
            }
        }
        List<Map.Entry<Double, Step>> entries = new ArrayList<>();
        if (none!=null) entries.add(Map.entry((double)game.last()/game.unknown(), none));
        stepProbability.forEach((step, p) -> {
            entries.add(Map.entry(p/stepProbabilityNum.get(step),step));
        });
        Step step =  Collections.min(entries,Map.Entry.comparingByKey()).getValue();
        game.onTyped(step.x(),step.y());
        return step;
    }

    private void around(int width, int height, int x, int y, int[][] temp, Map<Step, Double> stepProbability,Map<Step, Integer> stepProbabilityNum) {
        int num = temp[x][y];
        if (num == -1 || num == -2) return;
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
        if (nones.isEmpty() || num <= 0) return;
        Double probability = (double) (num - flags.size()) / nones.size();
        for (var step : nones) {
            stepProbability.compute(step, (key, value) -> {
                if (value == null) return probability;
                return value + probability;
            });
            stepProbabilityNum.compute(step, (key, value) -> {
                if (value == null) return num;
                return value + num;
            });
        }
    }

    private int check(int width, int height, int x, int y, int[][] temp) {
        if( x >= 0 && y >= 0 && x < width && y < height) return temp[x][y];
        return Integer.MIN_VALUE;
    }


}
