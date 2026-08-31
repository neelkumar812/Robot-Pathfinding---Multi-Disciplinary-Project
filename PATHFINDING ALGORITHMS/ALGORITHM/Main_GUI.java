package GUI;

import Main.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import Algorithm.Path;


public class Main_GUI {


    public static void main(String[] args) {

        JFrame window = new JFrame();
        JLabel label1 = new JLabel("Input obstacles here");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(60, 20));
        JButton algo_button = new JButton("Algo");
        JButton nw_button = new JButton("NW");
        JButton n_button = new JButton("N");
        JButton ne_button = new JButton("NE");
        JButton sw_button = new JButton("SW");
        JButton s_button = new JButton("S");
        JButton se_button = new JButton("SE");
        JButton reset = new JButton("RESET");
        Arena arena = new Arena();

        Timer timer = new Timer();

        ActionListener ab_buttonlistenner = e -> {

        };
        algo_button.addActionListener(ab_buttonlistenner);


        // NORTHWEST BUTTON
        ActionListener nw_buttonlistener = e -> {
            switch (Robot.direction) {
                case 0 -> {    //facing north
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32) - 2];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) - 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32) - 2];
                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");

                    } else {

                        Robot.y -= Arena.tileSize;


                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 1 -> {   // facing east
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) - 1];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) - 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) - 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {


                        Robot.x += Arena.tileSize;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 500);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 0;
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1000);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1500);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 2000);

                    }
                }
                case 3 -> {  // facing south

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32) + 2];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) + 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32) + 2];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {

//                        Robot.direction = 1;
//                        Robot.x += 3 * Arena.tileSize;
//                        Robot.y += 2 * Arena.tileSize;

                        Robot.y += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 1;
                                Robot.x += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 2 -> {    // facing west
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) + 1];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) + 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) + 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {

//                        Robot.direction = 3;
//                        Robot.x -= 2 * Arena.tileSize;
//                        Robot.y += 3 * Arena.tileSize;


                        Robot.x -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 3;
                                Robot.y += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
            }
        };
        nw_button.addActionListener(nw_buttonlistener);


        //NORTH
        ActionListener n_buttonlistener = e -> {
            switch (Robot.direction) {
                case 0 -> {    //facing north


                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32 - 1)];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.y -= Arena.tileSize;

                    }

                }

                case 1 -> {   // facing east

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32)];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.x += Arena.tileSize;
                    }
                }

                case 3 -> {  // facing south
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 1];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.y += Arena.tileSize;
                    }
                }

                case 2 -> {    // facing west

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32)];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.x -= Arena.tileSize;
                    }
                }
            }
        };
        n_button.addActionListener(n_buttonlistener);


        // NORTHEAST
        ActionListener ne_buttonlistener = e -> {

            switch (Robot.direction) {
                case 0 -> {    //facing north

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32) - 2];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) - 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32) - 2];
                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {

                        Robot.y -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 1;
                                Robot.x += Arena.tileSize;
                            }
                        }, 1000);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 1500);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 2000);
                    }
                }
                case 1 -> {   // facing east

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) + 1];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) + 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) + 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x += 2 * Arena.tileSize;
//                        Robot.y += 3 * (Arena.tileSize);
//                        Robot.direction = 3;


                        Robot.x += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 3;
                                Robot.y += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 3 -> {  // facing south

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32 + 1)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32 + 2)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32 + 2)];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) + 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32) + 2];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x -= 3 * Arena.tileSize;
//                        Robot.y += 2 * Arena.tileSize;
//                        Robot.direction = 2;


                        Robot.y += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 2;
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 2 -> {    // facing west

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) - 1];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) - 2];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) - 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x -= 2 * Arena.tileSize;
//                        Robot.y -= 3 * Arena.tileSize;
//                        Robot.direction = 0;


                        Robot.x -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 0;
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
            }
        };
        ne_button.addActionListener(ne_buttonlistener);


        // SOUTHWEST
        ActionListener sw_buttonlistener = e -> {
            switch (Robot.direction) {
                case 0 -> {    //facing north

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 3];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32) + 3];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) + 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x -= 2 * Arena.tileSize;
//                        Robot.y += 3 * Arena.tileSize;
//                        Robot.direction = 1;

                        Robot.y += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 1;
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 1 -> {   // facing east

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32)];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32) - 1];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32) - 2];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x -= 3 * Arena.tileSize;
//                        Robot.y -= 2 * Arena.tileSize;
//                        Robot.direction = 3;


                        Robot.x -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 3;
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 3 -> {  // facing south
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 3];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32) - 3];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) - 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x += 2 * Arena.tileSize;
//                        Robot.y -= 3 * Arena.tileSize;
//                        Robot.direction = 2;


                        Robot.y -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 2;
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 2 -> {    // facing west

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32)];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32) + 1];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32) + 2];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
//                        Robot.x += 3 * Arena.tileSize;
//                        Robot.y += 2 * Arena.tileSize;
//                        Robot.direction = 0;


                        Robot.x += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 0;
                                Robot.y += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
            }
        };
        sw_button.addActionListener(sw_buttonlistener);

        //SOUTH
        ActionListener s_buttonlistener = e -> {
            switch (Robot.direction) {
                case 0 -> {    //facing north

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 1];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.y += Arena.tileSize;
                    }
                }
                case 1 -> {   // facing east

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32)];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.x -= Arena.tileSize;
                    }
                }
                case 3 -> {  // facing south

                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 1];

                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {
                        Robot.y -= Arena.tileSize;
                    }
                }
                case 2 -> {    // facing west
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32)];
                    if (arena.tileManager.tile[Robot.tile1].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {

                        Robot.x += Arena.tileSize;
                    }

                }
            }
        };
        s_button.addActionListener(s_buttonlistener);

        //SOUTHEAST
        ActionListener se_buttonlistener = e -> {
            switch (Robot.direction) {
                case 0 -> {    //facing north
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) + 3];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32) + 3];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32) + 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {

//                        Robot.x += 2 * Arena.tileSize;
//                        Robot.y += 3 * Arena.tileSize;
//                        Robot.direction = 2;


                        Robot.y += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 2;
                                Robot.x += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 1 -> {   // facing east
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32)];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32) + 1];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 3][(Robot.y / 32) + 2];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;

                        System.out.println("Robot will collide!");
                    } else {

//                        Robot.x -= 3 * Arena.tileSize;
//                        Robot.y += 2 * Arena.tileSize;
//                        Robot.direction = 0;


                        Robot.x -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 0;
                                Robot.y += Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y += Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 3 -> {  // facing south
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 1];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 2];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32)][(Robot.y / 32) - 3];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) - 1][(Robot.y / 32) - 3];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) - 2][(Robot.y / 32) - 3];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;

                        System.out.println("Robot will collide!");
                    } else {


                        Robot.y -= Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 1;
                                Robot.x -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
                case 2 -> {    // facing west
                    Robot.tile1 = arena.tileManager.mapTileNum[(Robot.x / 32) + 1][(Robot.y / 32)];
                    Robot.tile2 = arena.tileManager.mapTileNum[(Robot.x / 32) + 2][(Robot.y / 32)];
                    Robot.tile3 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32)];
                    Robot.tile4 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32) - 1];
                    Robot.tile5 = arena.tileManager.mapTileNum[(Robot.x / 32) + 3][(Robot.y / 32) - 2];

                    if (arena.tileManager.tile[Robot.tile1].collision || arena.tileManager.tile[Robot.tile2].collision || arena.tileManager.tile[Robot.tile3].collision || arena.tileManager.tile[Robot.tile4].collision || arena.tileManager.tile[Robot.tile5].collision) {
                        Robot.collided = true;
                        System.out.println("Robot will collide!");
                    } else {

//                        Robot.x += 3 * Arena.tileSize;
//                        Robot.y -= 2 * Arena.tileSize;
//                        Robot.direction = 3;


                        Robot.x += Arena.tileSize;

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.x += Arena.tileSize;
                            }
                        }, 1000);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.direction = 3;
                                Robot.y -= Arena.tileSize;
                            }
                        }, 1500);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Robot.y -= Arena.tileSize;
                            }
                        }, 2000);


                    }
                }
            }
        };
        se_button.addActionListener(se_buttonlistener);

        ActionListener reset_buttonlistener = e -> {
            System.out.println("------------------------RESET------------------------");
            Robot.setDefaultValues();
            Robot.direction = 0;
        };
        reset.addActionListener(reset_buttonlistener);

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setTitle("GUI");
        window.getContentPane().setLayout(new GridBagLayout());
        window.add(arena);
        window.add(nw_button);
        window.add(n_button);
        window.add(ne_button);
        window.add(sw_button);
        window.add(s_button);
        window.add(se_button);
        window.add(reset);
        window.add(label1);
        window.add(tf1);
        window.add(algo_button);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        arena.startGameThread();
    }

}