package fun.feellmoose.gui.swing;

import fun.feellmoose.core.Game;
import fun.feellmoose.core.IGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @projectName: 1
 * @author: mio
 * @date: 2024/11/5 10:31
 */
public class MainWindow extends JWindow {
    int width = (int) this.getMaximumSize().getWidth();
    int height = (int) this.getMaximumSize().getHeight();
    JFrame frame;
    JButton[][] buttons;

    private MainWindow(JFrame owner){
        super(owner);
    }
    public static MainWindow start(IGame game){
        var frame = new JFrame("wine_sweeper");
        var window = new MainWindow(frame);
        window.frame = frame;

        window.setSize(window.width,window.height);
        frame.setSize(window.width, window.height);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        frame.setLayout(new GridLayout(game.units().length,game.units()[0].length));


        var control = new JPanel();
        control.setLayout(new FlowLayout());
        frame.add(control);

        window.buttons = new JButton[game.units().length][game.units()[0].length];
        for(int i = 0; i < game.units().length; i ++){
            for (int j = 0; j < game.units()[0].length; j++){
                var button = new JButton();
                int num = game.units()[i][j].getFilteredNum();
                switch (num) {
                    case -1 -> button.setText(" ");
                    case -2 -> button.setText("F");
                    default -> button.setText(String.valueOf(num));
                }
                int finalI = i;
                int finalJ = j;
                button.setAction(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        window.setVisible(true);
                        game.onTyped(finalI, finalJ);
                    }
                });
                button.setSize(window.width/game.units().length, window.height/game.units()[0].length);
                button.setVisible(true);
                control.add(button);
                window.buttons[i][j] = button;
            }
        }

        control.setVisible(true);
        frame.setVisible(true);

        window.setName("wine_sweeper");
        window.setLocationRelativeTo(frame);
        window.setVisible(true);
        return window;
    }

    @Override
    public void paint(Graphics g) {
        this.width = this.getWidth();
        this.height = this.getHeight();
        this.setSize(width,height);
        frame.setSize(width, height);
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[0].length; j++) {
                buttons[i][j].setSize(width/buttons.length, height/buttons[0].length);
            }
        }
        super.paint(g);
    }
}