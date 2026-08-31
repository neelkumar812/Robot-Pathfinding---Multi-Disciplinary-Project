package Algorithm;

public class Cell implements Comparable<Cell>{
    //private Cell[][] grid;
    private int y, x;               
    private String startDir;        
    private Cell parent; 
    private String headDir;     
    private boolean isObstacle;     
    private String obstacleDir;     
    private String robotFacingObstacleDir;
    private int obstacleId;
    private int imageId;
    private boolean isTargetCell;   
    private boolean isSolnPath;     
    private boolean isVisited;      
    private double heuristicCost;
    private double finalCost;
    
    public Cell(int y, int x) {
        this.y = y;
        this.x = x;

        this.isObstacle = false;
        this.obstacleDir = "0";
        this.obstacleId = 0;
        this.imageId = 100;
        this.robotFacingObstacleDir = "0";

        this.isSolnPath = false;
        this.isVisited = false;
    }

    // setters and getters
    public void setY(int y) {
        this.y = y;
    }
    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }
    public int getX() {
        return x;
    }
    public void setStartDir(String startDir) {
        this.startDir = startDir;
    }
    public String getStartDir() {
        return startDir;
    }
    public void setParent(Cell parent) {
        this.parent = parent;
    }
    public Cell getParent() {
        return parent;
    }
    public void setHeadDir(String headDir) {
        this.headDir = headDir;
    }
    public String getHeadDir() {
        return headDir;
    }
    public void setIsObstacle(boolean isObstacle) {
        this.isObstacle = isObstacle;
    }
    public boolean getIsObstacle() {
        return isObstacle;
    }
    public void setIsNotObstacle() {
        this.obstacleDir = "X";
        this.isObstacle = false;
    }
    public void setObstacleId(int obstacleId) {
        this.obstacleId = obstacleId;
    }
    public int getObstacleId() {
        return obstacleId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public int getImageId() {
        return imageId;
    }
    public void setObstacleDir(String obstacleDir) {
        this.obstacleDir = obstacleDir;
    }
    public String getObstacleDir() {
        return obstacleDir;
    }
    public void setRobotFacingObstacleDir(String robotFacingObstacleDir) {
        this.robotFacingObstacleDir = robotFacingObstacleDir;
    }
    public String getRobotFacingObstacleDir() {
        return robotFacingObstacleDir;
    }
    public void setIsTargetCell(boolean isTargetCell) {
        this.isTargetCell = isTargetCell;
    }
    public boolean getIsTargetCell() {
        return isTargetCell;
    }

    public void setIsSolnPath(boolean isSolnPath) {
        this.isSolnPath = isSolnPath;
    }
    public boolean getIsSolnPath() {
        return isSolnPath;
    }

    public void setIsVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }
    public boolean getIsVisited() {
        return isVisited;
    }
    public void setHeuristicCost(double heuristicCost) {
        this.heuristicCost = heuristicCost;
    }
    public double getHeuristicCost() {
        return heuristicCost;
    }
    public void setFinalCost(double finalCost) {
        this.finalCost = finalCost;
    } 
    public double getFinalCost() {
        return finalCost;
    }

    public void resetCell() {
        this.isObstacle = false;
        this.obstacleDir = "0";

        this.isSolnPath = false;
        this.isVisited = false;
        
        this.heuristicCost = 0;
        this.finalCost = 0;
    }

    
    public Cell setGridTargetCell(int obstacleY, int obstacleX, String obstacleDir, Cell[][] grid) {
        Cell obstacleCell;
        Cell targetCell;
        String oldObstacleDir = "";

        if (!obstacleDir.equals("0")) { 
            obstacleCell = new Cell(obstacleY, obstacleX);
            obstacleCell.setIsObstacle(true);
            obstacleCell.setObstacleDir(obstacleDir);
            grid[obstacleY][obstacleX].setIsObstacle(true);
            grid[obstacleY][obstacleX].setObstacleDir(obstacleDir);

            switch(obstacleDir) {
                case "N":
                    targetCell = new Cell(obstacleY + 3, obstacleX);
                    targetCell.setIsTargetCell(true);
                    targetCell.setRobotFacingObstacleDir("S");
                    grid[obstacleY + 3][obstacleX].setIsTargetCell(true);
                    grid[obstacleY + 3][obstacleX].setRobotFacingObstacleDir("S");
                    return targetCell;
                case "S":
                    targetCell = new Cell(obstacleY - 3, obstacleX);
                    targetCell.setIsTargetCell(true);
                    targetCell.setRobotFacingObstacleDir("N");
                    grid[obstacleY - 3][obstacleX].setIsTargetCell(true);
                    grid[obstacleY - 3][obstacleX].setRobotFacingObstacleDir("N");
                    return targetCell;
                case "E":
                    targetCell = new Cell(obstacleY, obstacleX + 3);
                    targetCell.setIsTargetCell(true);
                    targetCell.setRobotFacingObstacleDir("W");
                    grid[obstacleY][obstacleX + 3].setIsTargetCell(true);
                    grid[obstacleY][obstacleX + 3].setRobotFacingObstacleDir("W");
                    return targetCell;
                case "W":
                    targetCell = new Cell(obstacleY, obstacleX - 3);
                    targetCell.setIsTargetCell(true);
                    targetCell.setRobotFacingObstacleDir("E");
                    grid[obstacleY][obstacleX - 3].setIsTargetCell(true);
                    grid[obstacleY][obstacleX - 3].setRobotFacingObstacleDir("E");
                    return targetCell;
            }
        }
        else { 
            obstacleCell = new Cell(obstacleY, obstacleX);

            oldObstacleDir = grid[obstacleY][obstacleX].getObstacleDir();

            obstacleCell.setIsObstacle(false);
            obstacleCell.setObstacleDir(obstacleDir);
            grid[obstacleY][obstacleX].setIsObstacle(false);
            grid[obstacleY][obstacleX].setObstacleDir(obstacleDir);

            switch(oldObstacleDir) {
                case "N":
                    targetCell = new Cell(obstacleY + 3, obstacleX);
                    targetCell.setIsTargetCell(false);
                    targetCell.setRobotFacingObstacleDir("0");
                    grid[obstacleY + 3][obstacleX].setIsTargetCell(false);
                    grid[obstacleY + 3][obstacleX].setRobotFacingObstacleDir("0");
                    return targetCell;
                case "S":
                    targetCell = new Cell(obstacleY - 3, obstacleX);
                    targetCell.setIsTargetCell(false);
                    targetCell.setRobotFacingObstacleDir("0");
                    grid[obstacleY - 3][obstacleX].setIsTargetCell(false);
                    grid[obstacleY - 3][obstacleX].setRobotFacingObstacleDir("0");
                    return targetCell;
                case "E":
                    targetCell = new Cell(obstacleY, obstacleX + 3);
                    targetCell.setIsTargetCell(false);
                    targetCell.setRobotFacingObstacleDir("0");
                    grid[obstacleY][obstacleX + 3].setIsTargetCell(false);
                    grid[obstacleY][obstacleX + 3].setRobotFacingObstacleDir("0");
                    return targetCell;
                case "W":
                    targetCell = new Cell(obstacleY, obstacleX - 3);
                    targetCell.setIsTargetCell(false);
                    targetCell.setRobotFacingObstacleDir("0");
                    grid[obstacleY][obstacleX - 3].setIsTargetCell(false);
                    grid[obstacleY][obstacleX - 3].setRobotFacingObstacleDir("0");
                    return targetCell;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Cell o) {
        if(this.getHeuristicCost() > o.getHeuristicCost()) {
			return 1;
		}
		else if(this.getHeuristicCost() < o.getHeuristicCost()){
			return -1;
		}
		else {
			return 0;
		}
    }

    @Override
    public String toString() {
        return "{" + this.y + ", " + this.x + ", " + this.obstacleDir + "}";
    }
}