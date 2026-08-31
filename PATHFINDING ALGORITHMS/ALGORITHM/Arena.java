package GUI;

import javax.swing.*;
import java.awt.*;

public class Arena extends JPanel implements Runnable {

    public static final int tileSize = 32; //tile size
    public final int numCol = 22;
    public final int numRow = 22;
    public final int arenaWidth = tileSize * numCol;
    public final int arenaHeight = tileSize * numRow;

    Thread gameThread;
    Robot robot = new Robot(this);

    public TileManager tileManager = new TileManager(this);

    public Arena() {
        this.setPreferredSize(new Dimension(arenaWidth, arenaHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void run() {
        while (gameThread != null) {
//            System.out.println("Thread loop is running!");
            update();
            repaint();
        }
    }


    public void update() {

    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        tileManager.draw(g2);
        robot.draw(g2);
        g2.dispose();
    }


}
