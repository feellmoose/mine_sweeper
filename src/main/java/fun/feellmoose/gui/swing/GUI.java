package fun.feellmoose.gui.swing;

import fun.feellmoose.core.IGame;
import fun.feellmoose.core.IUnit;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

public class GUI extends JFrame {
    private final IGame game;

    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 1000;

    private GUI(String name, IGame game) {
        super(name);
        this.game = game;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static void start(IGame game){
        GUI gui = new GUI("wine_sweeper", game);
        gui.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() * game.width() / gui.getWidth();
                int y = e.getY() * game.height() / gui.getHeight();
                super.mouseClicked(e);
                switch (e.getButton()) {
                    case 1 -> game.onTyped(x,y);
                    case 3 -> game.onFlag(x,y);
                    default -> {}
                }
                gui.repaint();
            }
        });
        gui.setBackground(Color.LIGHT_GRAY);
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);

    }

    @SneakyThrows
    @Override
    public void paint(Graphics g) {
        int uw = this.getWidth() / game.width();
        int uh = this.getHeight() / game.height();
        IUnit[][] units = game.units();
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0,0,getWidth(),getHeight());
        for (int i = 0; i < game.width(); i++) {
            for (int j = 0; j < game.height(); j++) {
                int num = units[i][j].getFilteredNum();
                switch (num) {
                    case -1 ->  {}
                    case -2 ->  g.drawImage(getFlagImage(),uw * i,uh * j,uw,uh,null );
                    default ->  g.drawImage(getImageN(num),uw * i,uh * j,uw,uh,null );
                }
            }
        }
        switch (game.status()){
            case Init, Running -> {}
            case End -> {
                if (game.isWin()) {
                    g.drawString("win",0,0);
                }else {
                    BufferedImage image = ImageIO.read(new File("E:\\my_works\\work_space\\JAVA\\SAST\\mine_sweeper\\src\\main\\resources\\mine.png"));
                    game.mines().forEach(step -> {
                        g.drawImage(image,uw * step.x(),uh * step.y(),uw,uh,null );
                    });
                }
            }
        }
        for (int i = 0; i <= this.getWidth(); i += uw) {
            g.drawLine(i, 0, i, this.getHeight());
        }
        for (int i = 0; i <= this.getHeight(); i += uh) {
            g.drawLine(0, i, this.getWidth(), i);
        }

    }

    @SneakyThrows
    private BufferedImage getImageN(int n){
        return images.get(n);
    }

    @SneakyThrows
    private static BufferedImage getImage(int n){
        return ImageIO.read(new File("E:\\my_works\\work_space\\JAVA\\SAST\\mine_sweeper\\src\\main\\resources\\number-"+ n +".png"));
    }

    private static final Map<Integer, BufferedImage> images = Map.of(
            0,getImage(0),1,getImage(1),2,getImage(2),3,getImage(3),4,getImage(4),5,getImage(5),6,getImage(6),7,getImage(7),8,getImage(8)
    );

    @SneakyThrows
    private BufferedImage getFlagImage(){
        return ImageIO.read(new File("E:\\my_works\\work_space\\JAVA\\SAST\\mine_sweeper\\src\\main\\resources\\flag.png"));
    }

}