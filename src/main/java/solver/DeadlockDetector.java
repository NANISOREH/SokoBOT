package solver;

import game.Action;
import game.Cell;
import game.CellContent;
import game.GameBoard;
import solver.configuration.DDRoutine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class DeadlockDetector {
    private static Logger log = Logger.getLogger("DeadlockDetector");
    private static DDRoutine routine = DDRoutine.ALL_ROUTINES;
    private static ArrayList<Cell> deadCells = new ArrayList<>();
    private static ArrayList<CellContent[][]> TwoTwoDeadlocks = new ArrayList<>();
    private static ArrayList<CellContent[][]> ThreeThreeDeadlocks = new ArrayList<>();

    public static boolean isDeadlock (Node node) throws CloneNotSupportedException {
        switch (routine) {
            case ALL_ROUTINES : {
                if (isDeadPosition(node)) return true;
                else return isInDeadlockTable(node);
            }
            case LOOKUP_TABLES: {
                return isInDeadlockTable(node);
            }
            case NO_DEADLOCK_DETECTION: {
                return false;
            }
            case DEAD_POSITIONS: {
                return isDeadPosition(node);
            }
        }

        return false;
    }

    private static boolean isInDeadlockTable(Node node) {
        if (node.getLastMovedBox() == null)
            return false;
        
        GameBoard board = node.getGame();
        Cell box = node.getGame().getBoxCells().get(node.getLastMovedBox());

        if (box.isGoal())
            return false;

/*        log.info(board.getSokobanCell() + "");
        log.info(box + "");*/

        Action push = null;
        if (board.getNorth(box).getContent() == CellContent.SOKOBAN)
            push = Action.MOVE_DOWN;
        else if (board.getSouth(box).getContent() == CellContent.SOKOBAN)
            push = Action.MOVE_UP;
        else if (board.getEast(box).getContent() == CellContent.SOKOBAN)
            push = Action.MOVE_LEFT;
        else if (board.getWest(box).getContent() == CellContent.SOKOBAN)
            push = Action.MOVE_RIGHT;
        
        CellContent[][] first = new CellContent[2][2];
        CellContent[][] second = new CellContent[2][2];

        switch (push) {
            case MOVE_LEFT : {
                first[1][0] = CellContent.BOX;
                first[1][1] = board.getNorth(box).getContent();
                Cell temp = board.getEast(box);
                first[0][0] = temp.getContent();
                first[0][1] = board.getNorth(temp).getContent();

                second[1][1] = CellContent.BOX;
                second[1][0] = board.getSouth(box).getContent();
                temp = board.getEast(box);
                second[0][1] = temp.getContent();
                second[0][0] = board.getSouth(temp).getContent();

/*                log.info("\n" + first[0][0] + " " + first[0][1] + "\n" + first[1][0] + " " + first[1][1]);
                log.info("\n" + second[0][0] + " " + second[0][1] + "\n" + second[1][0] + " " + second[1][1]);*/

                break;
            }
            case MOVE_RIGHT : {
                first[1][0] = CellContent.BOX;
                first[1][1] = board.getSouth(box).getContent();
                Cell temp = board.getEast(box);
                first[0][0] = temp.getContent();
                first[0][1] = board.getSouth(temp).getContent();

                second[1][1] = CellContent.BOX;
                second[1][0] = board.getNorth(box).getContent();
                temp = board.getEast(box);
                second[0][1] = temp.getContent();
                second[0][0] = board.getNorth(temp).getContent();

                break;
            }
            case MOVE_UP : {
                first[1][0] = box.getContent();
                first[0][0] = board.getNorth(box).getContent();
                Cell temp = board.getEast(box);
                first[1][1] = temp.getContent();
                first[0][1] = board.getNorth(temp).getContent();

                second[1][1] = box.getContent();
                second[0][1] = board.getNorth(box).getContent();
                temp = board.getWest(box);
                second[0][0] = temp.getContent();
                second[1][0] = board.getSouth(temp).getContent();
                break;
            }
            case MOVE_DOWN : {
                first[0][0] = CellContent.BOX;
                first[1][0] = board.getSouth(box).getContent();
                Cell temp = board.getEast(box);
                first[0][1] = temp.getContent();
                first[1][1] = board.getSouth(temp).getContent();

                second[0][1] = CellContent.BOX;
                second[1][1] = board.getSouth(box).getContent();
                temp = board.getEast(box);
                second[0][0] = temp.getContent();
                second[1][0] = board.getSouth(temp).getContent();
                break;
            }
        }


        if (tableCheck(first) || tableCheck(second)) {
            //log.info("found");
            return true;
        }
        else
            return false;
    }

    private static boolean tableCheck(CellContent[][] submatrix) {

        for (CellContent[][] c : TwoTwoDeadlocks) {
            if (c[0][0] == submatrix[0][0] && c[0][1] == submatrix[0][1] && c[1][0] == submatrix[1][0] && c[1][1] == submatrix[1][1])
                return true;
        }
        return false;
    }

/*
    Confronts the cell containing the last moved box (if there's one) with the list of the dead cells and returns true
    if said cell is in the list.
    In other words, we're checking if we just pushed a box into a dead position.
*/
    public static boolean isDeadPosition(Node node) {

        if (node.getLastMovedBox() == null)
            return false;

        int boxNumber =  node.getLastMovedBox();
        Cell lastMoved = node.getGame().getBoxCells().get(boxNumber);

        if (deadCells.contains(lastMoved))
            return true;
        else
            return false;
    }


    /*
        This method launches the search for dead positions before the search for solutions starts.
        It empties the level and tries to put a box in every position, one at a time, to check if said position is dead
        adding dead positions to a list that will be consulted whenever we push a box during the solution searching,
    */
    public static void handleDeadPositions(GameBoard toSolve) throws CloneNotSupportedException {
        Node node = new Node(toSolve, new ArrayList<>());

        //Clearing the node's game board
        HashMap<Integer, Cell> boxCells = node.getGame().getBoxCells();
        int target = boxCells.size();
        for (int i = 0; i<target; i++) {
            boxCells.get(i).setContent(CellContent.EMPTY);
            boxCells.remove(i);

        }
        boxCells.clear();


        //trying the "deadness" of every possible position
        Cell[][] board = toSolve.getBoard();
        int i,j;
        for (i = 0; i < toSolve.getRows(); i++) {
            for (j = 0; j < toSolve.getColumns(); j++) {

                //we navigate every cell where a box could end up
                if (board[i][j].getContent() == CellContent.EMPTY || board[i][j].getContent() == CellContent.SOKOBAN ) {

                    //we put a box here (the rest of the board will be empty)
                    board[i][j].setContent(CellContent.BOX);
                    boxCells.put(0, board[i][j]);

                    //adding a cell to the dead positions' list if the box we just placed can't be pushed to a goal
                    if (searchDeadPosition(new Node(toSolve, new ArrayList<>()))) {
                        deadCells.add((Cell) board[i][j].clone());
                    }

                    //removing the box to prepare for the next iteration, where a new position will be tried
                    boxCells.remove(0);
                    board[i][j].setContent(CellContent.EMPTY);
                }

            }
        }

    }

/*
    This method takes a node representing a state with just one box and returns true if said box is in a dead position
    Uses a best-first search to do so.
*/
    private static boolean searchDeadPosition (Node node) throws CloneNotSupportedException {
        ArrayList<Long> transpositionTable = new ArrayList<>();

        //The frontier in the search will be ordered by distance to the nearest goal
        PriorityQueue<Node> frontier = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node extendedNode, Node t1) {
                int minimumFirst = getDistanceToNearestGoal(extendedNode);
                int minimumSecond = getDistanceToNearestGoal(t1);
                return Integer.compare(minimumFirst, minimumSecond);
            }
        });

        //adding the starting point to the frontier and to the transposition table
        frontier.add(node);
        transpositionTable.add(node.hash());

        for (int count = 0; true; count++) {

            //we are greedily always extracting the node with the lowest distance from the closest goal
            Node n = frontier.remove();
            Node temp;

            //checking if the push that brought us to this node put the box onto a goal
            if (n.getGame().getBoxCells().get(0).isGoal()) {
                return false;
            }

            //expanding the current node with all possible pushes
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_UP);
            if (!transpositionTable.contains(temp.hash())) {
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_DOWN);
            if (!transpositionTable.contains(temp.hash())) {
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_LEFT);
            if (!transpositionTable.contains(temp.hash())) {
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_RIGHT);
            if (!transpositionTable.contains(temp.hash())){
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }

            //nothing left on the frontier, there was no way to push the box to a goal cell
            //that means the starting node had that box in a dead position
            if (frontier.isEmpty()) {
                return true;
            }
        }

    }


    private static int getDistanceToNearestGoal (Node extendedNode) {
        int minimumFirst = Integer.MAX_VALUE;

        for (Cell c : extendedNode.getGame().getGoalCells()) {
            int temp = extendedNode.getGame().getBoxCells().get(0).manhattanDistance(c);
            if (temp < minimumFirst)
                minimumFirst = temp;
        }

        return minimumFirst;
    }

    public static void setRoutine(DDRoutine routine) {
        DeadlockDetector.routine = routine;
        deadCells.clear();
    }

/*
    Inserting some deadlocks into the lookup table
*/
    public static void populateDeadlocks () {
        CellContent[][] deadlock = parseDeadlock(CellContent.WALL, CellContent.WALL,
                CellContent.WALL, CellContent.BOX);
        TwoTwoDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.BOX,
                CellContent.WALL, CellContent.BOX);
        TwoTwoDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.BOX, CellContent.BOX,
                CellContent.WALL, CellContent.BOX);
        TwoTwoDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.BOX, CellContent.BOX,
                CellContent.BOX, CellContent.BOX);
        TwoTwoDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.WALL,
                CellContent.EMPTY, CellContent.BOX, CellContent.WALL);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.WALL,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.WALL);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.BOX, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.BOX, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.BOX, CellContent.BOX, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.WALL, CellContent.BOX, CellContent.EMPTY,
                CellContent.BOX, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.BOX, CellContent.BOX, CellContent.EMPTY,
                CellContent.BOX, CellContent.EMPTY, CellContent.BOX,
                CellContent.EMPTY, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.EMPTY, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.WALL,
                CellContent.WALL, CellContent.BOX, CellContent.WALL);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.EMPTY, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.WALL,
                CellContent.WALL, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.EMPTY, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.WALL, CellContent.BOX, CellContent.WALL);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.EMPTY, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.WALL,
                CellContent.BOX, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.EMPTY, CellContent.WALL, CellContent.EMPTY,
                CellContent.WALL, CellContent.EMPTY, CellContent.BOX,
                CellContent.BOX, CellContent.BOX, CellContent.BOX);
        ThreeThreeDeadlocks.add(deadlock);

        deadlock = parseDeadlock(CellContent.EMPTY, CellContent.BOX, CellContent.WALL,
                CellContent.WALL, CellContent.BOX, CellContent.EMPTY,
                CellContent.EMPTY, CellContent.EMPTY, CellContent.EMPTY);
        ThreeThreeDeadlocks.add(deadlock);

        //rotating each deadlock 3 times to obtain the deadlocks in every possible orientation
        ArrayList<CellContent[][]> rotated = new ArrayList<>();
        for (CellContent[][] c : TwoTwoDeadlocks) {
            CellContent[][] temp;
            temp = rotateDeadlock(c);
            rotated.add(temp);
            temp = rotateDeadlock(temp);
            rotated.add(temp);
            temp = rotateDeadlock(temp);
            rotated.add(temp);
        }
        TwoTwoDeadlocks.addAll(rotated);

        rotated = new ArrayList<>();
        for (CellContent[][] c : ThreeThreeDeadlocks) {
            CellContent[][] temp;
            temp = rotateDeadlock(c);
            rotated.add(temp);
            temp = rotateDeadlock(temp);
            rotated.add(temp);
            temp = rotateDeadlock(temp);
            rotated.add(temp);
        }
        ThreeThreeDeadlocks.addAll(rotated);

    }

/*
    Parses a varargs of cell contents into a matrix that's usable for lookups
*/
    private static CellContent[][] parseDeadlock(CellContent... content) {
        CellContent[][] deadlock;
        int mod;
        if (content.length == 4) {
            deadlock = new CellContent[2][2];
            mod = 2;
        }
        else if (content.length == 9) {
            deadlock = new CellContent[3][3];
            mod = 3;
        }
        else return null;

        int row=0, column=0;
        for (int i = 0; i < content.length; i++) {
            deadlock[row][column] = content[i];
            column++;
            if (column > 0 && column % mod == 0) {
                column = 0;
                row++;
            }
        }

        return deadlock;
    }

/*
    Rotates a deadlock matrix clockwise.
    The first row will become the last column, the second row will become the column n-1 and so on
*/
    private static CellContent[][] rotateDeadlock(CellContent[][] deadlock) {
        ArrayList<CellContent[]> rows = new ArrayList<>();

        for (int i = 0; i < deadlock.length; i++) {
            CellContent[] row = deadlock[i];
            rows.add(row);
        }

        CellContent[][] result = new CellContent[deadlock.length][deadlock[0].length];
        for (int i = deadlock[0].length - 1; i >= 0; i--) {
            CellContent[] row = rows.get(deadlock.length - 1 - i);
            for (int j = 0; j<row.length; j++) {
                result[j][i] = row[j];
            }
        }

        return result;
    }

}
