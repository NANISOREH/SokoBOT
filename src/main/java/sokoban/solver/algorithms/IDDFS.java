package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;
import java.util.ArrayList;
import java.util.logging.Logger;

public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution = null;
    private static int currentBest = 0;
    private static ArrayList<Action> oracle = new ArrayList<>();

    public static ArrayList<Action> launch(GameBoard game) throws CloneNotSupportedException {

        Node root = new Node(null, game, new ArrayList<Action>());
        recursiveComponent(root, 6);
        return solution;
    }

    private static void recursiveComponent(Node current, int limit) throws CloneNotSupportedException {

        current.setVisited(true);
        if (limit == 0) {
            return;
        }
        if (current.getGame().checkVictory()) {
            solution = current.getActionHistory();
            return;
        }

        else if (current.getGame().checkPartialVictory() > currentBest) {
            currentBest = current.getGame().checkPartialVictory();
            solution = current.getActionHistory();
        }

        ArrayList <Node> front = new ArrayList<>();
        front.addAll(current.expand());

        for (Node n : front) {
            if (!n.isVisited())
                recursiveComponent(n, limit -1);
        }
    }

}
