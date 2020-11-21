package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution = null;
    private static int depthLimit = 5;

    public static ArrayList<Action> launch(GameBoard game) throws CloneNotSupportedException {

        //The node representing the initial state of the GameBoard is passed to the recursive method
        Node root = new Node(null, game, new ArrayList<Action>());
        root.setPathCost(0);
        Stack<Node> search = new Stack();
        search.push(root);

        while (!search.isEmpty()) {
            Node toCheck = search.pop();
            toCheck.setVisited(true);
            if (toCheck.getPathCost() > depthLimit) {
                continue;
            }

            if (toCheck.getGame().checkVictory() && (solution == null || solution.size() > toCheck.getActionHistory().size())) {
                log.info(toCheck.getActionHistory().size() + ": " + toCheck.getActionHistory());
                solution = new ArrayList<>((ArrayList<Action>) toCheck.getActionHistory().clone());
                continue;
            }

            ArrayList <Node> neighbours = new ArrayList<>();
            neighbours.addAll(toCheck.expand());
            for (Node n : neighbours) {
                n.setPathCost(toCheck.getPathCost() + 1);
                if (!n.isVisited()) {
                    search.push(n);
                }
            }

        }

        return solution;
    }

}
