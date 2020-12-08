package solver;

import game.Action;
import game.Cell;
import game.CellContent;
import solver.configuration.DDRoutine;

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
                if (isDeadState(node)) return true;
                else return isInDeadlockTable(node);
            }
            case LOOKUP_TABLES: {
                return isInDeadlockTable(node);
            }
            case NO_DEADLOCK_DETECTION: {
                return false;
            }
            case DEAD_POSITIONS: {
                return isDeadState(node);
            }
        }

        return false;
    }

    private static boolean isInDeadlockTable(Node node) {
        return false;
    }


    public static boolean isDeadState (Node node) throws CloneNotSupportedException {
        ArrayList<Long> transpositionTable = new ArrayList<>();

        if (node.getLastMovedBox() == null)
            return false;

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

        PriorityQueue<Node> frontier = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node extendedNode, Node t1) {
                int minimumFirst = getDistanceToNearestGoal(extendedNode);
                int minimumSecond = getDistanceToNearestGoal(t1);

                return Integer.compare(minimumFirst, minimumSecond);
            }
        });

        frontier.add(node);
        transpositionTable.add(node.hash());

        while (true) {

            Node n = frontier.remove();
            Node temp;

            if (n.getGame().getBoxCells().get(0).isGoal()) {
                return false;
            }

            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_UP);
            if (temp != null && !transpositionTable.contains(temp.hash())) {
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_DOWN);
            if (temp != null && !transpositionTable.contains(temp.hash())) {
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_LEFT);
            if (temp != null && !transpositionTable.contains(temp.hash())) {
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }
            temp = (Node) n.clone();
            temp.getGame().boxTeleport(0, Action.MOVE_RIGHT);
            if (temp != null && !transpositionTable.contains(temp.hash())){
                frontier.add(temp);
                transpositionTable.add(temp.hash());
            }


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
    }
}
