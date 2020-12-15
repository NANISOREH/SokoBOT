package solver;

import game.Action;
import game.Cell;
import game.CellContent;
import game.GameBoard;
import solver.configuration.DDRoutine;

import java.util.*;
import java.util.logging.Logger;

public class DeadlockDetector {
    private static Logger log = Logger.getLogger("DeadlockDetector");
    private static DDRoutine routine = DDRoutine.ALL_ROUTINES;
    private static int prunedNodes = 0;
    private static ArrayList<Cell> deadCells = new ArrayList<>();
    private static ArrayList<CellContent[][]> TwoTwoDeadlocks = new ArrayList<>();

    //this structure will contain, for every box involved in a freeze deadlock, a boolean meaning whether or not
    //these boxes are on a goal cell. If every goal cell involved in a freeze deadlock are on a goal cell (no false values)
    //said boxes are deadlocked but we don't mind, since they're already on goal.
    private static ArrayList<Boolean> frozenBoxesOnGoal = new ArrayList<>();

/*
    Facade method that the client launches to get the deadlock detection routines asked by configuring the routine variable.
    Intuitively, it returns true if a deadlock was found
*/
    public static boolean isDeadlock (GameBoard board) throws CloneNotSupportedException {
        if (board.getLastMovedBox() == null)
            return false;
        if (board.checkVictory())
            return false;

        switch (routine) {
            case ALL_ROUTINES : {

                if (isDeadPosition(board)) return true;
                else if (isInDeadlockTable(board)) return true;
                else if (isFrozenBox(board.getBoxCells().get(board.getLastMovedBox()), (GameBoard) board.clone())) {
                    //the box is certainly frozen, but we first have to check if the freeze deadlock involves boxes
                    //that aren't on a goal cell
                    if (frozenBoxesOnGoal.contains(false)) {
                        frozenBoxesOnGoal.clear();
                        prunedNodes++;
                        return true;
                    }
                    frozenBoxesOnGoal.clear();
                }
                return false;

            }
            case LOOKUP_TABLES: {
                return isInDeadlockTable(board);
            }
            case FROZEN_BOXES : {

                if (isFrozenBox(board.getBoxCells().get(board.getLastMovedBox()), (GameBoard) board.clone())) {
                    if (frozenBoxesOnGoal.contains(false)) {
                        frozenBoxesOnGoal.clear();
                        prunedNodes++;
                        return true;
                    }
                    frozenBoxesOnGoal.clear();
                }
                return false;

            }
            case NO_DEADLOCK_DETECTION: {
                return false;
            }
            case DEAD_POSITIONS: {
                return isDeadPosition(board);
            }
        }

        return false;
    }


    //ONLINE OPERATIONS

    private static boolean isFrozenBox(Cell box, GameBoard board) throws CloneNotSupportedException {
        boolean frozen;

        //Horizontal checks
        frozen = isHorizontallyFrozen(box, board);
        if (!frozen) {
            box.setContent(CellContent.WALL);
            if (board.getEast(box).getContent() == CellContent.BOX)
                frozen = isFrozenBox(board.getEast(box), board);
            if (board.getWest(box).getContent() == CellContent.BOX)
                frozen = isFrozenBox(board.getWest(box), board);
            box.setContent(CellContent.BOX);
        }

        //the box is not frozen horizontally, can't be a deadlock
        if (!frozen) return false;

        //Vertical checks
        frozen = isVerticallyFrozen(box, board);
        if (!frozen) {
            box.setContent(CellContent.WALL);
            if (board.getNorth(box).getContent() == CellContent.BOX)
                frozen = isFrozenBox(board.getNorth(box), board);
            if (board.getSouth(box).getContent() == CellContent.BOX)
                frozen = isFrozenBox(board.getSouth(box), board);
            box.setContent(CellContent.BOX);
        }

        //the box passed both horizontal and vertical checks, it's frozen
        if (frozen) {
            //for every box that's declared frozen, we keep track of whether said box is on a goal or not
            //if all of the boxes touched by this freeze deadlock are on a goal, it's not a real deadlock
            if (box.isGoal()) frozenBoxesOnGoal.add(true);
            else frozenBoxesOnGoal.add(false);

            return true;
        }
        else return false;
    }

    private static boolean isHorizontallyFrozen(Cell box, GameBoard board) {
        if (board.getWest(box).getContent() == CellContent.WALL || board.getEast(box).getContent() == CellContent.WALL) {
            return true;
        }
        else if (board.getWest(box).isDeadPosition() && board.getEast(box).isDeadPosition()) {
            return true;
        }

        return false;
    }

    private static boolean isVerticallyFrozen(Cell box, GameBoard board) {
        if (board.getNorth(box).getContent() == CellContent.WALL || board.getSouth(box).getContent() == CellContent.WALL)
            return true;
        else if (board.getNorth(box).isDeadPosition() && board.getSouth(box).isDeadPosition())
            return true;

        return false;
    }

    private static boolean isInDeadlockTable(GameBoard board) {
        
        Cell box = board.getBoxCells().get(board.getLastMovedBox());

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

/*
        log.info(push + "\n");
*/

        Cell[][] first = new Cell[2][2];
        Cell[][] second = new Cell[2][2];

        switch (push) {
            case MOVE_LEFT : {
                first[1][0] = box;
                first[1][1] = board.getNorth(box);
                Cell temp = board.getWest(box);
                first[0][0] = temp;
                first[0][1] = board.getNorth(temp);

                second[1][1] = box;
                second[1][0] = board.getSouth(box);
                temp = board.getWest(box);
                second[0][1] = temp;
                second[0][0] = board.getSouth(temp);

/*                log.info("\n" + first[0][0] + " " + first[0][1] + "\n" + first[1][0] + " " + first[1][1]);
                log.info("\n" + second[0][0] + " " + second[0][1] + "\n" + second[1][0] + " " + second[1][1]);*/

                break;
            }
            case MOVE_RIGHT : {
                first[1][0] = box;
                first[1][1] = board.getSouth(box);
                Cell temp = board.getEast(box);
                first[0][0] = temp;
                first[0][1] = board.getSouth(temp);

                second[1][1] = box;
                second[1][0] = board.getNorth(box);
                temp = board.getEast(box);
                second[0][1] = temp;
                second[0][0] = board.getNorth(temp);

/*                log.info("\n" + first[0][0] + " " + first[0][1] + "\n" + first[1][0] + " " + first[1][1]);
                log.info("\n" + second[0][0] + " " + second[0][1] + "\n" + second[1][0] + " " + second[1][1]);*/

                break;
            }
            case MOVE_UP : {
                first[1][0] = box;
                first[0][0] = board.getNorth(box);
                Cell temp = board.getEast(box);
                first[1][1] = temp;
                first[0][1] = board.getNorth(temp);

                second[1][1] = box;
                second[0][1] = board.getNorth(box);
                temp = board.getWest(box);
                second[0][0] = temp;
                second[1][0] = board.getNorth(temp);

/*                log.info("\n" + first[0][0] + " " + first[0][1] + "\n" + first[1][0] + " " + first[1][1]);
                log.info("\n" + second[0][0] + " " + second[0][1] + "\n" + second[1][0] + " " + second[1][1]);*/
                break;
            }
            case MOVE_DOWN : {
                first[0][0] = box;
                first[1][0] = board.getSouth(box);
                Cell temp = board.getEast(box);
                first[0][1] = temp;
                first[1][1] = board.getSouth(temp);

                second[0][1] = box;
                second[1][1] = board.getSouth(box);
                temp = board.getEast(box);
                second[0][0] = temp;
                second[1][0] = board.getSouth(temp);

/*                log.info("\n" + first[0][0] + " " + first[0][1] + "\n" + first[1][0] + " " + first[1][1]);
                log.info("\n" + second[0][0] + " " + second[0][1] + "\n" + second[1][0] + " " + second[1][1]);*/
                break;
            }
        }

        //checking if all the box cells in the patterns are on a goal
        //in these cases we can't prune anything because we might be pruning a solution
        boolean allBoxesFirst = true;
        boolean allBoxesSecond = true;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j<2; j++) {
                if (first[i][j].getContent() == CellContent.BOX && !first[i][j].isGoal())
                    allBoxesFirst = false;
                if (second[i][j].getContent() == CellContent.BOX && !second[i][j].isGoal())
                    allBoxesSecond = false;
            }
        }
        if (allBoxesFirst) return false;
        if (allBoxesSecond) return false;

        if (tableCheck(first) || tableCheck(second)) {
            prunedNodes++;
            return true;
        }
        else
            return false;
    }

    private static boolean tableCheck(Cell[][] submatrix) {

        if (submatrix.length == 2) {
            for (CellContent[][] c : TwoTwoDeadlocks) {
                if (c[0][0] == submatrix[0][0].getContent() && c[0][1] == submatrix[0][1].getContent() &&
                        c[1][0] == submatrix[1][0].getContent() && c[1][1] == submatrix[1][1].getContent())
                    return true;
            }
            return false;
        }

        return false;
    }

/*
    Confronts the cell containing the last moved box (if there's one) with the list of the dead cells and returns true
    if said cell is in the list.
    In other words, we're checking if we just pushed a box into a dead position.
*/
    public static boolean isDeadPosition(GameBoard board) {

        int boxNumber =  board.getLastMovedBox();
        Cell lastMoved = board.getBoxCells().get(boxNumber);

        if (deadCells.contains(lastMoved)) {
            prunedNodes++;
            return true;
        }
        else
            return false;
    }


    //OFFLINE OPERATIONS

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
                if ((board[i][j].getContent() == CellContent.EMPTY || board[i][j].getContent() == CellContent.SOKOBAN ) &&
                    !board[i][j].isGoal()) {

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

    }

/*
    Parses a varargs of cell contents into a matrix that's usable for lookups
*/
    private static CellContent[][] parseDeadlock(CellContent... content) {
        int mod = (int) Math.sqrt(content.length);
        CellContent[][] deadlock = new CellContent[mod][mod];

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

        rows.addAll(Arrays.asList(deadlock));

        CellContent[][] result = new CellContent[deadlock.length][deadlock[0].length];
        for (int i = deadlock[0].length - 1; i >= 0; i--) {
            CellContent[] row = rows.get(deadlock.length - 1 - i);
            for (int j = 0; j<row.length; j++) {
                result[j][i] = row[j];
            }
        }

        return result;
    }


    //GETTERS AND SETTERS

    public static int getPrunedNodes() {
        return prunedNodes;
    }

    public static void setPrunedNodes(int prunedNodes) {
        DeadlockDetector.prunedNodes = prunedNodes;
    }

    public static void setRoutine(DDRoutine routine) {
        DeadlockDetector.routine = routine;
        deadCells.clear();
    }

}
