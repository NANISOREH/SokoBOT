package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.CellContent;
import sokoban.game.GameBoard;
import sokoban.solver.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/*
Implementation of a simple DFS search with Iterative Deepening
*/
public class IDDFS {
    private static Logger log = Logger.getLogger("IDDFS");
    private static ArrayList<Action> solution;
    //private static ArrayList<Long> transpositionTable = new ArrayList<>();

    public static ArrayList<Action> launch(GameBoard game, int lowerBound) throws CloneNotSupportedException {

        //resetting the solution in case this method was already called in this execution of the program
        solution = new ArrayList<>();

        int limit = lowerBound;
        log.info("The lower bound estimate is: " + limit);

        //Iterative deepening cycle
        while (true) {
            //Resetting the transposition table and initializing the variables for the search.
            //The node representing the initial state of the GameBoard is pushed into the search stack
            Node.resetTranspositionTable();
            Node root = new Node(null, game, new ArrayList<>());

            //Starting the search up to the current depth limit
            recursiveComponent(root, limit);

            log.info("visited nodes at depth " + limit + ": " + Node.getExaminedNodes());

            //If we didn't find a solution after the iteration, we deepen the search
            if (!solution.isEmpty()) return solution;
            else limit = limit + lowerBound/2;
        }

    }

    private static void recursiveComponent(Node root, int limit) throws CloneNotSupportedException {
        //exit conditions first
        if (root.getGame().checkVictory()) {
            solution = new ArrayList<>(root.getActionHistory());
            return;
        }
        if (limit == 0)
            return;

        //creating the children of the current root node
        ArrayList<Node> expanded = orderMoves(root, (ArrayList<Node>) root.expand());

        //recursively calling this method on root's children, lowering by one the depth they are allowed to explore
        for (Node n : expanded) {
            recursiveComponent(n, limit - 1);
        }

    }

/*
    A simple move ordering optimization: states that involve pushing a box that was pushed by their parents too
    are considered before the others. That's useful because a lot of Sokoban proper solutions involve a certain number of consecutive
    pushes to the same box.
*/
    private static ArrayList<Node> orderMoves(Node root, ArrayList<Node> expanded) {
        Integer boxNumber = root.getLastMovedBox();
        if (boxNumber == null) {
            return expanded;
        }

        ArrayList<Node> result = new ArrayList<>();

        for (Node n : expanded) {
            if (n.getLastMovedBox() == null)
                continue;
            else if (n.getLastMovedBox().equals(boxNumber))
                result.add(n);
        }

        for (Node n : expanded) {
            if (!result.contains(n)) {
                result.add(n);
            }
        }

        return result;
    }
}



/*
package sokoban.solver.algorithms;

import sokoban.game.Action;
import sokoban.game.GameBoard;
import sokoban.solver.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Stack;
import java.util.logging.Logger;

*/
/*
Implementation of a simple DFS search with Iterative Deepening
*//*

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
*/
