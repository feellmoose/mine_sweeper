package fun.feellmoose.gui.swing;

import fun.feellmoose.computer.AlgoGame;
import fun.feellmoose.computer.DefaultAlgo;
import fun.feellmoose.computer.HighProbabilityAlgo;
import fun.feellmoose.computer.RandomAlgo;
import fun.feellmoose.core.Game;
import fun.feellmoose.core.GameException;
import fun.feellmoose.core.IGame;

import javax.swing.*;
import java.util.concurrent.*;

public class GameGui extends JFrame {
    private GamePanel panel;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
    private ScheduledFuture<?> current;

    private final AlgoGame algoGame = AlgoGame.getInstance()
            .registerAlgo(new DefaultAlgo())
            .registerAlgo(new HighProbabilityAlgo())
            .registerAlgo(new RandomAlgo());
    private static final int DEFAULT_WIDTH = 650;
    private static final int DEFAULT_HEIGHT = 500;

    private GameGui(GamePanel panel) {
        this.panel = panel;
    }

    public static void start() throws GameException {
        GamePanel panel = GamePanel.start(Game.init(9, 9, 10));

        GameGui gui = new GameGui(panel);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        gui.setLocationRelativeTo(null);

        panel.setSize(gui.getWidth(), gui.getHeight());
        gui.add(panel);
        gui.setVisible(true);

        gui.current = gui.scheduled.scheduleAtFixedRate(() -> gui.panel.repaint(),0, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void frameInit() {
        super.frameInit();

        JMenuBar bar = new JMenuBar();

        // handle and log is better here.

        JMenu difficulty = new JMenu("Play");
        JMenuItem easy = new JMenuItem("Easy");
        easy.addActionListener(e -> {
            try {
                restart(Game.init(9, 9, 10));
            } catch (GameException ex) {
                throw new RuntimeException(ex);
            }
        });
        JMenuItem medium = new JMenuItem("Medium");
        medium.addActionListener(e -> {
            try {
                restart(Game.init(16, 16, 40));
            } catch (GameException ex) {
                throw new RuntimeException(ex);
            }
        });
        JMenuItem hard = new JMenuItem("Hard");
        hard.addActionListener(e -> {
            try {
                restart(Game.init(25, 25, 99));
            } catch (GameException ex) {
                throw new RuntimeException(ex);
            }
        });

        JMenu auto = new JMenu("Auto");
        JMenuItem easy_auto = new JMenuItem("Easy");
        easy_auto.addActionListener(e -> {
            try {
                auto(Game.init(9, 9, 10));
            } catch (GameException ex) {
                throw new RuntimeException(ex);
            }
        });
        JMenuItem medium_auto = new JMenuItem("Medium");
        medium_auto.addActionListener(e -> {
            try {
                auto(Game.init(16, 16, 40));
            } catch (GameException ex) {
                throw new RuntimeException(ex);
            }
        });
        JMenuItem hard_auto = new JMenuItem("Hard");
        hard_auto.addActionListener(e -> {
            try {
                auto(Game.init(25, 25, 99));
            } catch (GameException ex) {
                throw new RuntimeException(ex);
            }
        });

        difficulty.setVisible(true);
        difficulty.add(easy);
        difficulty.add(medium);
        difficulty.add(hard);

        auto.setVisible(true);
        auto.add(easy_auto);
        auto.add(medium_auto);
        auto.add(hard_auto);

        bar.add(difficulty);
        bar.add(auto);
        bar.setVisible(true);
        this.setJMenuBar(bar);
    }

    public void auto(IGame game) {
        current.cancel(true);

        GamePanel panel = GamePanel.start(game);
        executor.submit(() -> algoGame.start(panel));
        this.remove(this.panel);
        this.panel = panel;
        panel.setSize(getWidth(), getHeight());
        this.add(panel);

        current = scheduled.scheduleAtFixedRate(() -> this.panel.repaint(),0, 200, TimeUnit.MILLISECONDS);

    }

    public void restart(IGame game) {
        current.cancel(true);

        GamePanel panel = GamePanel.start(game);
        this.remove(this.panel);
        this.panel = panel;
        panel.setSize(getWidth(), getHeight());
        this.add(panel);

        current = scheduled.scheduleAtFixedRate(() -> this.panel.repaint(),0, 200, TimeUnit.MILLISECONDS);
    }


}
