/**
 * @author Neel Kumar
 * @email [neelkumar812@gmail.com]
 * @create date 2022-09-26 18:08:30
 * @modify date 2022-09-26 18:08:30
 * @desc [description]
 */

package Algorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class Graph {
	ArrayList<Node> adjacencyList = new ArrayList<Node>();
	
	private static String[] dict = {"w", "s", "e", "q", "d", "a"};
	
	/*index	- function
	 * 0	- forward (w)
	 * 1	- backward (s)
	 * 2	- forward right (e)
	 * 3	- forward left (q)
	 * 4	- backward right (d)
	 * 5	- backward right (a)
	 * */
	
	public static int hash(int x, int y, int dir) {
		if(x > -1 && x < 20 && y > -1 && y < 20) return(400*dir + 20*x + y);
		else return -1;
	}
	
	public static int[] unhash(int num) {
		int dir = num/400;
		int xy = num%400;
		int x = xy/20;
		int y = xy%20;
		int[] out = {x, y, dir};
		return out;
	}
	
	private Node minDist(ArrayList<Node> q) {
		Node min = null;
		int minVal = Integer.MAX_VALUE;
		for(int i = 1; i < q.size(); i++) {
			if(q.get(i).getDist() < minVal && !q.get(i).isExplored()) {
				min = q.get(i);
				minVal = q.get(i).getDist();
			}
		}
		return min;
	}
	
	public Node getNode(int hash) {
		return this.adjacencyList.get(hash);
	}
	
	public Graph(List<Cell> obstacleList) {
		for(int dir = 0; dir < 4; dir++) {
			for(int x = 0; x<20; x++) {
				for(int y = 0; y<20; y++) {
					Node temp = new Node(x, y, dir);
					adjacencyList.add(hash(x, y, dir), temp);
				}
			}
		}
		//set a 3x3 grid around the obstacle as obstacle
		for(Cell c : obstacleList) {
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					for(int k = 0; k < 4; k++) {
						try {adjacencyList.get(hash(c.getX()+i,c.getY()+j,k)).setObstacle();}
						catch(IndexOutOfBoundsException e) {}
						//System.out.println("adding obstacle at x=" + (c.getX()+i) + ", y=" + (c.getY()+j) + ", dir=" + dict[k]);
					}	
				}
			}
		}
	}


	private void setStartZone(){
		adjacencyList.get(hash(0,0,0)).setObstacle();
		adjacencyList.get(hash(0,0,1)).setObstacle();
		adjacencyList.get(hash(0,0,2)).setObstacle();
		adjacencyList.get(hash(0,0,3)).setObstacle();

		adjacencyList.get(hash(0,1,0)).setObstacle();
		adjacencyList.get(hash(0,1,1)).setObstacle();
		adjacencyList.get(hash(0,1,2)).setObstacle();
		adjacencyList.get(hash(0,1,3)).setObstacle();

		adjacencyList.get(hash(1,0,0)).setObstacle();
		adjacencyList.get(hash(1,0,1)).setObstacle();
		adjacencyList.get(hash(1,0,2)).setObstacle();
		adjacencyList.get(hash(1,0,3)).setObstacle();
		

	}
	
	private boolean isValidMove(int curNode, int edge, int mov) {
		if(edge == -1) {return false;}
		setStartZone();
		if(adjacencyList.get(edge).isObstacle()) {return false;}
		if(mov > 1) {
			int[] unhashed = unhash(curNode);
			int x = unhashed[0];
			int y = unhashed[1];
			int dir = unhashed[2];
			switch(mov*4 + dir) {
			case 8:  //Forward Right, North
                if(x+2 < 20 && y+3 < 20){if(adjacencyList.get(hash(x,y+1,0)).isObstacle() || adjacencyList.get(hash(x+1,y+2,0)).isObstacle() || adjacencyList.get(hash(x+2,y+3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 9:  //Forward Right, East
                if(x+3 < 20 && y-2 > -1){if(adjacencyList.get(hash(x+1,y,0)).isObstacle() || adjacencyList.get(hash(x+2,y-1,0)).isObstacle() || adjacencyList.get(hash(x+3,y-2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 10:  //Forward Right, West
                if(x-3 > -1 && y+2 < 20){if(adjacencyList.get(hash(x-1,y,0)).isObstacle() || adjacencyList.get(hash(x-2,y+1,0)).isObstacle() || adjacencyList.get(hash(x-3,y+2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 11:  //Forward Right, South
                if(x-2 > -1 && y-3 > -1){if(adjacencyList.get(hash(x,y-1,0)).isObstacle() || adjacencyList.get(hash(x-1,y-2,0)).isObstacle() || adjacencyList.get(hash(x-2,y-3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 12: //Forward Left, North
                if(x-2 > -1 && y+3 < 20){if(adjacencyList.get(hash(x,y+1,0)).isObstacle() || adjacencyList.get(hash(x-1,y+2,0)).isObstacle() || adjacencyList.get(hash(x-2,y+3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 13: //Forward Left, East
                if(x+3 < 20 && y+2 < 20){if(adjacencyList.get(hash(x+1,y,0)).isObstacle() || adjacencyList.get(hash(x+2,y+1,0)).isObstacle() || adjacencyList.get(hash(x+3,y+2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 14: //Forward Left, West
                if(x-3 > -1 && y+2 < 20){if(adjacencyList.get(hash(x-1,y,0)).isObstacle() || adjacencyList.get(hash(x-2,y+1,0)).isObstacle() || adjacencyList.get(hash(x-3,y+2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 15: //Forward Left, South
                if(x+2 < 20 && y-3 > -1){if(adjacencyList.get(hash(x,y-1,0)).isObstacle() || adjacencyList.get(hash(x+1,y-2,0)).isObstacle() || adjacencyList.get(hash(x+2,y-3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 16: //Backward Right, North
                if(x+2 < 20 && y-3 > -1){if(adjacencyList.get(hash(x,y-1,0)).isObstacle() || adjacencyList.get(hash(x+1,y-2,0)).isObstacle() || adjacencyList.get(hash(x+2,y-3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 17: //Backward Right, East
                if(x-3 > -1 && y-2 > -1){if(adjacencyList.get(hash(x-1,y,0)).isObstacle() || adjacencyList.get(hash(x-2,y-1,0)).isObstacle() || adjacencyList.get(hash(x-3,y-2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 18: //Backward Right, West
                if(x+3 < 20 && y+2 < 20){if(adjacencyList.get(hash(x+1,y,0)).isObstacle() || adjacencyList.get(hash(x+2,y+1,0)).isObstacle() || adjacencyList.get(hash(x+3,y+2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 19: //Backward Right, South
                if(x-2 > -1 && y+3 < 20){if(adjacencyList.get(hash(x,y+1,0)).isObstacle() || adjacencyList.get(hash(x-1,y+2,0)).isObstacle() || adjacencyList.get(hash(x-2,y+3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 20: //Backward Left, North
                if(x-2 > -1 && y-3 > -1){if(adjacencyList.get(hash(x,y-1,0)).isObstacle() || adjacencyList.get(hash(x-1,y-2,0)).isObstacle() || adjacencyList.get(hash(x-2,y-3,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 21: //Backward Left, East
                if(x-3 > -1 && y+2 < 20){if(adjacencyList.get(hash(x-1,y,0)).isObstacle() || adjacencyList.get(hash(x-2,y+1,0)).isObstacle() || adjacencyList.get(hash(x-3,y+2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 22: //Backward Left, West
                if(x+3 < 20 && y-2 > -1){if(adjacencyList.get(hash(x+1,y,0)).isObstacle() || adjacencyList.get(hash(x+2,y-1,0)).isObstacle() || adjacencyList.get(hash(x+3,y-2,0)).isObstacle()) {return false;}}
                else return false;
                break;
            case 23: //Backward Left, South
                if(x+2 < 20 && y+3 < 20){if(adjacencyList.get(hash(x,y+1,0)).isObstacle() || adjacencyList.get(hash(x+1,y+2,0)).isObstacle() || adjacencyList.get(hash(x+2,y+3,0)).isObstacle()) {return false;}}
                else return false;
                break;
			}
		}
		
		return true;
	}
	
	public String BFS(int start, int end) {
		Queue<Integer> q = new LinkedList<>(); //Initialize the queue
		this.adjacencyList.get(start).setExplored(); //explored the starting Node
		q.add(start); //Enqueue starting Node
		while(!q.isEmpty()) { //While queue is not empty
			int v = q.remove(); //Dequeue element
			if(v == end) {break;} //Break if goal found
			int i = 0;
			for(int edge : adjacencyList.get(v).getAdjacencies()) { //For the edges of our Node
				if(!isValidMove(v, edge, i)) {i++; continue;}
				
				if(!adjacencyList.get(edge).isExplored()) { //if the Node is not explored and not an obstacle
					
					adjacencyList.get(edge).setExplored(); //explore the Node
					String pathToNode = adjacencyList.get(v).getPath().concat(dict[i]); //set shortest path to the Node
					adjacencyList.get(edge).setPath(pathToNode); //set it
					q.add(edge); //add the Node to the queue
				}
				i++;
			}
		}
		return adjacencyList.get(end).getPath();
	}
	
	public String Dijkstra(int start, int end) {
		adjacencyList.get(start).setDist(0);
		while(true) {
			
			Node u = minDist(this.adjacencyList); //get closest unexplored
			
			if(u == null) {break;}
			//System.out.println(u.index);
			
			u.setExplored(); //set it as explored
			
		    if(u.index == end) {break;} //if we find end, break
			int i = 0;
			for(int edge : u.getAdjacencies()) {
				
				if(!isValidMove(u.index, edge, i)) {i++; continue;}
				int newDist = u.getDist();
				if(i < 1) {newDist += 2;}		//Forward
				else if(i < 2) {newDist += 3;}	//Backward
				else if(i < 4){newDist += 14;}	//Forward Turns
				else if(i < 6){newDist += 19;}  //Backward Turns
				else {newDist += 100;}			//OTS Turns
				
				if(newDist < adjacencyList.get(edge).getDist()) {
					
					
					adjacencyList.get(edge).setDist(newDist);
					String pathToNode = u.getPath().concat(dict[i]); //set shortest path to the Node
					adjacencyList.get(edge).setPath(pathToNode); //set it
				}
				i++;
			}
		}
		if(end != -1) return adjacencyList.get(end).getPath();
		else return "";
	}
	
	public void clearExplored() {
		for(Node n : adjacencyList) {
			n.setUnexplored();
			n.clearPath();
			n.setDist(Integer.MAX_VALUE);
		}
	}

}
