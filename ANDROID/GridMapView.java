package com.example.mdp_android_grp21.ui.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Handler;
import android.view.ViewConfiguration;
import android.graphics.Bitmap.Config;
import android.graphics.Point;


import androidx.annotation.Nullable;

import com.example.mdp_android_grp21.MainActivity;
import com.example.mdp_android_grp21.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class GridMapView extends View {

    public GridMapView(Context c) {
        super(c);
        initMap();
    }

    SharedPreferences sharedPreferences;
    ImageView raceCar = findViewById(R.id.racecar);
    private Paint black = new Paint();
    private Paint obstacle = new Paint();
    private Paint robot = new Paint();
    private Paint end = new Paint();
    private Paint start = new Paint();
    private Paint waypoint = new Paint();
    private Paint unExplored = new Paint();
    private Paint explored = new Paint();


    private static JSONObject receivedJsonObject = new JSONObject();
    private static JSONObject bpMapInfo;
    private static String robotDirection = "None";
    private static int[] startPoint = new int[]{-1, -1};
    private static int[] currentPoint = new int[]{-1, -1};
    private static int[] previousPoint = new int[]{-1, -1};
    private static int[] wayCoordinates = new int[]{-1, -1};
    private static float positionX=0, positionY=0, startX=0,startY=0;// for dragging
    private static ArrayList<int[]> obstacleCoordinates = new ArrayList<>();
    private static ArrayList<String[]> directionOfObstacleCoordinates = new ArrayList<>();
    private static Bitmap obstacleDirectionBitmap;
    private static boolean isAutoUpdateSet = false;
    private static boolean isRobotDrawable = false;
    private static boolean isWayPointSet = false;
    private static boolean isStartCoordinatesSet = false;
    private static boolean isObstacleSet = false;
    public static boolean isObstacleDirectionCoordinatesSet = false;
    private static boolean isCellUnSet = false;
    private static boolean isExploring = false;
    private static boolean isPositionValid = false;
    public static String obstacleDirection = "";
    public static boolean isAddObstacle = false;
    private static boolean obstacleDrag=false;
    private static boolean isChangeDirection=false;

    private static final String TAG = "GridMap";
    private static final int Column = 20;
    private static final int Row = 20;
    private static float sizeOfCell;
    private static Cell[][] cellsDetail;
    private static String[] clickedCell;

    private boolean isMapDrawn = false;
    public static String MDF_Exploration_Details;
    public static String MDF_Obstacle_Details;


    public GridMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initMap();
        black.setStyle(Paint.Style.FILL_AND_STROKE);
        obstacle.setColor(Color.BLACK);
        robot.setColor(getResources().getColor(R.color.brown));
        //end.setColor(Color.RED);
        start.setColor(Color.CYAN);
        waypoint.setColor(Color.YELLOW);
        unExplored.setColor(getResources().getColor(R.color.lt_beige));
        explored.setColor(getResources().getColor(R.color.brown));
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }


    private void initMap() {
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isMapDrawn == false) {
            this.createGridCell();
           // this.setEndCoordinates(19, 19);
            isMapDrawn = true;
        }
        displayCellInGrid(canvas);
        displayHorizontalLines(canvas);
        displayVerticalLines(canvas);
        displayGridNumber(canvas);
        if (getCanDrawRobot())
            displayRobotOnGrid(canvas, currentPoint);
        drawObstacleWithDirection(canvas, directionOfObstacleCoordinates);
        showLog("Exiting onDraw");
    }

    private void displayCellInGrid(Canvas canvas) {
        showLog("Entering displayCellInGrid");

        for (int x = 1; x <= Column; x++)
            for (int y = 0; y < Row; y++)
                if (!cellsDetail[x][y].type.equals("image") && cellsDetail[x][y].getId() == -1) {
                    canvas.drawRect(cellsDetail[x][y].startX, cellsDetail[x][y].startY, cellsDetail[x][y].endX, cellsDetail[x][y].endY, cellsDetail[x][y].paint);
                } else {
                    Paint textPaint = new Paint();
                    textPaint.setTextSize(30);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawRect(cellsDetail[x][y].startX, cellsDetail[x][y].startY, cellsDetail[x][y].endX, cellsDetail[x][y].endY, cellsDetail[x][y].paint);
                    canvas.drawText(String.valueOf(cellsDetail[x][y].getId()), (cellsDetail[x][y].startX + cellsDetail[x][y].endX) / 2, cellsDetail[x][y].endY + (cellsDetail[x][y].startY - cellsDetail[x][y].endY) / 4, textPaint);
                }

        showLog("Exiting displayCellInGrid");
    }

    public void displayNumberInCell(int x, int y, int id) {
        cellsDetail[x + 1][19 - y].setType("image");
        cellsDetail[x + 1][19 - y].setId(id);
        this.invalidate();
    }


    private void displayHorizontalLines(Canvas canvas) {
        for (int y = 0; y <= Row; y++)
            canvas.drawLine(cellsDetail[1][y].startX, cellsDetail[1][y].startY - (sizeOfCell /30), cellsDetail[20][y].endX, cellsDetail[20][y].startY - (sizeOfCell / 30), black);
    }

    private void displayVerticalLines(Canvas canvas) {
        for (int x = 0; x <= Column; x++)
            canvas.drawLine(cellsDetail[x][0].startX - (sizeOfCell / 30) + sizeOfCell, cellsDetail[x][0].startY - (sizeOfCell / 30), cellsDetail[x][0].startX - (sizeOfCell / 30) + sizeOfCell, cellsDetail[x][19].endY + (sizeOfCell / 30), black);
    }

    private void displayGridNumber(Canvas canvas) {
        showLog("Entering displayGridNumber");
        for (int x = 1; x <= Column; x++) {
            if (x > 9)
                canvas.drawText(Integer.toString(x-1), cellsDetail[x][20].startX + (sizeOfCell / 5), cellsDetail[x][20].startY + (sizeOfCell / 3), black);
            else
                canvas.drawText(Integer.toString(x-1), cellsDetail[x][20].startX + (sizeOfCell / 3), cellsDetail[x][20].startY + (sizeOfCell / 3), black);
        }
        for (int y = 0; y < Row; y++) {
            if ((20 - y) > 10)
                canvas.drawText(Integer.toString(19 - y), cellsDetail[0][y].startX + (sizeOfCell / 3), cellsDetail[0][y].startY + (sizeOfCell / 1.5f), black);
            else
                canvas.drawText(Integer.toString(19 - y), cellsDetail[0][y].startX + (sizeOfCell / 1.5f), cellsDetail[0][y].startY + (sizeOfCell / 1.5f), black);
        }
        showLog("Exiting displayGridNumber");
    }

    private void displayRobotOnGrid(Canvas canvas, int[] curCoord) {
        showLog("Entering displayRobotOnGrid");
        int rowCoordinates = this.convertRow(curCoord[1]);
        int y = rowCoordinates;
        while(y<=rowCoordinates+1)
        {
            canvas.drawLine(cellsDetail[curCoord[0] - 1][y].startX, cellsDetail[curCoord[0] - 1][y].startY - (sizeOfCell / 30), cellsDetail[curCoord[0] + 1][y].endX, cellsDetail[curCoord[0] + 1][y].startY - (sizeOfCell / 30), robot);
            y++;
        }

        int x = curCoord[0] - 1;

        while (x < curCoord[0] + 1)
        {
            canvas.drawLine(cellsDetail[x][rowCoordinates - 1].startX - (sizeOfCell / 30) + sizeOfCell, cellsDetail[x][rowCoordinates - 1].startY, cellsDetail[x][rowCoordinates + 1].startX - (sizeOfCell / 30) + sizeOfCell, cellsDetail[x][rowCoordinates + 1].endY, robot);
            x++;
        }

        showLog("Exiting displayRobotOnGrid");
    }


    public String getRobotDirection() {
        return robotDirection;
    }

    public void setChangeDirection(boolean state){
        isChangeDirection = state;
    }


    public JSONObject getJsonObjectReceived() {
        return receivedJsonObject;
    }

    public void setReceivedJsonObject(JSONObject receivedJsonObject) {
        showLog("Entered setReceivedJsonObject");
        GridMapView.receivedJsonObject = receivedJsonObject;
        bpMapInfo = receivedJsonObject;
    }


    private void setValidPosition(boolean status) {
        isPositionValid = status;
    }

    public boolean getValidPosition() {
        return isPositionValid;
    }

    public void setUnSetCellStatus(boolean status) {
        isCellUnSet = status;
    }

    public boolean getUnSetCellStatus() {
        return isCellUnSet;
    }

    public void setSetObstacleStatus(boolean status) {
        isObstacleSet = status;
    }

    public boolean getSetObstacleStatus() {
        return isObstacleSet;
    }

    public void setStartCoordStatus(boolean status) {
        isStartCoordinatesSet = status;
    }

    private boolean getStartCoordStatus() {
        return isStartCoordinatesSet;
    }


    public boolean getCanDrawRobot() {
        return isRobotDrawable;
    }

    private void createGridCell() {
        showLog("Entering cellCreate");
        cellsDetail = new Cell[Column + 1][Row + 1];
        this.calculateDimension();
        sizeOfCell = this.getCellSize();

        for (int x = 0; x <= Column; x++)
            for (int y = 0; y <= Row; y++)
                cellsDetail[x][y] = new Cell(x * sizeOfCell + (sizeOfCell / 30), y * sizeOfCell + (sizeOfCell / 30), (x + 1) * sizeOfCell, (y + 1) * sizeOfCell, unExplored, "unexplored");
        showLog("Exiting createGridCell");
    }

    public void setStartCoordinates(int col, int row) {
        showLog("Entering setStartCoordinates");
        startPoint[0] = col;
        startPoint[1] = row;
        String direction = getRobotDirection();
        if (direction.equals("None")) {
            direction = "up";
        }
        ImageView racecar = MainActivity.getRaceCar();

        if (this.getStartCoordStatus())
            this.setCurrentCoordinates(col, row, direction);
        racecar.setX((col-1) * sizeOfCell);
        racecar.setY((convertRow(row)-1 ) * sizeOfCell);
        showLog("Exiting setStartCoordinates");
    }

    private int[] getStartCoordinates() {
        return startPoint;
    }

    public void setCurrentCoordinates(int col, int row, String direction) {
        showLog("Entering setCurrentCoordinates");
        currentPoint[0] = col;
        currentPoint[1] = row;
        this.setDirectionOfRobot(direction);
        this.updateAxisOfRobot(col, row, direction);

        row = this.convertRow(row);
        MainActivity.updateRaceCar(col,row,direction);
        for (int x = col - 1; x <= col + 1; x++)
            for (int y = row - 1; y <= row + 1; y++)
                cellsDetail[x][y].setType("robot");
        showLog("Exiting setCurrentCoordinates");
    }

    public int[] getCurrentCoordinates() {
        return currentPoint;
    }

    private void calculateDimension() {
        float width = getWidth();
        this.setCellSize(width / (Column +1));
    }

    private static int convertRow(int row) {
        return (20 - row);
    }

    private void setCellSize(float cellSize) {
        GridMapView.sizeOfCell = cellSize;
    }

    public float getCellSize() {
        return sizeOfCell;
    }

    private void setPreviousRobotCoordinates(int oldCol, int oldRow) {
        showLog("Entering setPreviousRobotCoordinates");
        previousPoint[0] = oldCol;
        previousPoint[1] = oldRow;
        oldRow = this.convertRow(oldRow);
        for (int x = oldCol - 1; x <= oldCol + 1; x++)
            for (int y = oldRow - 1; y <= oldRow + 1; y++)
                cellsDetail[x][y].setType("explored");
        showLog("Exiting setPreviousRobotCoordinates");
    }

    private int[] getPreviousPoint() {
        return previousPoint;
    }


    public void setDirectionOfRobot(String direction) {
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        robotDirection = direction;
        editor.putString("direction", direction);
        editor.apply();
        this.invalidate();
    }

    private void updateAxisOfRobot(int col, int row, String direction) {
        TextView xAxisTextView = ((Activity) this.getContext()).findViewById(R.id.xAxisTextView);
        TextView yAxisTextView = ((Activity) this.getContext()).findViewById(R.id.yAxisTextView);
        TextView directionAxisTextView = ((Activity) this.getContext()).findViewById(R.id.directionAxisTextView);

        xAxisTextView.setText(String.valueOf(col - 1));
        yAxisTextView.setText(String.valueOf(row - 1));
        directionAxisTextView.setText(direction);
    }

    private void setCoordinatesOfObstacle(int col, int row) {
        showLog("Entering setCoordinatesOfObstacle");
        int[] coordinatesOfObstacle = new int[]{col, row};
        GridMapView.obstacleCoordinates.add(coordinatesOfObstacle);
        row = this.convertRow(row);
        cellsDetail[col][row].setType("obstacle");
        showLog("Exiting setCoordinatesOfObstacle");
    }

    private ArrayList<int[]> getCoordinatesOfObstacle() {
        return obstacleCoordinates;
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }


    private class Cell {
        float startX, startY, endX, endY;
        Paint paint;
        String type;
        int id = -1;

        private Cell(float startX, float startY, float endX, float endY, Paint paint, String type) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.paint = paint;
            this.type = type;
        }

        public void setType(String type) {
            this.type = type;
            switch (type) {
                case "obstacle":
                    this.paint = obstacle;
                    break;
                case "robot":
                    this.paint = robot;
                    break;
                case "end":
                    this.paint = end;
                    break;
                case "startConnectionService":
                    this.paint = start;
                    break;
                case "waypoint":
                    this.paint = waypoint;
                    break;
                case "unexplored":
                    this.paint = unExplored;
                    break;
                case "explored":
                    this.paint = explored;
                    break;
                case "image":
                    this.paint = obstacle;
                default:
                    break;
            }
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    @Override
    public boolean onDragEvent (DragEvent event){
        switch(event.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:

                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                showLog("dragging..");
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                return true;

            case DragEvent.ACTION_DROP:
                showLog("dropping image");
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis();
                int action = MotionEvent.ACTION_DOWN;
                MotionEvent e  = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, event.getX(), event.getY(),0);
                this.dispatchTouchEvent(e);
//                handler.removeCallbacks(mLongPressed);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                return true;

            default:
                break;
        }
        return false;
    }

    public boolean isPositionInGrid( int x, int y, boolean isStartPoint){
        float left,bottom,right,top;
        if (!isStartPoint) {
            left = sizeOfCell; bottom = 0; right = this.getWidth(); top = this.getHeight()-sizeOfCell;
        }
        else{
            left = 2*sizeOfCell; bottom = sizeOfCell; right = this.getWidth()-sizeOfCell; top = this.getHeight()-2*sizeOfCell;
        }
        showLog(String.format("grid:(%.2f,%.2f)(%d,%d) (left %.2f,bottom %.2f, right %.2f, top%.2f), cursor: (%d,%d)",this.getX(),this.getY(),this.getWidth(),this.getHeight(),left,bottom,right,top, x, y));
        if(left <= x && x <= right){
            if(bottom <= y && y <= top){
                return true;
            }
        }
        return false;
    }

    public boolean validStartPosition(int x, int y){
        ArrayList<int[]> obstacleCoord = this.getCoordinatesOfObstacle();
        for(int i=0;i<obstacleCoord.size();i++){
            int column = obstacleCoord.get(i)[0];
            int row = obstacleCoord.get(i)[1];
            if (((column-1) <= x && (column+1) >=x) && ((row-1) <= y && (row+1) >=y))
                return false;
        }
        return true;
    }

    public void addObstacleViaText(int x,int y){
        //MainActivity.sendMessageToBlueTooth("Y"+Integer.toString(column));
        ArrayList<int[]> obstacleCoord = this.getCoordinatesOfObstacle();
        for (int i = 0; i < obstacleCoord.size(); i++){
            if (obstacleCoord.get(i)[0] == x && obstacleCoord.get(i)[1] == y) {
                showLog("existing obstacle at position");
                return;
            }
        }
        this.setCoordinatesOfObstacle(x, y);
        setObstacleDirectionCoordinate(x, y, obstacleDirection);
        this.isObstacleDirectionCoordinatesSet = false;
        isAddObstacle = true;
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        showLog("Entering gridmapView onTouchEvent");
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                positionX = event.getX() - sizeOfCell/2;
                positionY = event.getY() - sizeOfCell/2;
                break;
            case MotionEvent.ACTION_UP:
                if(obstacleDrag){
                    obstacleDrag=false;
                    //check if cell is occupied
                    if (!isPositionInGrid((int) event.getX(), (int) event.getY(), false)) {
                        this.invalidate();
                        MainActivity.sendMessageToBlueTooth(String.format("OBSTACLEREMOVED:%s",clickedCell[3]));
                        Toast.makeText(getContext(), "Obstacle removed!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    int column = (int) (event.getX()/ sizeOfCell);
                    int row = this.convertRow((int) (event.getY()/ sizeOfCell));
                    ArrayList<int[]> obstacleCoord = this.getCoordinatesOfObstacle();

                    for (int i = 0; i < obstacleCoord.size(); i++)
                        if (obstacleCoord.get(i)[0] == column && obstacleCoord.get(i)[1] == row) {
                            //if occupied reject movement and move cell back
                            Toast.makeText(getContext(), "Obstacle at position!", Toast.LENGTH_SHORT).show();
                            String[] temp = this.getObstacleDirectionCoord().get(i);
                            String[] celldata = new String[]{clickedCell[0], clickedCell[1], clickedCell[2], clickedCell[3], clickedCell[4]};
                            GridMapView.obstacleCoordinates.add(Integer.valueOf(clickedCell[5]), new int[]{Integer.valueOf(celldata[0]), Integer.valueOf(celldata[1])});
                            this.getObstacleDirectionCoord().add(Integer.valueOf(clickedCell[5]), celldata);
                            int x = Integer.valueOf(clickedCell[0]);
                            int y = Integer.valueOf(clickedCell[1]);
                            y = this.convertRow(y);
                            cellsDetail[x][y].paint = obstacle;
                            cellsDetail[x][y].setType("obstacleDirection");
                            this.invalidate();
                            return true;
                        }
                    //add obstacle if available
                    Toast.makeText(getContext(), "Obstacle placed!", Toast.LENGTH_SHORT).show();
                    String[] celldata = new String[]{String.valueOf(column), String.valueOf(row), clickedCell[2], clickedCell[3], clickedCell[4]};
                    GridMapView.obstacleCoordinates.add(Integer.valueOf(clickedCell[5]),new int[]{column, row});
                    this.getObstacleDirectionCoord().add(Integer.valueOf(clickedCell[5]),celldata);
                    row = this.convertRow(row);
                    cellsDetail[column][row].paint = obstacle;
                    cellsDetail[column][row].setType("obstacleDirection"); //obstacle no, col ,row, obstacle dir,obstacle no
                    MainActivity.sendMessageToBlueTooth(String.format("OBSTACLECHANGE:%s (%d,%d,%s,%s)",clickedCell[3],column-1,19- row,clickedCell[2],clickedCell[3]));
                    this.invalidate();
                    return true;

                }
                break;
            case MotionEvent.ACTION_DOWN:
                float startX=event.getX(), startY=event.getY();
                if(!isPositionInGrid((int) event.getX(),(int) event.getY(),false)){
                    showLog("out of grid");
                    Toast.makeText(getContext(), "out of Grid!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                int column = (int) (event.getX()/ sizeOfCell);
                int row = this.convertRow((int) (event.getY() / sizeOfCell));
                ArrayList<int[]> obstacleCoord = this.getCoordinatesOfObstacle();
                for (int i = 0; i < obstacleCoord.size(); i++)
                    if (obstacleCoord.get(i)[0] == column && obstacleCoord.get(i)[1] == row) {
                        showLog("existing obstacle at position");
                        String[] temp = this.getObstacleDirectionCoord().get(i);
                        if(isChangeDirection) {
                            this.getObstacleDirectionCoord().get(i)[2] = obstacleDirection;
                            int cols = Integer.valueOf(this.getObstacleDirectionCoord().get(i)[0]);
                            int rows = Integer.valueOf(this.getObstacleDirectionCoord().get(i)[1]);
                            String targetid = this.directionOfObstacleCoordinates.get(i)[3];
                            MainActivity.sendMessageToBlueTooth(String.format("Updated Obstacle Direction id:%s direction:%d %d,%d",targetid, Integer.valueOf(obstacleDirection),cols-1,19- rows));
                            this.isObstacleDirectionCoordinatesSet = false;
                            this.invalidate();
                            break;
                        }
                        else if(isCellUnSet) break;
                        else{
                            clickedCell = new String[]{temp[0], temp[1], temp[2], temp[3], temp[4], String.valueOf(i)};
                            cellsDetail[column][20 - row].setType("unexplored");
                            cellsDetail[column][20 - row].paint = unExplored;
                            obstacleDrag = true;
                            obstacleCoordinates.remove(i);
                            directionOfObstacleCoordinates.remove(i);
                            Toast.makeText(getContext(), "Dragging obstacle!", Toast.LENGTH_SHORT).show();
                            this.isObstacleDirectionCoordinatesSet = false;
                            this.invalidate();
                            return true;
                        }
                    }
                ToggleButton startbtn = ((Activity) this.getContext()).findViewById(R.id.startpointbtn);

                if (isStartCoordinatesSet) {
                    if(!isPositionInGrid((int) event.getX(),(int) event.getY(), true) || !validStartPosition(column, row)){
                        Toast.makeText(getContext(), "invalid position for start point!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    MainActivity.getRaceCar().setVisibility(View.VISIBLE);
                    if (isRobotDrawable) {
                        int[] startCoord = this.getStartCoordinates();
                        if (startCoord[0] >= 2 && startCoord[1] >= 2) {
                            startCoord[1] = this.convertRow(startCoord[1]);
                            for (int x = startCoord[0] - 1; x <= startCoord[0] + 1; x++)
                                for (int y = startCoord[1] - 1; y <= startCoord[1] + 1; y++)
                                    cellsDetail[x][y].setType("unexplored");
                        }
                    } else
                        isRobotDrawable = true;
                    this.setStartCoordinates(column, row);
                    isStartCoordinatesSet = false;
                    String direction = getRobotDirection();
                    if (direction.equals("None")) {
                        direction = "up";
                    }
                    try {
                        int directionInt = 0;
                        switch (direction) {
                            case "left":
                                directionInt = 3;
                                break;
                            case "right":
                                directionInt = 1;
                                break;
                            case "down":
                                directionInt = 2;
                                break;
                        }
                        MainActivity.sendMessageToBlueTooth("STARTRUN");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateAxisOfRobot(column, row, direction);
                    if (startbtn.isChecked())
                        startbtn.toggle();
                    this.invalidate();
                    return true;
                }
                if (isObstacleDirectionCoordinatesSet) {

                    this.setCoordinatesOfObstacle(column, row);
                    setObstacleDirectionCoordinate(column, row, obstacleDirection);
//                        MainActivity.sendMessageToBlueTooth(String.format("Updated Obstacle coordinates %d,%d",column, convertRow(row)));
                    this.isObstacleDirectionCoordinatesSet = false;
                    isAddObstacle = true;
                    this.invalidate();
                    return true;
                }
                if (isObstacleSet) {
                    //this.setCoordinatesOfObstacle(column, row);
                    this.isObstacleDirectionCoordinatesSet = false;
                    this.invalidate();
                    return true;
                }
                if (isExploring) {
                    cellsDetail[column][20 - row].setType("explored");
                    this.invalidate();
                    return true;
                }
                if (isCellUnSet) {
                    //                ArrayList<int[]> obstacleCoord = this.getCoordinatesOfObstacle();
                    cellsDetail[column][20 - row].setType("unexplored");
                    for (int i = 0; i < obstacleCoord.size(); i++)
                        if (obstacleCoord.get(i)[0] == column && obstacleCoord.get(i)[1] == row) {
                            obstacleCoord.remove(i);
                            directionOfObstacleCoordinates.remove(i);
                        }
                    isCellUnSet = false;
                    this.invalidate();
                    return true;
                }
                break;
        }
        showLog("Exiting onTouchEvent");
        return false;
    }

    public void toggleCheckedBtn(String buttonName) {
        ToggleButton startButton = ((Activity) this.getContext()).findViewById(R.id.startpointbtn);
        ToggleButton addObstacleButton = ((Activity) this.getContext()).findViewById(R.id.addobstaclebtn);
        ToggleButton ChangeDirectionBtn = ((Activity) this.getContext()).findViewById(R.id.ChangeDirectionBtn);
        ImageButton clearButton = ((Activity) this.getContext()).findViewById(R.id.clearbtn);

        if (!buttonName.equals("startButton"))
            if (startButton.isChecked()) {
                this.setStartCoordStatus(false);
                startButton.toggle();
            }
        if (!buttonName.equals("ChangeDirectionBtn"))
            if (ChangeDirectionBtn.isChecked()){
                this.setChangeDirection(false);
                ChangeDirectionBtn.toggle();
                ChangeDirectionBtn.setBackground(getResources().getDrawable(R.drawable.change_dir_on));
                ChangeDirectionBtn.setText(getResources().getString(R.string.toggleDirection));
            }
        if (!buttonName.equals("addObstacleButton"))
        {
            if (addObstacleButton.isChecked()) {
                this.setSetObstacleStatus(false);
                addObstacleButton.toggle();
            }
            if (!buttonName.equals("ChangeDirectionBtn")) {
                ((Activity) this.getContext()).findViewById(R.id.eastObstacleBTN).setVisibility(View.GONE);
                ((Activity) this.getContext()).findViewById(R.id.northObstacleBTN).setVisibility(View.GONE);
                ((Activity) this.getContext()).findViewById(R.id.southObstacleBTN).setVisibility(View.GONE);
                ((Activity) this.getContext()).findViewById(R.id.westObstacleBTN).setVisibility(View.GONE);
                ((Activity) this.getContext()).findViewById(R.id.xcoord).setVisibility(View.GONE);
                ((Activity) this.getContext()).findViewById(R.id.ycoord).setVisibility(View.GONE);
                ((Activity) this.getContext()).findViewById(R.id.obstaclebtn).setVisibility(View.GONE);
            }
        }

        if (!buttonName.equals("clearButton"))
            if (clearButton.isEnabled())
                this.setUnSetCellStatus(false);
    }


    public void resetMap() {
        showLog("Entering resetMap");
        TextView robotStatusTextView = ((Activity) this.getContext()).findViewById(R.id.robotStatusTextView);
        TextView xaxisTextView = ((Activity) this.getContext()).findViewById(R.id.xAxisTextView);
        TextView yaxisTextView = ((Activity) this.getContext()).findViewById(R.id.yAxisTextView);
        TextView dirTextView = ((Activity) this.getContext()).findViewById(R.id.directionAxisTextView);
        Switch phoneTiltSwitch = ((Activity) this.getContext()).findViewById(R.id.phoneTiltSwitch);
        MainActivity.getRaceCar().setVisibility(View.INVISIBLE);
        MainActivity.sendMessageToBlueTooth("OBSTACLECLEAR");
        xaxisTextView.setText("-");
        yaxisTextView.setText("-");
        dirTextView.setText("-");
        robotStatusTextView.setText("Resetting Map");
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here
                        robotStatusTextView.setText("Ready To Start");
                    }
                },
                2500
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();


        this.toggleCheckedBtn("None");

        if (phoneTiltSwitch.isChecked()) {
            phoneTiltSwitch.toggle();
            phoneTiltSwitch.setText(getResources().getString(R.string.tilt_off));
        }

        receivedJsonObject = null;
        bpMapInfo = null;
        startPoint = new int[]{-1, -1};
        currentPoint = new int[]{-1, -1};
        previousPoint = new int[]{-1, -1};
        robotDirection = "None";
        obstacleCoordinates = new ArrayList<>();
        isMapDrawn = false;
        isRobotDrawable = false;
        isPositionValid = false;
        directionOfObstacleCoordinates = new ArrayList<>();
        Bitmap arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_error);

        showLog("Exiting resetMap");
        this.invalidate();
    }



    public void moveRobot(String direction) {
        showLog("Entering moveRobot");
        setValidPosition(false);
        int[] curCoord = this.getCurrentCoordinates();
        ArrayList<int[]> obstacleCoord = this.getCoordinatesOfObstacle();
        this.setPreviousRobotCoordinates(curCoord[0], curCoord[1]);
        int[] oldCoord = this.getPreviousPoint();
        String robotDirection = getRobotDirection();
        String backupDirection = robotDirection;

        switch (robotDirection) {
            case "up":
                switch (direction) {
                    case "forward":
                        if (curCoord[1] != 19) {
                            curCoord[1] += 1;
                            isPositionValid = true;
                        }
                        break;
                    case "right":
                        robotDirection = "right";
                        break;
                    case "back":
                        if (curCoord[1] != 2) {
                            curCoord[1] -= 1;
                            isPositionValid = true;
                        }
                        break;
                    case "left":
                        robotDirection = "left";
                        break;
                    default:
                        robotDirection = "error up";
                        break;
                }
                break;
            case "right":
                switch (direction) {
                    case "forward":
                        if (curCoord[0] != 19) {
                            curCoord[0] += 1;
                            isPositionValid = true;
                        }
                        break;
                    case "right":
                        robotDirection = "down";
                        break;
                    case "back":
                        if (curCoord[0] != 2) {
                            curCoord[0] -= 1;
                            isPositionValid = true;
                        }
                        break;
                    case "left":
                        robotDirection = "up";
                        break;
                    default:
                        robotDirection = "error right";
                }
                break;
            case "down":
                switch (direction) {
                    case "forward":
                        if (curCoord[1] != 2) {
                            curCoord[1] -= 1;
                            isPositionValid = true;
                        }
                        break;
                    case "right":
                        robotDirection = "left";
                        break;
                    case "back":
                        if (curCoord[1] != 19) {
                            curCoord[1] += 1;
                            isPositionValid = true;
                        }
                        break;
                    case "left":
                        robotDirection = "right";
                        break;
                    default:
                        robotDirection = "error down";
                }
                break;
            case "left":
                switch (direction) {
                    case "forward":
                        if (curCoord[0] != 2) {
                            curCoord[0] -= 1;
                            isPositionValid = true;
                        }
                        break;
                    case "right":
                        robotDirection = "up";
                        break;
                    case "back":
                        if (curCoord[0] != 19) {
                            curCoord[0] += 1;
                            isPositionValid = true;
                        }
                        break;
                    case "left":
                        robotDirection = "down";
                        break;
                    default:
                        robotDirection = "error left";
                }
                break;
            default:
                robotDirection = "error moveCurCoord";
                break;
        }
        if (getValidPosition())
            for (int x = curCoord[0] - 1; x <= curCoord[0] + 1; x++) {
                for (int y = curCoord[1] - 1; y <= curCoord[1] + 1; y++) {
                    for (int i = 0; i < obstacleCoord.size(); i++) {
                        if (obstacleCoord.get(i)[0] != x || obstacleCoord.get(i)[1] != y)
                            setValidPosition(true);
                        else {
                            setValidPosition(false);
                            break;
                        }
                    }
                    if (!getValidPosition())
                        break;
                }
                if (!getValidPosition())
                    break;
            }
        if (getValidPosition())
            this.setCurrentCoordinates(curCoord[0], curCoord[1], robotDirection);
        else {
            if (direction.equals("forward") || direction.equals("back"))
                robotDirection = backupDirection;
            this.setCurrentCoordinates(oldCoord[0], oldCoord[1], robotDirection);
        }
        this.invalidate();
        showLog("Exiting moveRobot");
    }

    public void printRobotStatus(String message) {
        TextView robotStatusTextView = ((Activity) this.getContext()).findViewById(R.id.robotStatusTextView);
        robotStatusTextView.setText(message);
    }
    public String getRobotStatus(){
        TextView robotStatusTextView = ((Activity) this.getContext()).findViewById(R.id.robotStatusTextView);
        return robotStatusTextView.getText().toString();
    }
    public static void setMDF_Exploration_Details(String msg) {
        MDF_Exploration_Details = msg;
    }

    public static void setMDF_Obstacle_Details(String msg) {
        MDF_Obstacle_Details = msg;
    }

    public static String getMDFstring() {
        return MDF_Exploration_Details;
    }

    public static String getMDF_Obstacle_Details() {
        return MDF_Obstacle_Details;
    }

    private ArrayList<String[]> getObstacleDirectionCoord(){
        return directionOfObstacleCoordinates;
    }


    private void setObstacleDirectionCoordinate(int col, int row,String obstacleDirection) {
        showLog("Entering setDirectionCoord");
        String[] directionCoord = new String[5];
        directionCoord[0] = String.valueOf(col);
        directionCoord[1] = String.valueOf(row);
        directionCoord[2] = obstacleDirection;
        directionCoord[3] = String.valueOf(this.getObstacleDirectionCoord().size()+1);
        directionCoord[4] = "F";
        this.getObstacleDirectionCoord().add(directionCoord);

        row = this.convertRow(row);
        cellsDetail[col][row].setType("obstacleDirection");
        showLog("Exiting setDirectionCoord");
    }

    /*
    set text on Obstacle, col refers to x axis, row refers to y axis
    */
    public boolean setObstacleText(String obstacle_Number, String target_ID){
        ArrayList obstacleDirectionCoord = this.getObstacleDirectionCoord();
        for(int i=0;i<obstacleDirectionCoord.size();i++){
            String[] obstacle = (String[]) obstacleDirectionCoord.get(i);
            if(obstacle[3].equals(obstacle_Number)){
                obstacle[3] = target_ID;
                obstacle[4] = "T";
                this.invalidate();
                return true;
            }
        }
        return false;
    }

    private void drawObstacleWithDirection(Canvas canvas,ArrayList<String[]> obstacleDirectionCoord){
        showLog("Entering drawObstacleWithDirection");
        RectF rect;
        int arrayIndex=0;

        for(int i =0; i<obstacleDirectionCoord.size(); i++){
            arrayIndex=i;
            String text;
            int x= Integer.parseInt(obstacleDirectionCoord.get(i)[0]);
            int y= convertRow(Integer.parseInt(obstacleDirectionCoord.get(i)[1]));
            rect = new RectF(x * sizeOfCell, y * sizeOfCell, (x+1) * sizeOfCell, (y+1) * sizeOfCell);
            Paint white = new Paint();
            white.setColor(Color.WHITE);
            white.setTextAlign(Paint.Align.CENTER);
            if(obstacleDirectionCoord.get(i)[4].equals("T")){
                white.setTextSize(25);
                white.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
                text = obstacleDirectionCoord.get(i)[3];
            }
            else{
                white.setTextSize(15);
                if (obstacleDrag) {
                    text = obstacleDirectionCoord.get(i)[3];
                }
                else {
                    obstacleDirectionCoord.get(i)[3] = String.valueOf(i+1);
                    text = String.valueOf(i+1);
                }
            }
            switch(obstacleDirectionCoord.get(i)[2]){
                case "0":
                    obstacleDirectionBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.north_obstacle);
                    break;
                case "1":
                    obstacleDirectionBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.east_obstacle);
                    break;
                case "2":
                    obstacleDirectionBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.west_obstacle);
                    break;
                case "3":
                    obstacleDirectionBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.south_obstacle);
                    break;
                default:
                    break;

            }

            canvas.drawText(text, (cellsDetail[x][y].startX + cellsDetail[x][y].endX) / 2, cellsDetail[x][y].endY + (cellsDetail[x][y].startY - cellsDetail[x][y].endY) / 4, white);
            canvas.drawBitmap(obstacleDirectionBitmap, null, rect, null);
            showLog("Exiting drawObstacleWithDirection");
        }
        if(!obstacleDirectionCoord.isEmpty() && isAddObstacle == true) {
            int x = (Integer.parseInt(obstacleDirectionCoord.get(arrayIndex)[0]) - 1);
            int y = (Integer.parseInt(obstacleDirectionCoord.get(arrayIndex)[1]) - 1);
            int dir = Integer.parseInt(obstacleDirectionCoord.get(arrayIndex)[2]);
            MainActivity.sendMessageToBlueTooth("OBSTACLE: " + "(" + x + "," + y + "," + dir + "," + Integer.parseInt(obstacleDirectionCoord.get(arrayIndex)[3]) + ")");
        }
        isAddObstacle = false;
    }
}
