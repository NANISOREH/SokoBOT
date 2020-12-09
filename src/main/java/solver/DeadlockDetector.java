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
        return false;
    }

/*
    Confronts the cell containing the last moved box (if there's one) with the list of the dead cells.
    In other words, checks if we just pushed a box into a dead position.
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
        This method launches the search for dead positions before the search for solution starts.
        It empties the level and tries to put a box in any position, then checks if said position is dead
        and adds dead positions to a list that will be consulted whenever we push a box during the solution searching.
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

    public static DDRoutine getRoutine() {
        return routine;
    }

    public static void setRoutine(DDRoutine routine) {
        DeadlockDetector.routine = routine;
        deadCells.clear();
    }
}
