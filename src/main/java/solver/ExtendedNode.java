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
    private int bestChild;

    //constructs a new extended node from scratch
    public ExtendedNode(GameBoard game, ArrayList<Action> actions, Node parent, int label) {
        super(game, actions);
        this.parent = parent;
        this.label = label;
        this.bestChild = Integer.MAX_VALUE;
    }

    //constructs an extended node starting from a Node
    public ExtendedNode (Node node, Node parent, int label) {
        super(node.getGame(), node.getActionHistory());
        this.setPushesNumber(node.getPushesNumber());
        this.parent = parent;
        this.label = label;
        this.bestChild = Integer.MAX_VALUE;
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

    public int getBestChild() {
        return bestChild;
    }

    public void setBestChild(int bestChild) {
        this.bestChild = bestChild;
    }

}
