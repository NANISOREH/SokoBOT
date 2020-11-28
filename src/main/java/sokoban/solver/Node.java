package sokoban.solver;

import sokoban.game.Action;
import sokoban.game.Cell;
import sokoban.game.CellContent;
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
    private static int depth;
    private static ExpansionScheme expansionScheme = ExpansionScheme.MOVE_BASED;
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

    public Collection<? extends Node> test () throws CloneNotSupportedException {
        return this.expandByPushes();
    }

/*
    Public node expansion method.
    It calls the concrete implementation of the expansion schemes, as decided by the client.
    This way, it doesn't have to be hardcoded into the algorithms and it can be switched at runtime.
*/
    public Collection<? extends Node> expand() throws CloneNotSupportedException {
        if (!transpositionTable.contains(this.hash()))
            transpositionTable.add(this.hash());

        if (this.getActionHistory().size() > depth)
            depth = this.getActionHistory().size();

        if (expansionScheme == ExpansionScheme.MOVE_BASED)
            return this.expandByMoves();
        else
            return this.expandByPushes();
    }

    private Collection<? extends Node> expandByPushes() throws CloneNotSupportedException {
        ArrayList<Node> expanded = new ArrayList<>();
        HashMap<Integer, Cell> boxes = this.getGame().getBoxCells();
        Cell neighbour;
        Cell oppositeNeighbour;
        Node newState;

        //examining all current positions of the boxes on the board
        for (Integer boxKey : boxes.keySet()) {

            neighbour = this.getGame().getNorth(boxes.get(boxKey));
            oppositeNeighbour = this.getGame().getSouth(boxes.get(boxKey));
            //checking if the cell adjacent to the box is empty or contains sokoban: that's the only way we can push the box
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {


                newState = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
                //checking if the neighbour of the selected box is reachable by sokoban
                //if that's true, the actions involved in reaching the box and pushing it can be carried out
                if (push (newState, neighbour, Action.MOVE_DOWN) && !transpositionTable.contains(newState)) {
                    expanded.add(newState);
                    transpositionTable.add(newState.hash());
                }
            }

            neighbour = this.getGame().getSouth(boxes.get(boxKey));
            oppositeNeighbour = this.getGame().getNorth(boxes.get(boxKey));
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                newState = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
                if (push (newState, neighbour, Action.MOVE_UP) && !transpositionTable.contains(newState)) {
                    expanded.add(newState);
                    transpositionTable.add(newState.hash());
                }
            }

            neighbour = this.getGame().getEast(boxes.get(boxKey));
            oppositeNeighbour = this.getGame().getWest(boxes.get(boxKey));
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                newState = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
                if (push (newState, neighbour, Action.MOVE_LEFT) && !transpositionTable.contains(newState)) {
                    expanded.add(newState);
                    transpositionTable.add(newState.hash());
                }

            }

            neighbour = this.getGame().getWest(boxes.get(boxKey));
            oppositeNeighbour = this.getGame().getEast(boxes.get(boxKey));
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                newState = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
                if (push (newState, neighbour, Action.MOVE_RIGHT) && !transpositionTable.contains(newState)) {
                    expanded.add(newState);
                    transpositionTable.add(newState.hash());
                }

            }

        }

        return expanded;
    }

/*
    Private helper method that checks if in the given node Sokoban can move to a certain cell adjacent to a box, then
    carries out the action of pushing said box if there's a way to do so.
    Returns true if it succesfully pushed the box, false if there was no way to reach the given cell and push the box.
*/
    private boolean push (Node newState, Cell neighbour, Action action) throws CloneNotSupportedException {
        ArrayList<Action> path = null;

        path = SokobanToolkit.searchPath((GameBoard) newState.getGame().clone(), neighbour);
        if (path != null) {
            //reaching a cell adjacent to the box
            for (Action a : path) {
                newState.executeMove(a);
            }
            //moving the box
            newState.executeMove(action);
            return true;
        }

        return false;
    }

    //Node expansion, with expansion by moving the character
    //It simply takes a node in input and creates a collection of nodes representing a maximum of 4 states relative to the
    //4 possible moves of Sokoban. Of course, if a move is not legal it would generate the same node as the input node,
    //so it's discarded.
    //A state is also discarded if its hashed value is already in the transposition table: that would mean that we already
    //encountered this exact configuration of the board during the search, so it wouldn't be productive to develop it again.
    private Collection<? extends Node> expandByMoves() throws CloneNotSupportedException {
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
    Generates a new game configuration by executing a move and updates the action history.
    Returns true if a new configuration was reachable by executing the input move,
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

            //we check if a box was moved in this turn and update the lastMovedBox variable
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

        byte[] toHash = new byte[(bytes.get(0).length) * (bytes.size() + 3)]; //TODO: check why it goes out of bounds if you don't add 3 (?!)
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

    public static void resetSearchSpace () {
        transpositionTable.clear();
        depth = 0;
    }


    //GETTERS AND SETTERS

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

    public static ArrayList<Long> getTranspositionTable() {
        return transpositionTable;
    }

    public static void setTranspositionTable(ArrayList<Long> transpositionTable) {
        Node.transpositionTable = transpositionTable;
    }

    public static ExpansionScheme getExpansionScheme() {
        return expansionScheme;
    }

    public static void setExpansionScheme(ExpansionScheme expansionScheme) {
        Node.expansionScheme = expansionScheme;
    }

    public static long getExaminedNodes () {
        return transpositionTable.size();
    }

    public static int getDepth() {
        return depth;
    }
}
