package solver;

import game.Action;
import game.Cell;
import game.CellContent;
import game.GameBoard;
import solver.configuration.ExpansionScheme;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/*This class represents a node in the search graph.
It stores the information about the game state it models and the history of actions that led to said state.
It also implements node expansion algorithms.
*/
public class Node {
    private static Logger log = Logger.getLogger("Node");
    //maximum depth reached by the search at a certain point in time
    protected static int depth;
    protected static ExpansionScheme expansionScheme;
    protected GameBoard game;
    protected int pathCost;
    protected ArrayList<Action> actionHistory = new ArrayList<>();

    public Node(){};

    public Node(GameBoard game, ArrayList<Action> actions) {
        this.game = game;
        this.actionHistory = actions;
        this.pathCost = 0;
    }

/*
    Public node expansion method.
    It calls the concrete implementation of the expansion schemes, as decided by the client.
    This way, it doesn't have to be hardcoded into the algorithms and it can be switched at runtime.
*/
    public Collection<? extends Node> expand() throws CloneNotSupportedException {
        ArrayList<Node> expanded;
        if (expansionScheme == ExpansionScheme.MOVE_BASED)
            expanded = (ArrayList<Node>) this.expandByMoves();
        else
            expanded = (ArrayList<Node>) this.expandByPushes();

        //incrementing the path cost of the new nodes
        for (Node n : expanded) {
            n.pathCost = this.pathCost + 1;
        }

        if (!expanded.isEmpty() && expanded.get(0).pathCost > depth)
            depth = expanded.get(0).pathCost;

        return expanded;
    }

/*
    Node expansion method, expands a node by box pushes rather than by character moves.
    It takes a node, representing a certain game state, and it returns the collection of all the neighbour nodes,
    representing all possible future states that are just a box push away from being discovered.
    Box configurations that were already explored are excluded thanks to the transposition table.
*/
    protected Collection<? extends Node> expandByPushes() throws CloneNotSupportedException {

        ArrayList<Node> expanded = new ArrayList<>();
        GameBoard initialState = (GameBoard) this.game.clone();
        HashMap<Integer, Cell> boxes = initialState.getBoxCells();
        Cell neighbour;
        Cell oppositeNeighbour;

        //examining all current positions of the boxes on the board
        for (Integer boxKey : boxes.keySet()) {

            neighbour = initialState.getNorth(boxes.get(boxKey));
            oppositeNeighbour = initialState.getSouth(boxes.get(boxKey));
            Node down = new Node((GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            //checking if two opposite cells adjacent to the box are either empty or contain sokoban: that's the only way we can push the box
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                //checking if the neighbour of the selected box is reachable by sokoban
                //if that's true, the actions involved in reaching the box and pushing it can be carried out
                //after that, we let the deadlock detector do its job
                if (push (down, neighbour, Action.MOVE_DOWN)) {
                    expanded.add(down);

                }
            }

            //Same as before with other directions.
            //Generalizing this stuff to avoid repeated code is possible but not worth the time investment.

            neighbour = initialState.getSouth(boxes.get(boxKey));
            oppositeNeighbour = initialState.getNorth(boxes.get(boxKey));
            Node up = new Node((GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                if (push (up, neighbour, Action.MOVE_UP)) {
                    expanded.add(up);
                }
            }

            neighbour = initialState.getEast(boxes.get(boxKey));
            oppositeNeighbour = initialState.getWest(boxes.get(boxKey));
            Node left = new Node((GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                if (push (left, neighbour, Action.MOVE_LEFT)) {
                    expanded.add(left);
                }

            }

            neighbour = initialState.getWest(boxes.get(boxKey));
            oppositeNeighbour = initialState.getEast(boxes.get(boxKey));
            Node right = new Node((GameBoard) initialState.clone(), (ArrayList<Action>) this.getActionHistory().clone());
            if ((neighbour.getContent() == CellContent.EMPTY || neighbour.getContent() == CellContent.SOKOBAN) &&
                    (oppositeNeighbour.getContent() == CellContent.EMPTY || oppositeNeighbour.getContent() == CellContent.SOKOBAN)) {

                if (push (right, neighbour, Action.MOVE_RIGHT)) {
                    expanded.add(right);
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

        path = SokobanToolkit.searchPath((GameBoard) newState.getGame().clone(), (Cell) neighbour.clone());
        if (path != null) {
            //reaching a cell adjacent to the box
            for (Action a : path) {
                executeMove(newState, a);
            }
            //moving the box
            if (!executeMove(newState, action))
                return false;

            if (newState.game.getLastMovedBox() != null)
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

        Node first = new Node((GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        //Checkin if the move is legal and we execute it, then we check if the generated state was already discovered
        if (executeMove(first, Action.MOVE_DOWN)) {
            expanded.add(first);
            //If we reached a new maximum depth in the search, we keep note of it in depth. a static variable of Node
            if (first.getActionHistory().size() > depth)
                depth = first.getActionHistory().size();
        }

        //Same as before with other directions.
        //Generalizing this stuff to avoid repeated code is possible but not worth the time investment.

        Node second = new Node((GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (executeMove(second, Action.MOVE_UP)) {
            expanded.add(second);
            if (second.getActionHistory().size() > depth)
                depth = second.getActionHistory().size();
        }

        Node third = new Node((GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (executeMove(third, Action.MOVE_LEFT)) {

            expanded.add(third);
            if (third.getActionHistory().size() > depth)
                depth = third.getActionHistory().size();
        }

        Node fourth = new Node((GameBoard) this.getGame().clone(), (ArrayList<Action>) this.getActionHistory().clone());
        if (executeMove(fourth, Action.MOVE_RIGHT)) {

            expanded.add(fourth);
            if (fourth.getActionHistory().size() > depth)
                depth = fourth.getActionHistory().size();
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

            //we check if a box was moved in this move and update the lastMovedBox variable and the number of pushes
            afterBoxCells = new HashMap<>(node.game.getBoxCells());
            for (int i = 0; i < beforeBoxCells.size(); i++) {
                if (beforeBoxCells.get(i).getRow() != afterBoxCells.get(i).getRow() ||
                        beforeBoxCells.get(i).getColumn() != afterBoxCells.get(i).getColumn()) {
                    return true;
                }
            }
            return true;
        }
        else
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

        if (expansionScheme == ExpansionScheme.PUSH_BASED) {
            cloned.getBoard()[cloned.getSokobanCell().getRow()][cloned.getSokobanCell().getColumn()].setContent(CellContent.EMPTY);
        }

        //parsing each cell of the game board in a byte array
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

        //every byte array in the arraylist is merged into just one byte array that will be hashed
        byte[] toHash = new byte[(bytes.get(0).length) * (bytes.size()) + 5000];
        int count = 0;
        for (byte[] array : bytes) {
            for (int i = 0; i < array.length; i++) {
                toHash[count] = array[i];
                count++;
            }
        }

        //if we're working with push based expansion scheme things are a little more complex
        //we need to save every position reachable by sokoban in the state transposition
        //because two states are equal only if the boxes are in the same places and sokoban can reach the same cells
        byte[] reachableCells;
        if (expansionScheme == ExpansionScheme.PUSH_BASED) {
            bytes.clear();

            try {
                reachableCells = SokobanToolkit.getReachableCells((GameBoard) this.getGame().clone());
                bytes.add(reachableCells);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (byte[] array : bytes) {
                for (int i = 0; i < array.length; i++) {
                    toHash[count] = array[i];
                    count++;
                }
            }

        }

        byte[] hashed = md.digest(toHash);
        BigInteger no = new BigInteger(1, hashed);
        return no.longValue();
    }

/*
    Simply checks if this node is a solution
*/
    public boolean isGoal() {
        return this.getGame().checkVictory();
    }

/*
    Checks if the node is a solution and updates a solution variable if the new solution is an improvement
*/
    public boolean isGoal(Node solution) {
        if (this.isGoal() && solution.isGoal()) {
            if (solution == null) {
                solution = this;
            }
            else if (solution != null && this.getPathCost() < solution.getPathCost()) {
                solution = this;
            }
            else if (solution != null && this.getPathCost() == solution.getPathCost()) {
                if (this.getActionHistory().size() < solution.getActionHistory().size())
                    solution = this;
            }
            return true;
        }

        return false;
    }

    //GETTERS AND SETTERS

    public int getPathCost() {
        return pathCost;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
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

    public static ExpansionScheme getExpansionScheme() {
        return expansionScheme;
    }

    public static void setExpansionScheme(ExpansionScheme expansionScheme) {
        Node.expansionScheme = expansionScheme;
    }

    public static int getDepth() {
        return depth;
    }

    public static void setDepth(int depth) {
        Node.depth = depth;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Node cloned = new Node();

        cloned.game = (GameBoard) this.game.clone();
        cloned.pathCost = this.pathCost;
        cloned.actionHistory = (ArrayList<Action>) this.actionHistory.clone();

        return cloned;
    }
}
