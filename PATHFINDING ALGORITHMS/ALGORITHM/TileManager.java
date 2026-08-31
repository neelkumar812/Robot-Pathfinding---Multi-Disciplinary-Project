package GUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;

public class TileManager {

    Arena arena;
    public Tile[] tile;
    public int[][] mapTileNum;

    public TileManager(Arena arena) {
        this.arena = arena;
        tile = new Tile[15]; //tile types
        mapTileNum = new int[arena.numCol][arena.numRow];

        getTileImage();

        loadMap();

    }

    public void getTileImage() {
        try {

            tile[0] = new Tile(); //background
            tile[0].image = ImageIO.read(new File("res/white.png"));

            tile[1] = new Tile(); //up obstacle
            tile[1].image = ImageIO.read(new File("res/obstacle 0.png"));
            tile[1].collision = true;

            tile[2] = new Tile(); //right obstacle
            tile[2].image = ImageIO.read(new File("res/obstacle 1.png"));
            tile[2].collision = true;

            tile[3] = new Tile(); //down obstacle
            tile[3].image = ImageIO.read(new File("res/obstacle 3.png"));
            tile[3].collision = true;

            tile[4] = new Tile(); //left obstacle
            tile[4].image = ImageIO.read(new File("res/obstacle 2.png"));
            tile[4].collision = true;

            tile[7] = new Tile(); //deadzones
            tile[7].image = ImageIO.read(new File("res/obstacle zone.png"));
            tile[7].collision = true;

            tile[5] = new Tile(); //start zone
            tile[5].image = ImageIO.read(new File("res/yellow.png"));

            tile[6] = new Tile(); //Borders
            tile[6].image = ImageIO.read(new File("res/border.png"));
            tile[6].collision = true;

            tile[8] = new Tile(); //traversed
            tile[8].image = ImageIO.read(new File("res/grayup.png"));

            tile[9] = new Tile(); //traversed
            tile[9].image = ImageIO.read(new File("res/grayright.png"));

            tile[10] = new Tile(); //traversed
            tile[10].image = ImageIO.read(new File("res/grayleft.png"));

            tile[11] = new Tile(); //traversed
            tile[11].image = ImageIO.read(new File("res/graydown.png"));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap() {
        try {
            InputStream is = new FileInputStream("res/map.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;
            while (col < arena.numCol && row < arena.numRow) {
                String line = br.readLine();

                while (col < arena.numCol) {
                    String[] numbers = line.split("\t");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }
                if (col == arena.numCol) {
                    col = 0;
                    row++;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void draw(Graphics2D g2) {
        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        while (col < arena.numCol && row < arena.numRow) {

            int tileNum = mapTileNum[col][row];

            g2.drawImage(tile[tileNum].image, x, y, arena.tileSize, arena.tileSize, null);
            col++;
            x += arena.tileSize;

            if (col == arena.numCol) {
                col = 0;
                x = 0;
                row++;
                y += arena.tileSize;
            }
        }

    }


}
