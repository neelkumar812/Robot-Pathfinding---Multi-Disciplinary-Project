/**
 * @author Neel Kumar
 * @email [neelkumar812@gmail.com]
 * @create date 2022-09-26 18:08:01
 * @modify date 2022-09-26 18:08:01
 * @desc [description]
 */


package Algorithm;
import java.util.ArrayList;
import java.util.List;

public class Path { 
	
	static int[][][] perms = new int[8][][];
	int numTargets;
	
	ArrayList<Integer> unfindables = new ArrayList<Integer>();
	
	ArrayList<int[]> weights;
	ArrayList<String[]> routes;
	
	Graph g;
	
	ArrayList<Integer> targetList = new ArrayList<Integer>();
	
	int index = 0;
	
	
	
	private static int factorial(int n) {
		if(n == 1) return 1;
		return n*factorial(n-1);
	}
	
	private static int[] sliceInsert(int[] arr, int loc, int num) {
		//Insert num into arr at index loc
		int[] temp = new int[arr.length+1];
		for(int i = 0; i < arr.length+1; i++) {
			if(i < loc) temp[i] = arr[i];
			else if(i == loc) temp[i] = num;
			else temp[i] = arr[i-1];
		}
		return temp;
	}
	
	private static void genPerms(int n) {
		perms[0] = new int[1][1];
		perms[0][0][0] = 1;
		for(int i = 1; i < n; i++) genPermsRecursive(i+1);
	}
	
	private static void genPermsRecursive(int n) {
		int i = 0;
		int[][] temp2d = new int[factorial(n)][n]; 
		for(int[] p: perms[n-2]) {
			for(int j = 0; j < n; j++) {
				int[] temp = sliceInsert(p,j,n);
				temp2d[i] = temp;
				i++;
			}
		}
		perms[n-1] = temp2d;
	}
	
	public Path(ArrayList<Integer> targetList, List<Cell> obstacleList) {
		genPerms(8);
		this.g = new Graph(obstacleList);
		this.targetList = targetList;
		this.numTargets = targetList.size()-1;
		int i = 0;
		//System.out.println("Order in which targets were received by path planning algorithm:");
		for(int t: targetList) {
			//System.out.print(i + ": ");
			unhashPrint(t);
			i++;
		}
		//System.out.println("Number of targets: " + numTargets);
	}
	
	public static int hash(int x, int y, int dir) {
		if(x > -1 && x < 20 && y > -1 && y < 20) return(400*dir + 20*x + y);
		else return -1;
	}
	
	private int pathLength(String path) {
		int length = 0;
		for(char c : path.toCharArray()) {
			switch(c) {
			case 'w': 
			case 's': length++; break;
			case 'e':
			case 'q': length += 4; break;
			case 'd':
			case 'a': length += 5; break;
			}
		}
		
		return length;
	}
	

	private void generateWeights(ArrayList<Integer> targetList) {
		this.weights = new ArrayList<int[]>();
		this.routes = new ArrayList<String[]>();
		for(int i = 0; i < numTargets+1; i++) {
			this.g.Dijkstra(targetList.get(i), -1);
			int[] temp = new int[numTargets+1];
			String[] tempRoutes = new String[numTargets+1];
			for(int j = 1; j < numTargets+1; j++) {
				String path = this.g.getNode(targetList.get(j)).getPath();
				temp[j-1] = pathLength(path);
				tempRoutes[j-1] = path;
				if(i == 0) if (path.equals("")) unfindables.add(j);
			}
			this.weights.add(temp);
			this.routes.add(tempRoutes);
			this.g.clearExplored();
		}
	}
	
	private int evalPaths(ArrayList<Integer> targetList) {
		int min = Integer.MAX_VALUE;
		int ind = -1;
		for(int i = 0; i < factorial(numTargets); i++) {
			
			
			boolean flag = false;
			for(int u = 0; u < unfindables.size(); u++) {
				if(unfindables.get(u) != perms[numTargets-1][i][numTargets-1-u]) {
					flag = true;
				}
			}
			if(flag) continue;
			
			int len = 0;
			int prev = 0;
			for(int j : perms[numTargets-1][i]) {
				len += weights.get(prev)[j-1];
				prev = j;
			}
			for(int j = 0; j < perms[numTargets-1][i].length - unfindables.size(); j++) //{System.out.print(perms[numTargets-1][i][j]);}
			//System.out.println(": " + len);
			if(len < min) {
				min = len;
				ind = i;
			}
		}
		//System.out.println("FINAL");
		//for(int j = 0; j < perms[numTargets-1][ind].length - unfindables.size(); j++) //{System.out.print(perms[numTargets-1][ind][j]);}
		//System.out.println(": " + min);
		return ind;
	}
	
	public String plan() {
		//System.out.println("START");
		generateWeights(this.targetList);
		//System.out.println("WEIGHTS GENERATED");
		int ind = evalPaths(this.targetList);
		//System.out.println("PATHS EVALUATED");
		this.index = ind;
		
		//System.out.print  ("    ");
		for(int a = 1; a < numTargets+1; a++) //System.out.print(a + ",  ");
		//System.out.println();
		
		for(int i = 0; i < numTargets+1; i++) {
			//System.out.print(i);
			//System.out.print(": ");
			for(int j = 0; j < numTargets; j++) {
				if(weights.get(i)[j] < 10){} //{System.out.print(" ");}
				//System.out.print(weights.get(i)[j]);
				//System.out.print(", ");
			}
			//System.out.print('\n');
		}
		
		String path = "";
		int prev = 0;
		int u = 0;
		for(int next : perms[numTargets-1][ind]) {
			if(u == numTargets-unfindables.size()) break;
			
			
			String temp = this.routes.get(prev)[next-1];
			//unhashPrint(this.targetList[next]);
			path = path.concat(temp);
			path = path.concat("c");
			prev = next;
			
			u++;
		}
		//path = path.substring(0, path.length() - unfindables.size());
		//System.out.println("");
		return path;
	}
	
	private void unhashPrint(int num) {
		int dir = num/400;
		int xy = num%400;
		int x = xy/20;
		int y = xy%20;
	}

	public int[] getOrder() {
		return perms[numTargets-1][this.index];
	}
}
