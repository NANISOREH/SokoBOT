package solver;

import game.Action;
import game.GameBoard;

import java.util.ArrayList;
import java.util.Collection;

/*
This class extends a node by adding fields that are needed for informed search algorithms
*/
public class ExtendedNode extends Node{
    private Node parent;
    private int label;
    private Long hash = 0L;

    //constructs a new extended node from scratch
    public ExtendedNode(GameBoard game, ArrayList<Action> actions, Node parent, int label) throws CloneNotSupportedException {
        super(game, actions);
        this.parent = parent;
        this.label = label;
        if (!Node.isTranspositionManaged()) this.hash = this.hash();
        else this.hash = null;
    }

    //constructs an extended node starting from a Node
    public ExtendedNode (Node node, Node parent, int label) throws CloneNotSupportedException {
        super(node.getGame(), node.getActionHistory());
        this.setPushesNumber(node.getPushesNumber());
        this.parent = parent;
        this.label = label;
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
        ArrayList<ExtendedNode> exNodes = new ArrayList<>();

        for (Node n : nodes) {
            exNodes.add(new ExtendedNode(n, this, 0));
        }

        return exNodes;
    }

    //Compare method to give a PQueue ordering criteria for greedy best-first search
    public static int gbfsCompare(ExtendedNode extendedNode, ExtendedNode t1) {
        int comparison = Integer.compare(extendedNode.getLabel(), t1.getLabel());

            //tie breaker: inertia
            if (comparison == 0 && extendedNode.getParent() != null && t1.getParent() != null)
                comparison = SokobanToolkit.compareByInertia(extendedNode, t1, extendedNode.getParent(), t1.getParent());

            return comparison;
    }

    //Compare method to give a PQueue ordering criteria for A* search
    public static int astarCompare(ExtendedNode extendedNode, ExtendedNode t1) {
        //main criteria for insertion into the pqueue
        //it will favor the lowest f(n) label value among the two nodes
        int comparison = Integer.compare(extendedNode.getLabel(), t1.getLabel());

        //tie breaker: inertia
        if (comparison == 0 && extendedNode.getParent() != null && t1.getParent() != null)
            comparison = SokobanToolkit.compareByInertia(extendedNode, t1, extendedNode.getParent(), t1.getParent());

        //tie breaker: heuristics without the path cost
        if (comparison == 0)
            comparison = Integer.compare(extendedNode.getLabel() - extendedNode.getPathCost(),
                    t1.getLabel() - t1.getPathCost());

        return comparison;
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
