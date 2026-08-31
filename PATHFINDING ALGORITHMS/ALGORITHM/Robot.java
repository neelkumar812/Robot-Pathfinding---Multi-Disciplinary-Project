package GUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static GUI.Arena.tileSize;

public class Robot {

    Arena arena;

    public Robot(Arena arena) {
        this.arena = arena;
        setDefaultValues();
        getRobotImage();
    }

    public static int x, y;
    public BufferedImage n;
    public BufferedImage e;
    public BufferedImage s;
    public BufferedImage w;
    public static int direction = 0;
    public static boolean collided = false;

    public static int tile1, tile2, tile3, tile4, tile5, tile6, tile7, tile8, tile9;


    public static void setDefaultValues() {
        x = 64;
        y = 608;
        direction = 0;
    }

    public void getRobotImage() {
        try {
            n = ImageIO.read(new File("res/Robot 0.png"));
            e = ImageIO.read(new File("res/Robot 1.png"));
            s = ImageIO.read(new File("res/Robot 3.png"));
            w = ImageIO.read(new File("res/Robot 2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void draw(Graphics2D g2) {
        BufferedImage image = switch (direction) {
            case 0 -> n;
            case 1 -> e;
            case 3 -> s;
            case 2 -> w;
            default -> null;
        };
        g2.drawImage(image, x, y, tileSize, tileSize, null);
    }

}
