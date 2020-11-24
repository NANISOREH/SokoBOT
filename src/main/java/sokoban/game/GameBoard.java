package sokoban.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/*
This class contains the logic of the game, and encapsulates both the state of the board and the actions you can take to alter it.
The starting condition of the level is read from a json file.

Seeing it from the point of view of the search algorithms, an instance of this class represents a state in the state space.
In fact, GameBoard objects are duplicated and then altered whenever it's needed to expand a state to obtain all its neighbours.
*/
public class GameBoard implements Cloneable{
    Logger log = Logger.getLogger("GameBoard");
    private Cell[][] board;
    private Cell sokobanCell = null;
    private ArrayList<Cell> goalCells = new ArrayList<>();
    //Boxes are identified by an integer to easily track them down in their roaming
    private HashMap<Integer, Cell> boxCells = new HashMap<>();
    private int rows;
    private int columns;

    /*
        GameBoard constructor, takes a Level and uses it to initialize the instance variables.
    */
    public GameBoard(Level level){

        //Gets the content of the level
        CellContent[][] parsed = level.getContent();

        //updating the instance variables with the number of rows and columns
        rows = parsed.length;
        columns = parsed[1].length;

        //Forming the matrix of Cell objects representing the board
        //and finding the goal Cells and the initial position of Sokoban and the boxes
        board = new Cell[rows][columns];
        int i; int j;
        int countBoxes = 0;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < columns; j++) {
                board[i][j] = new Cell(i, j, parsed[i][j], false, null);
                if (parsed[i][j].equals(CellContent.SOKOBAN)) {
                    sokobanCell = new Cell(i, j, CellContent.SOKOBAN, false, null);
                }
                //Goal tiles are treated as if they were empty tiles with a "goal" boolean set to true
                //because they can also have Sokoban or boxes on them, so I'd like to avoid setting double content
                else if (parsed[i][j].equals(CellContent.GOAL)) {
                    board[i][j].setContent(CellContent.EMPTY);
                    board[i][j].setGoal(true);
                    goalCells.add(board[i][j]);
                }
                else if (parsed[i][j].equals(CellContent.BOX)) {
                    board[i][j].setBoxNumber(countBoxes);
                    boxCells.put(countBoxes, board[i][j]);
                    countBoxes++;
                }
            }
        }

    }

    /*
        Receives an action from the client and modifies the state of the level based on said action.
        Returns true if the action given actually modified the state of the board, or false if it didn't,
        e.g. if the action told Sokoban to move towards a cell occupied by a wall.
    */
    public boolean takeAction (Action action) throws CloneNotSupportedException {
        Cell neighbour = new Cell();
        Cell boxNeighbour = new Cell();
/*
        Depending on the action given, we update the cells involved in the possible changes together with Sokoban's,
        e.g. if we were told to move up, the two cells involved in the "transaction" would be the north neighbour of Sokoban,
        and optionally the one over said neighbour, if Sokoban has to push a box.
*/
        switch (action) {

            case MOVE_UP: {
                neighbour = getNorth(sokobanCell);
                boxNeighbour = getNorth(neighbour);
                break;
            }
            case MOVE_DOWN: {
                neighbour = getSouth(sokobanCell);
                boxNeighbour = getSouth(neighbour);
                break;
            }
            case MOVE_LEFT: {
                neighbour = getWest(sokobanCell);
                boxNeighbour = getWest(neighbour);
                break;
            }
            case MOVE_RIGHT: {
                neighbour = getEast(sokobanCell);
                boxNeighbour = getEast(neighbour);
                break;
            }

        }

        //if we were told to move on a free cell, we just swap Sokoban's cell content with that free cell
        if (neighbour.getContent() == CellContent.EMPTY) {
            swapCells(neighbour, sokobanCell);
            return true;
        }
        else if (neighbour.getContent() == CellContent.WALL) { //there's a wall, we can't move
            return false;
        }
        else if (neighbour.getContent() == CellContent.BOX) {
            //we were told to move "against" a box
            if (boxNeighbour.getContent() == CellContent.EMPTY) {
                //the box's neighbour is a free cell, so the box moves there and Sokoban moves to the cell where the box was
                swapCells(neighbour, boxNeighbour);
                swapCells(neighbour, sokobanCell);
                return true;
            }
            else //the box's neighbour is a wall or another box, we can't move it
                return false;
        }

        return false;
    }

    /*
        Private helper method that swaps the content of two cells and updates the instance variables accordingly
    */
    private void swapCells (Cell first, Cell second) throws CloneNotSupportedException {

        //Swapping the content of the two cells
        Cell temp = (Cell) board[first.getRow()][first.getColumn()].clone();
        board[first.getRow()][first.getColumn()].setContent(second.getContent());
        board[second.getRow()][second.getColumn()].setContent(temp.getContent());

        //Sokoban surely changed position after the swap, we update the instance variable accordingly
        if (first.getContent() == CellContent.SOKOBAN) {
            sokobanCell = (Cell) first.clone();
        }
        else if (second.getContent() == CellContent.SOKOBAN) {
            sokobanCell = (Cell) second.clone();
        }

        //if one of the two cells contains a box after the swap, it means that it was in the other one before:
        //we update the boxCells structure by removing the old box cell and adding the new one in its place
        if (first.getContent() == CellContent.BOX) {
            if (second.getBoxNumber() != null) {
                first.setBoxNumber(second.getBoxNumber());
                second.setBoxNumber(null);
                boxCells.remove(first.getBoxNumber());
                boxCells.put(first.getBoxNumber(), first);
            }
        }
        else if (second.getContent() == CellContent.BOX) {
            if (first.getBoxNumber() != null) {
                second.setBoxNumber(first.getBoxNumber());
                first.setBoxNumber(null);
                boxCells.remove(second.getBoxNumber());
                boxCells.put(second.getBoxNumber(), second);
            }
        }


    }

    /*
        Checks for the victory conditions: every goal cell must contain a box
    */
    public boolean checkVictory() {
        for (Cell c : goalCells) {
            if (c.getContent() != CellContent.BOX)
                return false;
        }
        return true;
    }

    /*
        Returns the number of boxes that are currently in a goal cell
    */
    public int countReachedGoals() {
        int count = 0;
        for (Cell c : goalCells) {
            if (c.getContent() == CellContent.BOX)
                count++;
        }
        return count;
    }

    /*
        Takes a given cell on the board, returns the adjacent cell to the north of it
    */
    public Cell getNorth(Cell given) {
        Cell neighbour;

        if (given.getRow() - 1 >= 0) {
            neighbour = board[given.getRow() - 1][given.getColumn()];
            return neighbour;
        }
        else
            return null;
    }

    /*
        Takes a cell on the board, returns the adjacent cell to the south of it
    */
    public Cell getSouth(Cell given) {
        Cell neighbour;
        if (given.getRow() + 1 < rows) {
            neighbour = board[given.getRow() + 1][given.getColumn()];
            return neighbour;
        }
        else
            return null;
    }

    /*
        Takes a cell on the board, returns the adjacent cell to the east of it
    */
    public Cell getEast(Cell given) {
        Cell neighbour;
        if (given.getColumn() + 1 < columns) {
            neighbour = board[given.getRow()][given.getColumn() + 1];
            return neighbour;
        }
        else
            return null;
    }

    /*
        Takes a cell on the board, returns the adjacent cell to the west of it
    */
    public Cell getWest(Cell given) {
        Cell neighbour;
        if (given.getColumn() - 1 >= 0) {
            neighbour = board[given.getRow()][given.getColumn() - 1];
            return neighbour;
        }
        else
            return null;
    }

    public Cell getSokobanCell() {
        return sokobanCell;
    }

    public Cell[][] getBoard () {
        return board;
    }

    public void setBoard (Cell[][] board) {
        this.board = board;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public ArrayList<Cell> getGoalCells() {
        return goalCells;
    }

    public HashMap<Integer, Cell> getBoxCells() {
        return boxCells;
    }

    /*
        Returns a deep copy of a GameBoard object
    */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = super.clone();
        GameBoard cloned = (GameBoard) obj;
        cloned.sokobanCell = new Cell();
        cloned.goalCells = new ArrayList<Cell>();
        cloned.boxCells = new HashMap<>();

        Cell [][] copy = new Cell[rows][columns];
        int countBoxes = 0;
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                copy[i][j] = (Cell) board[i][j].clone();
                if (copy[i][j].getContent() == CellContent.SOKOBAN)
                    cloned.sokobanCell = copy[i][j];
                if (copy[i][j].isGoal())
                    cloned.goalCells.add(copy[i][j]);
                if (copy[i][j].getContent() == CellContent.BOX) {
                    cloned.boxCells.put(countBoxes, copy[i][j]);
                    countBoxes++;
                }
            }
        }

        cloned.setBoard(copy);
        cloned.rows = this.rows;
        cloned.columns = this.columns;

        return cloned;
    }

}
