package fun.feellmoose.core;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game implements IGame {
    private final int width;
    private final int height;
    private final int sum;
    private final Unit[][] units;
    private final List<Step> steps;
    private final List<Step> mines;
    private final Set<Step> flags;

    private Status status = Status.Init;
    private int typed = 0;
    private LocalDateTime start;

    private Game(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.sum = width * height;
        this.steps = new LinkedList<>();
        this.flags = new HashSet<>();
        this.mines = new ArrayList<>(mines);
        if (mines * 2 > sum) throw new GameException("Mines are too many.");
        boolean[][] blocks = new boolean[width][height];
        this.units = new Unit[width][height];
        Random random = ThreadLocalRandom.current();
        for (int temp = mines; temp > 0; ) {
            int x = random.nextInt(width - 1);
            int y = random.nextInt(height - 1);
            if (!blocks[x][y]) {
                blocks[x][y] = true;
                this.mines.add(new Step(x, y));
                temp--;
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                units[i][j] = Unit.init(
                        checkAroundAndGetNumber(width, height, i, j, blocks),
                        blocks[i][j]
                );
            }
        }
    }

    public static Game init(final int width, final int height, int mineNum) {
        return new Game(width, height, mineNum);
    }

    private int checkAroundAndGetNumber(int width, int height, int x, int y, boolean[][] blocks) {
        int num = 0;
        if (checkMine(width, height, x - 1, y - 1, blocks)) num++;
        if (checkMine(width, height, x + 1, y - 1, blocks)) num++;
        if (checkMine(width, height, x - 1, y + 1, blocks)) num++;
        if (checkMine(width, height, x + 1, y + 1, blocks)) num++;
        if (checkMine(width, height, x, y + 1, blocks)) num++;
        if (checkMine(width, height, x, y - 1, blocks)) num++;
        if (checkMine(width, height, x - 1, y, blocks)) num++;
        if (checkMine(width, height, x + 1, y, blocks)) num++;
        return num;
    }

    private boolean checkMine(int width, int height, int x, int y, boolean[][] blocks) {
        return check(width, height, x, y) && blocks[x][y];
    }

    private boolean check(int width, int height, int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public IUnit[][] units() {
        return this.units;
    }

    @Override
    public List<Step> steps() {
        return this.steps;
    }

    @Override
    public List<Step> mines() {
        if (this.status == Status.End) return this.mines;
        return Collections.emptyList();
    }

    @Override
    public List<Step> flags() {
        return flags.stream().toList();
    }

    @Override
    public int typed() {
        return this.typed;
    }

    @Override
    public int last() {
        return mines.size() - flags.size();
    }

    @Override
    public int unknown() {
        return sum - flags.size() - typed;
    }

    @Override
    public Status status() {
        return this.status;
    }

    @Override
    public boolean isWin() {
        return status == Status.End && typed + mines.size() == sum;
    }

    @Override
    public boolean onTyped(int x, int y) {
        if (!check(width, height, x, y)) return false;
        if (refreshStatus()) return false;
        Unit u = units[x][y];
        switch (u.status()) {
            case None -> {
                steps.add(new Step(x, y));
                if (u.isMine()) {
                    //Typed mine ... boom! Game is Over.
                    this.status = Status.End;
                    return false;
                }
                u.setStatus(IUnit.Status.Typed);
                typed++;
                if (u.num() == 0) typedZeroFrom(x, y);
            }
            case Flag, Typed -> {
                return false;
            }
        }
        if (typed + mines.size() == sum) this.status = Status.End;
        return true;
    }

    private void typedZeroFrom(int x, int y) {
        List<Step> stack = new LinkedList<>();
        Set<Step> visited = new HashSet<>();
        List<Step> next;
        if (check(width, height, x - 1, y)) stack.add(new Step(x - 1, y));
        if (check(width, height, x + 1, y)) stack.add(new Step(x + 1, y));
        if (check(width, height, x, y - 1)) stack.add(new Step(x, y - 1));
        if (check(width, height, x, y + 1)) stack.add(new Step(x, y + 1));
        while (!stack.isEmpty()) {
            next = new LinkedList<>();
            for (Step step : stack) {
                int sx = step.x();
                int sy = step.y();
                if (check(width, height, sx, sy)) {
                    Unit u = units[sx][sy];
                    if (u.status() == IUnit.Status.None && !u.isMine()) {
                        u.setStatus(IUnit.Status.Typed);
                        typed++;
                        if (u.num() == 0) {
                            Step up = new Step(sx - 1, sy);
                            Step down = new Step(sx + 1, sy);
                            Step left = new Step(sx, sy - 1);
                            Step right = new Step(sx, sy + 1);
                            if (check(width, height, sx - 1, sy) && !visited.contains(up)) next.add(up);
                            if (check(width, height, sx + 1, sy) && !visited.contains(down)) next.add(down);
                            if (check(width, height, sx, sy - 1) && !visited.contains(left)) next.add(left);
                            if (check(width, height, sx, sy + 1) && !visited.contains(right)) next.add(right);
                        }
                    }
                }
            }
            visited.addAll(stack);
            stack = next;
        }
    }

    @Override
    public boolean onFlag(int x, int y) {
        if (!check(width, height, x, y)) return false;
        if (refreshStatus()) return false;
        Unit u = units[x][y];
        switch (u.status()) {
            case None -> {
                u.setStatus(IUnit.Status.Flag);
                flags.add(new Step(x, y));
            }
            case Flag -> {
                u.setStatus(IUnit.Status.None);
                flags.remove(new Step(x, y));
            }
            case Typed -> {
                return false;
            }
        }
        return true;
    }

    private boolean refreshStatus() {
        return !switch (status) {
            case Init -> {
                status = Status.Running;
                start = LocalDateTime.now();
                yield true;
            }
            case Running -> true;
            case End -> false;
        };
    }

}
