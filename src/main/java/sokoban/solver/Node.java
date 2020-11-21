package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.GameBoard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;

/*This class represents a node in the search graph.
It stores a Node's parent, the information about the game state it models and the history of actions that led to said state.*/
public class Node {
    private static Logger log = Logger.getLogger("Node");
    private Node parent;
    private GameBoard game;
    private ArrayList<Action> actionHistory = new ArrayList<>();
    private boolean visited;

    public Node(){};

    public Node(Node parent, GameBoard game, ArrayList<Action> actions) {
        this.parent = parent;
        this.game = game;
        this.actionHistory = actions;
        visited = false;
    }

/*
    Generates a new game state by executing a move and updates the action history.
    Returns true if a new state was reachable by executing the input move,
    false if the move was not legal and there was no state transition.
*/
    public boolean executeMove (Action move) throws CloneNotSupportedException {
        if (game.takeAction(move)) {
            actionHistory.add(move);
            return true;
        }
        return false;
    }

    //Node expansion method
    //It simply takes a node in input and creates a collection of 4 nodes representing a maximum of 4 states relative to the
    //4 possible moves of Sokoban. Of course, if a move is not legal it would generate the same node as the input node,
    //so it's discarded.
    public Collection<? extends Node> expand() throws CloneNotSupportedException {
        ArrayList<Node> expanded = new ArrayList<>();

        Node first = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (first.executeMove(Action.MOVE_DOWN)) {
            expanded.add(first);
        }

        Node second = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (second.executeMove(Action.MOVE_UP)) {
            expanded.add(second);
        }

        Node third = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (third.executeMove(Action.MOVE_LEFT)) {
            expanded.add(third);
        }

        Node fourth = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (fourth.executeMove(Action.MOVE_RIGHT)) {
            expanded.add(fourth);
        }

        return expanded;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public GameBoard getGame() {
        return game;
    }

    public void setGame(GameBoard game) {
        this.game = game;
    }

    public ArrayList<Action> getActionHistory() {
        return actionHistory;
    }

    public void setActionHistory(ArrayList<Action> actions) {
        this.actionHistory = actions;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
