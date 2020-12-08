package solver;

import game.Action;
import game.GameBoard;

import java.util.ArrayList;

/*
This class extends a node by adding fields that are needed for informed search algorithms
*/
public class ExtendedNode extends Node{
    private Node parent;
    private int label;

    //constructs a new extended node from scratch
    public ExtendedNode(GameBoard game, ArrayList<Action> actions, Node parent, int label) {
        super(game, actions);
        this.parent = parent;
        this.label = label;
    }

    //constructs an extended node starting from a Node
    public ExtendedNode (Node node, Node parent, int label) {
        super(node.getGame(), node.getActionHistory());
        this.setPushesNumber(node.getPushesNumber());
        this.setLastMovedBox(node.getLastMovedBox());
        this.parent = parent;
        this.label = label;
    }

    /*
    Compares nodes when inserting them into the priority queue for A*
    Nodes with the lowest label are favored, meaning that the priority queue will first extract the nodes
    with the smallest estimated distance from the goal state. In case of a tie, inertial move ordering is applied:
    we favor nodes in which Sokoban moved the same box as the last turn.
    */
    public int compare(ExtendedNode extendedNode) {
        int comparison = Integer.compare(this.label, extendedNode.label);

        if (comparison != 0)
            return comparison;
        else if (this.parent != null)
            return SokobanToolkit.compareByInertia(this, extendedNode, this.parent);

        return 0;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public boolean isGoal() {
        return this.getGame().checkVictory();
    }
}
