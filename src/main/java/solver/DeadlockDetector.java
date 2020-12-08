package solver;

import game.Action;
import game.Cell;
import game.CellContent;
import solver.configuration.DDRoutine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class DeadlockDetector {
    private static Logger log = Logger.getLogger("DeadlockDetector");
    private static DDRoutine routine = DDRoutine.ALL_ROUTINES;

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
    This method takes a node representing a state where a box was just pushed and returns true if said push brought
    the box in a dead position, false if it didn't.
    Uses a best-first search to do so.
*/
    private static boolean isDeadPosition (Node node) throws CloneNotSupportedException {
        ArrayList<Long> transpositionTable = new ArrayList<>();
        long timeElapsed;
        long start;

        if (node.getLastMovedBox() == null)
            return false;

        //Cleaning the node's game board to keep only the box that was last pushed in it
        int boxNumber = node.getLastMovedBox();
        HashMap<Integer, Cell> boxCells = node.getGame().getBoxCells();
        int target = boxCells.size();
        for (int i = 0; i<target; i++) {
            if (i != boxNumber) {
                boxCells.get(i).setContent(CellContent.EMPTY);
                boxCells.remove(i);
            }
        }
        Cell cell = boxCells.get(boxNumber);
        boxCells.clear();
        boxCells.put(0, cell);

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

        start = Instant.now().toEpochMilli();

        for (int count = 0; true; count++) {

            //we are greedily always extracting the node with the lowest distance from the closest goal
            Node n = frontier.remove();
            Node temp;

            //checking if the push that brought us to this node put the box onto a goal
            if (n.getGame().getBoxCells().get(0).isGoal()) {
                timeElapsed = (Instant.now().toEpochMilli() - start);
                //log.info("end time: " + timeElapsed + "\n" + count + " iterations\nCONFIRMED");
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
                timeElapsed = (Instant.now().toEpochMilli() - start);
                //log.info("end time: " + timeElapsed + "\n" + count + " iterations\nDISCARDED");
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
    }
}
