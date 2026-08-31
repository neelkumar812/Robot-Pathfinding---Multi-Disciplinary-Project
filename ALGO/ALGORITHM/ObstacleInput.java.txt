package needs_refactoring;
/**
 * @author Neel Kumar
 * @email [neelkumar812@gmail.com]
 * @create date 2022-09-20 21:43:15
 * @modify date 2022-09-20 21:43:15
 * @desc [description]
 */
import java.util.ArrayList;
import java.util.Scanner;

import Algorithm.Cell;




public class ObstacleInput {

    public ObstacleInput(){}

    static Scanner sc = new Scanner(System.in);

    public static ArrayList<Cell> obstaclelist = new ArrayList<>();
    public static ArrayList<Integer> targetList = new ArrayList<Integer>();

    static int ini_pos = ObstacleInput.hash(1, 1, 0);

    public static void create_obstacles(){
        //Initialize Target List with robot initial position
        targetList.add(ini_pos);

        // int flag;
        // int x;
        // int y;
        // int dir;
        int target_dir;
        int hashed;

        // do{
            // System.out.println("Welcome to the object creation menu, coordinates are in x,y cartesian plane. ");
            // System.out.println("Please enter the x coordinate of the obstacle");
            // x = sc.nextInt();
            // System.out.println("Please enter the y coordinate of the obstacle");
            // y = sc.nextInt();
            // System.out.println("Please enter direction of the obstacle");
            // dir = sc.nextInt();

        String sample = "(1,4,0,1) (1,2,1,2) (3,2,0,4) (6,9,2,5)";
        String[] array = sample.split(" ");
        for (String a : array){
            // System.out.println(a);
            a = a.substring(a.indexOf("(")+1, a.indexOf(")"));
            // System.out.println(a);
            String[] arr1 = a.split(",");
          for (String b : arr1){
            System.out.println(b);
            String l = arr1[0];
            int x = Integer.parseInt(l);
            String m = arr1[1];
            int y = Integer.parseInt(m);
            String n = arr1[2];
            int dir = Integer.parseInt(n);
            String o = arr1[3];
            int id = Integer.parseInt(o);
            obstaclelist.add(new Cell(y,x));

            switch(dir) {
                case 0:
                    target_dir = 2;
                    hashed = hash(x, y+3, target_dir);
                    targetList.add(hashed);
                    break;
                case 1:
                    target_dir = 0;
                    hashed = hash(x+3,y, target_dir);
                    targetList.add(hashed);
                    break;
                case 2:
                    target_dir = 3;
                    hashed = hash(x-3, y, target_dir);
                    targetList.add(hashed);
                    break;
                case 3:
                    target_dir = 1;
                    hashed = hash(x, y-3, target_dir);
                    targetList.add(hashed);
                    break;
                default:
                    target_dir = 0;
              }
          }  
        }

        //     System.out.println("Enter 0 to quit, 1 to add more obstacles");
        //     flag = sc.nextInt();
        // }while(flag!= 0);
    }

    public static int hash(int x, int y, int dir) {
        if (x > -1 && x < 20 && y > -1 && y < 20) return (400 * dir + 20 * x + y);
        else return -1;
    }


    

    
}

