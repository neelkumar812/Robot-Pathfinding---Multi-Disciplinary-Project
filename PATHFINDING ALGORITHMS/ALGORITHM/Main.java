/**
 * @author Neel Kumar
 * @email [neelkumar812@gmail.com]
 * @create date 2022-09-26 17:14:45
 * @modify date 2022-09-26 17:14:45
 * @desc [description]
 */

package Main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import Algorithm.*;


public class Main {

    //final formatted string 
    private static ArrayList<String> formatted = new ArrayList<String>();
    public static ArrayList<Integer> targetList = new ArrayList<Integer>();
    public static List<Cell> obstacleList = new ArrayList<Cell>();

    static int ini_pos = Main.hash(1, 1, 0);

    public static void main(String[] args) {

        //Over here, instead of the sample harcoded inputs, need to pass in the inputs dynamically from what Dion will send us.
        Main.create_obstacles("(3,5,3,1) (8,5,0,2) (4,13,2,3) (1,18,1,4) (15,16,1,5) (11,7,1,6) (13,2,0,7) (19,9,2,8)");
        
		
		Path hamiltonian = new Path(targetList, obstacleList);
		
		String unformatted = hamiltonian.plan();
		//System.out.println(unformatted);

        //place where the output is stored 
        ByteArrayOutputStream output_stored = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(output_stored);
        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);

        //methods that output robot movement string as well as order of obstacles visited.
        print_formatted(unformatted); 
        print_obstacle_order(hamiltonian.getOrder());  

        
        // Print some output: goes to your special stream
        //System.out.println("Foofoofoo!");
        // Put things back
        System.out.flush();
        System.setOut(old);
        // The string output is saved to output_stored.toString()
        System.out.println(output_stored.toString());
	}

    public static void create_obstacles(String input){

        targetList.add(ini_pos);
        int target_dir;
        int hashed;

        String sample = input;
        String[] array = sample.split(" ");
        for (String a : array){
            a = a.substring(a.indexOf("(")+1, a.indexOf(")"));
            String[] arr1 = a.split(",");
          //for (String b : arr1);
            // System.out.println(b);
            String l = arr1[0];
            int x = Integer.parseInt(l);
            // System.out.println("x:");
            // System.out.println(x);
            String m = arr1[1];
            int y = Integer.parseInt(m);
            // System.out.println("y:");
            // System.out.println(y);
            String n = arr1[2];
            int dir = Integer.parseInt(n);
            // System.out.println("dir:");
            // System.out.println(dir);
            String o = arr1[3];
            int id = Integer.parseInt(o);
            obstacleList.add(new Cell(y,x));

            switch(dir) {
                case 0:
                    target_dir = 2;
                    //System.out.println("X and Y In hashed:");
                    // System.out.println(x);
                    // System.out.println(y);
                    hashed = hash(x, y+3, target_dir);
                    // System.out.println("hashed:");
                    // System.out.println(hashed);
                    targetList.add(hashed);
                    break;
                case 1:
                    target_dir = 0;
                    //System.out.println("X and Y In hashed:");
                    // System.out.println(x);
                    // System.out.println(y);
                    hashed = hash(x+3,y, target_dir);
                    // System.out.println("hashed:");
                    // System.out.println(hashed);
                    targetList.add(hashed);
                    break;
                case 2:
                    target_dir = 3;
                    // System.out.println("X and Y In hashed:");
                    // System.out.println(x);
                    // System.out.println(y);
                    hashed = hash(x-3, y, target_dir);
                    // System.out.println("hashed:");
                    // System.out.println(hashed);
                    targetList.add(hashed);
                    break;
                case 3:
                    target_dir = 1;
                    // System.out.println("X and Y In hashed:");
                    // System.out.println(x);
                    // System.out.println(y);
                    hashed = hash(x, y-3, target_dir);
                    // System.out.println("hashed:");
                    // System.out.println(hashed);
                    targetList.add(hashed);
                    break;
                default:
                    target_dir = 0;
              
          }  
        }

    }

    public static int hash(int x, int y, int dir) {

        if (x > -1 && x < 20 && y > -1 && y < 20) return (400 * dir + 20 * x + y);
        else return -1;
    }

    public static void print_formatted(String unformatted){

        char cur;
        char next;
        Integer count = 1;
        Integer max;
        String num;

        for(int i = 1; i<unformatted.length(); i++){
            cur = unformatted.charAt(i-1);
            next = unformatted.charAt(i);
            if(cur==next){
                count++;    
            }
            else if(cur!=next){
                switch(cur){
                    case 'w':
                        //code
                        max = count;
                        num = Integer.toString(max*10);
                        if(max*10 < 100) {formatted.add("w0" + num + ",");} 
                        else if (max*10 >=100) {formatted.add("w" + num + ",");}  
                        count = 1;
                        break;
                    case 's':
                        //code
                        max = count;
                        num = Integer.toString(max*10);
                        if(max*10 < 100) {formatted.add("s0" + num + ",");} 
                        else if (max*10 >=100) {formatted.add("s" + num + ",");}  
                        count = 1;
                        break;
                    case 'e':
                        //code
                        for(int x = 0; x<count;x++){
                            formatted.add("e090,");
                        }
                        count = 1;
                        break;
                    case 'q':
                        //code
                        for(int x = 0; x<count;x++){
                            formatted.add("q090,");
                        }
                        count = 1;
                        break;
                    case 'd':
                        //code
                        for(int x = 0; x<count;x++){
                            formatted.add("d090,");
                        }
                        count = 1;
                        break;
                    case 'a':
                        //code
                        for(int x=0;x<count;x++){
                            formatted.add("a090,");
                        }
                        count = 1;
                        break;
                    case 'c':
                        //code
                        formatted.add("c001,");
                        count = 1;
                        break;
                }
           }
        }
        formatted.add("c001,");
        //print out the arraylist
        // System.out.print("\n----------------------------------------------------\n");
        // System.out.println("Output string command for the robot movement path:");
        for (int i = 0; i<formatted.size(); i++) 
	      { 		      
	          System.out.print(formatted.get(i)); 		
	      }
        //System.out.print("\n----------------------------------------------------\n");
    }


    
    public static void print_obstacle_order(int[] x){
        //System.out.print("Order of obstacles visited based on obstacle number as follows: \n");        
        for(int i: x){
            System.out.print(i);
        }
        //System.out.print("-----------------------------------------------------");
    }
}


