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
    private Long hash = 0L;

    //constructs a new extended node from scratch
    public ExtendedNode(GameBoard game, ArrayList<Action> actions, Node parent, int label) throws CloneNotSupportedException {
        super(game, actions);
        this.parent = parent;
        this.label = label;
        this.bestChild = Integer.MAX_VALUE;
        if (!Node.isTranspositionManaged()) this.hash = this.hash();
        else this.hash = null;
    }

    //constructs an extended node starting from a Node
    public ExtendedNode (Node node, Node parent, int label) throws CloneNotSupportedException {
        super(node.getGame(), node.getActionHistory());
        this.setPushesNumber(node.getPushesNumber());
        this.parent = parent;
        this.label = label;
        this.bestChild = Integer.MAX_VALUE;
        if (!Node.isTranspositionManaged()) this.hash = this.hash();
        else this.hash = null;
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

    public Long getHash () throws CloneNotSupportedException {
        if (hash != null)
            return hash;
        else
            return hash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtendedNode that = (ExtendedNode) o;
        if (this.hash != null && that.hash != null) return this.hash.equals(that.hash);
        return false;
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

}
