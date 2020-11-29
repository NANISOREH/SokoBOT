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
    private static ExpansionScheme expansionScheme;
    private Node parent;
    private GameBoard game;
    private int pathCost;
    private int pushesNumber;
    private Integer lastMovedBox; //Key of the cell currently occupied by the last box moved, needed for move ordering
    private ArrayList<Action> actionHistory = new ArrayList<>();

    public Node(){};

    public Node(Node parent, GameBoard game, ArrayList<Action> actions) {
        this.parent = parent;
        this.game = game;
        this.actionHistory = actions;
        this.pathCost = 0;
        this.pushesNumber = 0;
        lastMovedBox = null;
    }

/*
    Public node expansion method.
    It calls the concrete implementation of the expansion schemes, as decided by the client.
    This way, it doesn't have to be hardcoded into the algorithms and it can be switched at runtime.
*/
    public Collection<? extends Node> expand() throws CloneNotSupportedException {
        if (!transpositionTable.contains(this.hash()))
            transpositionTable.add(this.hash());

        if (expansionScheme == ExpansionScheme.MOVE_BASED)
            return this.expandByMoves();
        else
            return this.expandByPushes();
    }

/*
    Node expansion method, expands a node by box pushes rather than by character moves.
    It takes a node, representing a certain game state, and it returns the collection of all the neighbour nodes,
    representing all possible future states that are just a box push away from being discovered.
    Box configurations that were already explored are excluded thanks to the transposition table.
*/
    private Collection<? extends Node> expandByPushes() throws CloneNotSupportedException {

        ArrayList<Node> expanded = new ArrayList<>();
        GameBoard initialState = (GameBoard) this.game.clone();
        HashMap<Integer, Cell> boxes = initialState.getBoxCells();
        Cell neighbour;
        Cell oppositeNeighbour;

        //examining all current positions of the boxes on the board
        for (Integer boxKey : boxes.keySet()) {

            neighbour = initialState.getNorth(boxes.get(boxKey));
            oppositeNeighbour = initialState.getSouth(boxes.get(boxKey));
            Node down = new Node(this, (GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            //checking if two opposite cells adjacent to the box are empty or contain sokoban: that's the only way we can push the box
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                //checking if the neighbour of the selected box is reachable by sokoban
                //if that's true, the actions involved in reaching the box and pushing it can be carried out
                if (push (down, neighbour, Action.MOVE_DOWN)) {
                    if (!transpositionTable.contains(down.hash())) {
                        transpositionTable.add(down.hash());
                        expanded.add(down);
                    }
                }
            }

            neighbour = initialState.getSouth(boxes.get(boxKey));
            oppositeNeighbour = initialState.getNorth(boxes.get(boxKey));
            Node up = new Node(this, (GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                if (push (up, neighbour, Action.MOVE_UP)) {
                    if (!transpositionTable.contains(up.hash())) {
                        transpositionTable.add(up.hash());
                        expanded.add(up);
                    }
                }
            }

            neighbour = initialState.getEast(boxes.get(boxKey));
            oppositeNeighbour = initialState.getWest(boxes.get(boxKey));
            Node left = new Node(this, (GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                if (push (left, neighbour, Action.MOVE_LEFT)) {
                    if (!transpositionTable.contains(left.hash())) {
                        transpositionTable.add(left.hash());
                        expanded.add(left);
                    }
                }

            }

            neighbour = initialState.getWest(boxes.get(boxKey));
            oppositeNeighbour = initialState.getEast(boxes.get(boxKey));
            Node right = new Node(this, (GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                if (push (right, neighbour, Action.MOVE_RIGHT)) {
                    if (!transpositionTable.contains(right.hash())) {
                        transpositionTable.add(right.hash());
                        expanded.add(right);
                    }
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

        path = SokobanToolkit.searchPath(newState.getGame(), (Cell) neighbour.clone());
        if (path != null) {
            //reaching a cell adjacent to the box
            for (Action a : path) {
                executeMove(newState, a);
            }
            //moving the box
            executeMove(newState, action);

            if (newState.getPushesNumber() > depth)
                depth = newState.pushesNumber;

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
        first.setPushesNumber(this.pushesNumber);
        first.setPathCost(this.pathCost);
        //Checkin if the move is legal and we execute it, then we check if the generated state was already discovered
        if (executeMove(first, Action.MOVE_DOWN) && !transpositionTable.contains(first.hash())) {
            expanded.add(first);
            //If we reached a new maximum depth in the search, we keep note of it in depth. a static variable of Node
            if (first.getActionHistory().size() > depth)
                depth = first.getActionHistory().size();
            transpositionTable.add(first.hash());
        }

        Node second = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        second.setPathCost(this.pathCost);
        second.setPushesNumber(this.pushesNumber);
        if (executeMove(second, Action.MOVE_UP) && !transpositionTable.contains(second.hash())) {
            expanded.add(second);
            if (second.getActionHistory().size() > depth)
                depth = second.getActionHistory().size();
            transpositionTable.add(second.hash());
        }

        Node third = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        third.setPathCost(this.pathCost);
        third.setPushesNumber(this.pushesNumber);
        if (executeMove(third, Action.MOVE_LEFT) && !transpositionTable.contains(third.hash())) {
            expanded.add(third);
            if (third.getActionHistory().size() > depth)
                depth = third.getActionHistory().size();
            transpositionTable.add(third.hash());
        }

        Node fourth = new Node(this, (GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        fourth.setPathCost(this.pathCost);
        fourth.setPushesNumber(this.pushesNumber);
        if (executeMove(fourth, Action.MOVE_RIGHT)&& !transpositionTable.contains(fourth.hash())) {
            expanded.add(fourth);
            if (fourth.getActionHistory().size() > depth)
                depth = fourth.getActionHistory().size();
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
    private boolean executeMove (Node node, Action move) throws CloneNotSupportedException {
        HashMap<Integer, Cell> beforeBoxCells = new HashMap<>(game.getBoxCells());
        HashMap<Integer, Cell> afterBoxCells;

        if (node.game.takeAction(move)) {
            node.actionHistory.add(move);
            node.pathCost++;

            //we check if a box was moved in this move and update the lastMovedBox variable and the number of pushes
            afterBoxCells = new HashMap<>(node.game.getBoxCells());
            for (int i = 0; i < beforeBoxCells.size(); i++) {
                if (beforeBoxCells.get(i).getRow() != afterBoxCells.get(i).getRow() ||
                    beforeBoxCells.get(i).getColumn() != afterBoxCells.get(i).getColumn()) {
                    node.lastMovedBox = i;
                    node.pushesNumber = this.pushesNumber + 1;
                    return true;
                }
            }
            node.lastMovedBox = null;
            return true;
        }

        return false;
    }

    /*
        This method uses MD5 to produce a unique (I hope) representation of the state encapsulated by this node
*/
    public long hash() throws CloneNotSupportedException {

        MessageDigest md = null;
        GameBoard cloned = (GameBoard) this.getGame().clone();

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

/*        if (expansionScheme == ExpansionScheme.PUSH_BASED) {
            cloned.getBoard()[cloned.getSokobanCell().getRow()][cloned.getSokobanCell().getColumn()].setContent(CellContent.EMPTY);
        }*/

        ArrayList<byte[]> bytes = new ArrayList<>();
        for (int i = 0; i<cloned.getRows(); i++){
            for (int j=0; j<cloned.getColumns(); j++) {
                try {
                    bytes.add(cloned.getBoard()[i][j].toBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] toHash = new byte[(bytes.get(0).length) * (bytes.size() + 10)]; //TODO: check why it goes out of bounds if you don't add stuff (?!)
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
        return actionHistory.size();
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

    public int getPushesNumber() {
        return pushesNumber;
    }

    public void setPushesNumber(int pushesNumber) {
        this.pushesNumber = pushesNumber;
    }
}
