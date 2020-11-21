package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution;
    private static int depthLimit = 100;

    public static ArrayList<Action> launch(GameBoard game) throws CloneNotSupportedException {

        //resetting the solution in case this method was already called in this execution of the program
        solution = new ArrayList<>();

        //The node representing the initial state of the GameBoard is pushed into the search stack
        Node root = new Node(null, game, new ArrayList<Action>());
        Stack<Node> search = new Stack();
        search.push(root);

        //Algorithm cycle
        //It will keep popping off the stack until it's empty
        while (!search.isEmpty()) {
            Node toCheck = search.pop();
            toCheck.setVisited(true);

            //We will stop expanding the nodes if we reached the depth limit
            if (toCheck.getPathCost() > depthLimit) {
                continue;
            }

            //Breaking the cycle if we found a solution
            //It's not guaranteed to be the best one, but it should be pretty close
            //because if the lower bound estimate was done correctly, we surely didn't go too much deeper than needed
            if (toCheck.getGame().checkVictory()) {
                log.info(toCheck.getPathCost() + ": " + toCheck.getActionHistory());
                solution = new ArrayList<>((ArrayList<Action>) toCheck.getActionHistory().clone());
                break;
            }

            //Expanding the node by generating his neighbours
            //If they weren't visited already, they are pushed on top of the stack
            //Under the hood, the expand() method is also checking for duplicate hashes in a transposition table,
            //trying to discard nodes representing states that we already encountered.
            //This is necessary because of the way Sokoban works: you can find yourself in the same state despite
            //following different search branches, but duplicate states in different nodes of the search tree are not useful.
            ArrayList <Node> neighbours = new ArrayList<>();
            neighbours.addAll(toCheck.expand());
            for (Node n : neighbours) {
                if (!n.isVisited()) {
                    search.push(n);
                }
            }

        }

        if (!solution.isEmpty()) return solution;
        else return null;
    }

}
