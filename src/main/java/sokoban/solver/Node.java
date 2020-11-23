package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.GameBoard;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/*This class represents a node in the search graph.
It stores a Node's parent, the information about the game state it models and the history of actions that led to said state.
The class also stores a transposition table of the visited nodes as a static variable.
*/
public class Node {
    private static Logger log = Logger.getLogger("Node");
    private static ArrayList<Long> transpositionTable = new ArrayList<>();
    private Node parent;
    private GameBoard game;
    private int pathCost;
    private Integer lastMovedBox; //Key of the cell currently occupied by the last box moved, needed for move ordering
    private ArrayList<Action> actionHistory = new ArrayList<>();

    public Node(){};

    public Node(Node parent, GameBoard game, ArrayList<Action> actions) {
        this.parent = parent;
        this.game = game;
        this.actionHistory = actions;
        this.pathCost = 0;
        lastMovedBox = null;
    }

    //Node expansion method
    //It simply takes a node in input and creates a collection of nodes representing a maximum of 4 states relative to the
    //4 possible moves of Sokoban. Of course, if a move is not legal it would generate the same node as the input node,
    //so it's discarded.
    //A state is also discarded if its hashed value is already in the transposition table: that would mean that we already
    //encountered this exact configuration of the board during the search, so it wouldn't be productive to develop it again.
    public Collection<? extends Node> expand() throws CloneNotSupportedException {
        if (!transpositionTable.contains(this.hash()))
            transpositionTable.add(this.hash());

        ArrayList<Node> expanded = new ArrayList<>();

        Node first = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        first.setPathCost(this.pathCost);
        if (first.executeMove(Action.MOVE_DOWN) && !transpositionTable.contains(first.hash())) {
            expanded.add(first);
            transpositionTable.add(first.hash());
        }

        Node second = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        second.setPathCost(this.pathCost);
        if (second.executeMove(Action.MOVE_UP) && !transpositionTable.contains(second.hash())) {
            expanded.add(second);
            transpositionTable.add(second.hash());
        }

        Node third = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        third.setPathCost(this.pathCost);
        if (third.executeMove(Action.MOVE_LEFT) && !transpositionTable.contains(third.hash())) {
            expanded.add(third);
            transpositionTable.add(third.hash());
        }

        Node fourth = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        fourth.setPathCost(this.pathCost);
        if (fourth.executeMove(Action.MOVE_RIGHT)&& !transpositionTable.contains(fourth.hash())) {
            expanded.add(fourth);
            transpositionTable.add(fourth.hash());
        }

        return expanded;
    }

    /*
    Generates a new game state by executing a move and updates the action history.
    Returns true if a new state was reachable by executing the input move,
    false if the move was not legal and there was no state transition.
    It also updates the instance variable with the last moved box (if one was moved), to help with
    move ordering optimizations.
*/
    private boolean executeMove (Action move) throws CloneNotSupportedException {
        HashMap<Integer, Cell> beforeBoxCells = new HashMap<>(game.getBoxCells());
        HashMap<Integer, Cell> afterBoxCells;

        if (game.takeAction(move)) {
            actionHistory.add(move);
            pathCost++;

            //we check if a box was moved in the move and update the lastMovedBox variable
            afterBoxCells = new HashMap<>(game.getBoxCells());
            for (int i = 0; i < beforeBoxCells.size(); i++) {
                if (beforeBoxCells.get(i).getRow() != afterBoxCells.get(i).getRow() ||
                    beforeBoxCells.get(i).getColumn() != afterBoxCells.get(i).getColumn()) {
                    lastMovedBox = i;
                    return true;
                }
            }
            lastMovedBox = null;
            return true;
        }

        return false;
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

    public int getPathCost() {
        return pathCost;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    public Integer getLastMovedBox() {
        return lastMovedBox;
    }

    /*
            This method uses MD5 to produce a unique (I hope) representation of the state encapsulated by this node
        */
    public long hash() {

        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ArrayList<byte[]> bytes = new ArrayList<>();
        for (int i = 0; i<this.game.getRows(); i++){
            for (int j=0; j<this.game.getColumns(); j++) {
                try {
                    bytes.add(this.game.getBoard()[i][j].toBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] toHash = new byte[(bytes.get(0).length) * (bytes.size() + 3)];
        int count = 0;
        for (byte[] array : bytes) {
            for (int i = 0; i < array.length; i++) {
                toHash[count] = array[i];
                count++;
            }
        }

        byte[] hashed = md.digest(toHash);
        BigInteger no = new BigInteger(1, hashed);
        return no.longValue();
    }

    public static void resetTranspositionTable () {
        transpositionTable.clear();
    }

    public static long getExaminedNodes () {
        return transpositionTable.size();
    }
}
