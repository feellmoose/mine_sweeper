package fun.feellmoose.gui.swing;

import fun.feellmoose.core.IGame;
import fun.feellmoose.core.IUnit;
import fun.feellmoose.core.Step;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GamePanel extends JPanel implements IGame {
    private final IGame game;
    private final JTextArea textArea = new JTextArea();

    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 1000;

    private GamePanel(String name, IGame game) {
        this.game = game;
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static GamePanel start(IGame game) {
        GamePanel gamePanel = new GamePanel("wine_sweeper", game);
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() * game.width() / (gamePanel.getWidth() - 150);
                int y = e.getY() * game.height() / gamePanel.getHeight();
                super.mouseClicked(e);
                switch (e.getButton()) {
                    case 1 -> game.onTyped(x, y);
                    case 3 -> game.onFlag(x, y);
                    default -> {
                    }
                }
                gamePanel.repaint();
            }
        });
        gamePanel.setBackground(Color.LIGHT_GRAY);

        gamePanel.add(gamePanel.textArea);
        gamePanel.textArea.setVisible(true);
        gamePanel.textArea.setEditable(false);
        gamePanel.textArea.setOpaque(false);
        gamePanel.textArea.setWrapStyleWord(true);

        gamePanel.setVisible(true);

        return gamePanel;
    }

    @SneakyThrows
    @Override
    public void paint(Graphics g) {

        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics temp = buffer.getGraphics();

        temp.setColor(Color.LIGHT_GRAY);
        int uw = (this.getWidth() - 150) / game.width();
        int uh = this.getHeight() / game.height();
        IUnit[][] units = game.units();

        temp.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < game.width(); i++) {
            for (int j = 0; j < game.height(); j++) {
                int num = units[i][j].getFilteredNum();
                switch (num) {
                    case -1 -> {
                    }
                    case -2 -> {
                        temp.setColor(Color.WHITE);
                        temp.drawRect(uw * i, uh * j, uw, uh);
                        temp.drawImage(getFlagImage(), uw * i, uh * j, uw, uh, null);
                    }
                    default -> temp.drawImage(getImageN(num), uw * i, uh * j, uw, uh, null);
                }
            }
        }
        switch (game.status()) {
            case Init, Running -> {
            }
            case End -> {
                if (game.isWin()) {
                    temp.drawString("win", 0, 0);
                } else {
                    BufferedImage image = ImageIO.read(new File("E:\\my_works\\work_space\\JAVA\\SAST\\mine_sweeper\\src\\main\\resources\\mine.png"));
                    game.mines().forEach(step -> {
                        temp.setColor(Color.WHITE);
                        temp.drawRect(uw * step.x(), uh * step.y(), uw, uh);
                        temp.drawImage(image, uw * step.x(), uh * step.y(), uw, uh, null);
                    });
                }
            }
        }

        temp.setColor(Color.WHITE);
        temp.fillRect(uw * game.width(), 0, this.getWidth(), this.getHeight());
        temp.fillRect(0, uh * game.height(), this.getWidth(), this.getHeight());
        temp.setColor(Color.BLACK);

        StringBuilder sb = new StringBuilder();
        if (game.status() == Status.End) {
            if (game.isWin()) sb.append("You Win!\n");
            else sb.append("You Lose!\n");
        }

        sb.append("Game status: ")
                .append(game.status())
                .append("\n")
                .append("Game start: ")
                .append(game.time().toSeconds())
                .append('.')
                .append(game.time().toMillisPart())
                .append(" s\n")
                .append("Game History:\n");

        game.steps().reversed().forEach(step -> sb.append("\n").append(step));
        textArea.setText(sb.toString());

        temp.translate(uw * game.width() + 2, 10);
        textArea.paint(temp);

        temp.translate(- 2 - uw * game.width(), -10);
        temp.setColor(Color.BLACK);
        for (int i = 0; i <= this.getWidth() - 150; i += uw) {
            temp.drawLine(i, 0, i, uh * game.height());
        }
        for (int i = 0; i <= this.getHeight(); i += uh) {
            temp.drawLine(0, i, uw * game.width(), i);
        }

        g.drawImage(buffer, 0, 0, null);

    }

    @SneakyThrows
    private BufferedImage getImageN(int n) {
        return images.get(n);
    }

    @SneakyThrows
    private static BufferedImage getImage(int n) {
        return ImageIO.read(new File("E:\\my_works\\work_space\\JAVA\\SAST\\mine_sweeper\\src\\main\\resources\\number-" + n + ".png"));
    }

    private static final Map<Integer, BufferedImage> images = Map.of(
            0, getImage(0), 1, getImage(1), 2, getImage(2), 3, getImage(3), 4, getImage(4), 5, getImage(5), 6, getImage(6), 7, getImage(7), 8, getImage(8)
    );

    @SneakyThrows
    private BufferedImage getFlagImage() {
        return ImageIO.read(new File("E:\\my_works\\work_space\\JAVA\\SAST\\mine_sweeper\\src\\main\\resources\\flag.png"));
    }

    @Override
    public int width() {
        return game.width();
    }

    @Override
    public int height() {
        return game.height();
    }

    @Override
    public IUnit[][] units() {
        return game.units();
    }

    @Override
    public List<Step> steps() {
        return game.steps();
    }

    @Override
    public List<Step> mines() {
        return game.mines();
    }

    @Override
    public List<Step> flags() {
        return game.flags();
    }

    @Override
    public int typed() {
        return game.typed();
    }

    @Override
    public int unknown() {
        return game.unknown();
    }

    @Override
    public int last() {
        return game.last();
    }

    @Override
    public boolean onTyped(int x, int y) {
        return game.onTyped(x, y);
    }

    @Override
    public boolean onFlag(int x, int y) {
        return game.onFlag(x, y);
    }

    @Override
    public Status status() {
        return game.status();
    }

    @Override
    public boolean isWin() {
        return game.isWin();
    }

    @Override
    public boolean onEnd(Consumer<Boolean> consumer) {
        return game.onEnd(consumer);
    }

    @Override
    public Duration time() {
        return game.time();
    }
}