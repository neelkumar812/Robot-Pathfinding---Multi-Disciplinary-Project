/**
 * @author Neel Kumar
 * @email [neelkumar812@gmail.com]
 * @create date 2022-09-26 18:08:18
 * @modify date 2022-09-26 18:08:18
 * @desc [description]
 */

package Algorithm;

import java.util.ArrayList;

public class Node {
	int index;
	int x,y,dir;	
	boolean explored;
	String path;
	Integer dist;
	
	ArrayList<Integer> adjacency = new ArrayList<Integer>();
	/*index	- function
	 * 0	- forward (w)
	 * 1	- backward (s)
	 * 2	- forward  right (e)
	 * 3	- forward  left (q)
	 * 4	- backward right (d)
	 * 5	- backward left (a)
	 **/
	
	boolean obstacle;
	
	public static int hash(int x, int y, int dir) {
		if(x > -1 && x < 20 && y > -1 && y < 20) return(400*dir + 20*x + y);
		else return -1;
	}
	
	public Node (int x, int y, int dir){
		this.index = hash(x,y,dir);
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.explored = false;
		this.path = "";
		this.dist = Integer.MAX_VALUE;
		this.obstacle = false;
		
		//Instruction w
		switch(dir) {
		case 0: //N
			if(y+1 < 20) {adjacency.add(hash(x, y+1, dir));}
			else {adjacency.add(-1);}
			break;
		case 1: //E
			if(x+1 < 20) {adjacency.add(hash(x+1, y, dir));}
			else {adjacency.add(-1);}
			break;
		case 2: //W
			if(x-1 > -1) {adjacency.add(hash(x-1, y, dir));}
			else {adjacency.add(-1);}
			break;
		case 3: //S
			if(y-1 > -1) {adjacency.add(hash(x, y-1, dir));}
			else {adjacency.add(-1);}
			break;
		}
		
		//Instruction s
		switch(dir) {
		case 0: //N
			if(y-1 > -1) {adjacency.add(hash(x, y-1, dir));}
			else {adjacency.add(-1);}
			break;
		case 1: //E
			if(x-1 > -1) {adjacency.add(hash(x-1, y, dir));}
			else {adjacency.add(-1);}
			break;
		case 2: //W
			if(x+1 < 20) {adjacency.add(hash(x+1, y, dir));}
			else {adjacency.add(-1);}
			break;
		case 3: //S
			if(y+1 < 20) {adjacency.add(hash(x, y+1, dir));}
			else {adjacency.add(-1);}
			break;
		}
		
		//Instruction e
		switch(dir) {
		case 0: //N
			if(y+2 < 20 && x+3 < 20) {adjacency.add(hash(x+3, y+2, 1));}
			else {adjacency.add(-1);}
			break;
		case 1: //E
			if(y-3 > -1 && x+2 < 20) {adjacency.add(hash(x+2, y-3, 3));}
			else {adjacency.add(-1);}
			break;
		case 2: //W
			if(y+3 < 20 && x-2 > -1) {adjacency.add(hash(x-2, y+3, 0));}
			else {adjacency.add(-1);}
			break;
		case 3: //S
			if(y-2 > -1 && x-3 > -1) {adjacency.add(hash(x-3, y-2, 2));}
			else {adjacency.add(-1);}
			break;
		}
		//instruction q
		switch(dir) {
		case 0: //N
			if(y+2 < 20 && x-3 > -1) {adjacency.add(hash(x-3, y+2, 2));}
			else {adjacency.add(-1);}
			break;
		case 1: //E
			if(y+3 < 20 && x+2 < 20) {adjacency.add(hash(x+2, y+3, 0));}
			else {adjacency.add(-1);}
			break;
		case 2: //W
			if(y-3 > -1 && x-2 > -1) {adjacency.add(hash(x-2, y-3, 3));}
			else {adjacency.add(-1);}
			break;
		case 3: //S
			if(y-2 > -1 && x+3 < 20) {adjacency.add(hash(x+3, y-2, 1));}
			else {adjacency.add(-1);}
			break;
		}
		//instruction d
		
		 switch(dir) {
		 case 0: //N
		 	if(y-3 > -1 && x+2 < 20) {adjacency.add(hash(x+2, y-3, 2));}
		 	else {adjacency.add(-1);}
		 	break;
		 case 1: //E
		 	if(y-2 > -1 && x-3 > -1) {adjacency.add(hash(x-3, y-2, 0));}
		 	else {adjacency.add(-1);}
		 	break;
		 case 2: //W
		 	if(y+2 < 20 && x+3 < 20) {adjacency.add(hash(x+3, y+2, 3));}
		 	else {adjacency.add(-1);}
		 	break;
		 case 3: //S
		 	if(y+3 < 20 && x-2 > -1) {adjacency.add(hash(x-2, y+3, 1));}
		 	else {adjacency.add(-1);}
		 	break;
		 }
		 //Instruction a
		 switch(dir) {
		 case 0: //N
		 	if(y-3 > -1 && x-2 > -1) {adjacency.add(hash(x-2, y-3, 1));}
		 	else {adjacency.add(-1);}
		 	break;
		 case 1: //E
		 	if(y+2 < 20 && x-3 > -1) {adjacency.add(hash(x-3, y+2, 3));}
		 	else {adjacency.add(-1);}
		 	break;
		 case 2: //W
		 	if(y-2 > -1 && x+3 < 20) {adjacency.add(hash(x+3, y-2, 0));}
		 	else {adjacency.add(-1);}
		 	break;
		 case 3: //S
		 	if(y+3 < 20 && x+2 < 20) {adjacency.add(hash(x+2, y+3, 2));}
		 	break;
		 }
	}
	
	public void setExplored() {
		this.explored = true;
	}
	
	public void setUnexplored() {
		this.explored = false;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public ArrayList<Integer> getAdjacencies() {
		return this.adjacency;
	}
	
	public boolean isExplored() {
		return this.explored || this.obstacle;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void clearPath() {
		this.path = "";
	}
	
	public void setObstacle() {
		this.obstacle = true;
	}
	
	public boolean isObstacle() {
		return this.obstacle;
	}
	
	public void setDist(int dist) {
		this.dist = dist;
	}
	
	public int getDist() {
		return this.dist;
	}
	
}