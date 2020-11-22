package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Stack;
import java.util.logging.Logger;

/*
Implementation of a simple DFS search with Iterative Deepening
*/
public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution;

    public static ArrayList<Action> launch(GameBoard game, int lowerBound) throws CloneNotSupportedException {

        //resetting the solution in case this method was already called in this execution of the program
        solution = new ArrayList<>();

        int limit = lowerBound;
        log.info("The lower bound estimate is: " + limit);

        //Iterative deepening cycle
        while (true) {
            log.info("Starting the search over: depth " + limit);

            //Resetting the transposition table and initializing the variables for the search
            //The node representing the initial state of the GameBoard is pushed into the search stack
            Node.resetTranspositionTable();
            Node root = new Node(null, game, new ArrayList<Action>());
            Deque<Node> search = new ArrayDeque<>();
            search.push(root);

            //Algorithm cycle
            //It will keep popping off the stack until it's empty
            while (!search.isEmpty()) {
                Node toCheck = search.pop();
                log.info("" + toCheck.getActionHistory());

                //Breaking the cycle if we found a solution
                if (toCheck.getGame().checkVictory()) {
                    solution = new ArrayList<>((ArrayList<Action>) toCheck.getActionHistory());
                    break;
                }

                //Expanding the node by generating his neighbours
                //If they weren't visited already, they are pushed on top of the stack
                //Under the hood, the expand() method is checking for duplicate hashes in a transposition table,
                //trying to discard nodes representing states that we already encountered.
                //This is necessary because of the way Sokoban works: you can find yourself in the same state despite
                //following different search branches, but duplicate states in different nodes of the search tree are not useful.
                for (Node n : toCheck.expand()) {
                    if (toCheck.getActionHistory().size() < limit) {
                        search.push(n);
                    }
                }

            }

            //If we didn't find a solution after the iteration, we deepen the search
            if (!solution.isEmpty()) return solution;
            else limit += lowerBound/2;
        }

    }
}
