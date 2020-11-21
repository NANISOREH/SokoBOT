package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;
import java.util.ArrayList;
import java.util.logging.Logger;

public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution = null;
    private static ArrayList<Action> partialSolution = null;
    private static int currentBest = 0;
    private static final int depthLimit = 5;
    private static ArrayList<Action> oracle = new ArrayList<>();

    public static ArrayList<Action> launch(GameBoard game) throws CloneNotSupportedException {

        //The node representing the initial state of the GameBoard is passed to the recursive method
        Node root = new Node(null, game, new ArrayList<Action>());
        recursiveComponent(root, depthLimit);

        return solution;
    }

    private static void recursiveComponent(Node current, int limit) throws CloneNotSupportedException {
        current.setVisited(true);

        if (current.getGame().checkVictory()) { // We found a solution, stop the recursion!
            if (solution!= null && current.getActionHistory().size() < solution.size()) {
                log.info("" + current.getActionHistory());
                solution = current.getActionHistory();
            }
            return;
        }
        if (limit == 0) { // We reached the depth limit, stop the recursion!
            return;
        }

        else if (current.getGame().checkPartialVictory() > currentBest) {
            // We didn't find a solution but the current sequence of actions quite literally checks some boxes,
            // so we keep track of it
            currentBest = current.getGame().checkPartialVictory();
            partialSolution = current.getActionHistory();
        }

        ArrayList <Node> front = new ArrayList<>();
        front.addAll(current.expand());

        for (Node n : front) {
            if (!n.isVisited())
                recursiveComponent(n, limit -1);
        }
    }

}
