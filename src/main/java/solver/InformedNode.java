package solver;

import game.Action;
import game.GameBoard;

import java.util.ArrayList;
import java.util.Collection;

/*
This class extends a node by adding fields that are needed for informed search algorithms
*/
public class InformedNode extends Node{
    private Node parent;
    private int label;
    private Long hash = 0L;

    //constructs a new extended node from scratch
    public InformedNode(GameBoard game, ArrayList<Action> actions, Node parent, int label) throws CloneNotSupportedException {
        super(game, actions);
        this.parent = parent;
        this.label = label;
        this.hash = this.hash();
    }

    //constructs an extended node starting from a Node
    public InformedNode(Node node, Node parent, int label) throws CloneNotSupportedException {
        super(node.getGame(), node.getActionHistory());
        this.setPathCost(node.getPathCost());
        this.parent = parent;
        this.label = label;
        this.hash = this.hash();
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

    public Long getHash () throws CloneNotSupportedException {
        if (hash != null)
            return hash;
        else
            return hash();
    }

    // Wraps node.expand() and converts the resulting nodes into expandedNodes
    @Override
    public Collection<? extends Node> expand() throws CloneNotSupportedException {
        ArrayList<Node> nodes = (ArrayList<Node>) super.expand();
        ArrayList<InformedNode> exNodes = new ArrayList<>();

        for (Node n : nodes) {
            //we assign a negative label to the new nodes so that we can recognize newborn, unlabeled nodes
            //we can't label them right now because we don't necessarily know what is the labeling criteria
            exNodes.add(new InformedNode(n, this, -1));
        }

        return exNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InformedNode that = (InformedNode) o;
        if (this.hash != null && that.hash != null) return this.hash.equals(that.hash);
        return false;
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

}
