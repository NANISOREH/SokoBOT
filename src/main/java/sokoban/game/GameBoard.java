package sokoban.game;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
This class contains the logic of the game, and encapsulates both the state of the board and the actions you can take to alter it.
The starting condition of the level is read from a json file.

Seeing it from the point of view of the search algorithms, an instance of this class represents a state in the state space.
In fact, GameBoard objects are duplicated and then altered whenever it's needed to expand a state to obtain all its neighbours.
*/
public class GameBoard implements Cloneable{
    Logger log = Logger.getLogger("Level");
    private Cell[][] board;
    private Cell sokobanCell = null;
    private ArrayList<Cell> goalCells = new ArrayList<>();
    private int rows;
    private int columns;

/*
    GameBoard standard constructor
*/
    public GameBoard(Cell[][] board, Cell sokobanCell, ArrayList<Cell> goalCells, int rows, int columns) {
        this.board = board;
        this.sokobanCell = sokobanCell;
        this.goalCells = goalCells;
        this.rows = rows;
        this.columns = columns;
    }

    /*
        GameBoard alternate constructor, reads and parses a level from a json file, then uses parsed data to initialize the instance variables.
        The client of the class can choose which level to load via the level parameter.
    */
    public GameBoard(int level){
        //Reading the json file into a string
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("levels/level" + level + ".json")))
        {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                jsonBuilder.append(sCurrentLine).append("\n");
            }
        }
        catch (IOException e)
        {
            log.warning("The selected level was not found");
            e.printStackTrace();
        }
        String jsonLevel = jsonBuilder.toString();

        //Parsing the json my mapping it to a matrix of the enum CellContent
        Gson gson = new Gson();
        CellContent[][] parsed = gson.fromJson(jsonLevel, CellContent[][].class);

        //updating the instance variables with the number of rows and columns
        rows = parsed.length;
        columns = parsed[1].length;

        //Forming the matrix of Cell objects representing the board
        //and finding the goal Cells and the initial position of Sokoban and the boxes
        board = new Cell[rows][columns];
        int i; int j;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < columns; j++) {
                board[i][j] = new Cell(i, j, parsed[i][j], false);
                if (parsed[i][j].equals(CellContent.SOKOBAN)) {
                    sokobanCell = new Cell(i, j, CellContent.SOKOBAN, false);
                }
                else if (parsed[i][j].equals(CellContent.GOAL)) {
                    board[i][j].setContent(CellContent.EMPTY);
                    board[i][j].setGoal(true);
                    goalCells.add(board[i][j]);
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
                neighbour = board[sokobanCell.getRow() - 1][sokobanCell.getColumn()];
                if (sokobanCell.getRow() - 2 >= 0)
                    boxNeighbour = board[sokobanCell.getRow() - 2][sokobanCell.getColumn()];

                break;
            }
            case MOVE_DOWN: {
                neighbour = board[sokobanCell.getRow() + 1][sokobanCell.getColumn()];
                if (sokobanCell.getRow() + 2 < board.length)
                    boxNeighbour = board[sokobanCell.getRow() + 2][sokobanCell.getColumn()];

                break;
            }
            case MOVE_LEFT: {
                neighbour = board[sokobanCell.getRow()][sokobanCell.getColumn() - 1];
                if (sokobanCell.getColumn() - 2 >= 0)
                    boxNeighbour = board[sokobanCell.getRow()][sokobanCell.getColumn() - 2];

                break;
            }
            case MOVE_RIGHT: {
                neighbour = board[sokobanCell.getRow()][sokobanCell.getColumn() + 1];
                if (sokobanCell.getColumn() + 2 < board[1].length)
                    boxNeighbour = board[sokobanCell.getRow()][sokobanCell.getColumn() + 2];

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
            else //the box's neighbour is a wall or another box, we can't move
                return false;
        }

        return false;
    }

/*
    Private helper method that swaps the content of two cells and updates the instance variable with Sokoban position
*/
    private void swapCells (Cell first, Cell second) throws CloneNotSupportedException {
        Cell temp = (Cell) board[first.getRow()][first.getColumn()].clone();
        board[first.getRow()][first.getColumn()].setContent(second.getContent());
        board[second.getRow()][second.getColumn()].setContent(temp.getContent());

        if (first.getContent() == CellContent.SOKOBAN) {
            sokobanCell = (Cell) first.clone();
        }
        else if (second.getContent() == CellContent.SOKOBAN) {
            sokobanCell = (Cell) second.clone();
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
    public int checkPartialVictory() {
        int count = 0;
        for (Cell c : goalCells) {
            if (c.getContent() == CellContent.BOX)
                count++;
        }
        return count;
    }

    public Cell getNorth(Cell given) {
        Cell neighbour = new Cell();
        neighbour = board[given.getRow() - 1][given.getColumn()];
        return neighbour;
    }

    public Cell getSouth(Cell given) {
        Cell neighbour = new Cell();
        neighbour = board[given.getRow() + 1][given.getColumn()];
        return neighbour;
    }

    public Cell getEast(Cell given) {
        Cell neighbour = new Cell();
        neighbour = board[given.getRow()][given.getColumn() + 1];
        return neighbour;
    }

    public Cell getWest(Cell given) {
        Cell neighbour = new Cell();
        neighbour = board[given.getRow()][given.getColumn() - 1];
        return neighbour;
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

    public void setGoalCells(ArrayList<Cell> goalCells) {
        this.goalCells = goalCells;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = super.clone();
        GameBoard cloned = (GameBoard) obj;
        cloned.sokobanCell = new Cell();
        cloned.goalCells = new ArrayList<Cell>();

        Cell [][] copy = new Cell[rows][columns];
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                copy[i][j] = (Cell) board[i][j].clone();
                if (copy[i][j].getContent() == CellContent.SOKOBAN)
                    cloned.sokobanCell = copy[i][j];
                if (copy[i][j].isGoal())
                    cloned.goalCells.add(copy[i][j]);
            }

        }

        cloned.setBoard(copy);
        cloned.rows = this.rows;
        cloned.columns = this.columns;

        return cloned;
    }

}
