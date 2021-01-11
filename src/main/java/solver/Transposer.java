package solver;

import solver.configuration.Strategy;

import java.util.TreeMap;
import java.util.logging.Logger;

/*
This class is in charge of managing state transpositions
*/
public class Transposer {
    private static final Logger log = Logger.getLogger("Transposer");
    //transposition table, using the hashed state as key and the depth at which a state was encountered as value
    private static TreeMap<Long, Integer> transpositionTable = new TreeMap();

    //accounting table, using the hashed state as key and the node label as value
    //used by informed search algorithms to check if a node was already met with a higher label
    private static TreeMap<Long, Integer> accountingTable = new TreeMap();

/*
    This method transposes a node and inserts it into the transposition table.
    It returns true if the node was transposed, false if an equivalent transposition was already present.
    Note that we update the transposition if the node was already found at a deeper level, and we return true
    anyway. This allows dfs-like algorithms to function properly, without cutting proper paths after backtracking.

    Sidenote: I've crammed an isPresent(node) type of method and a transpose(node) type of method into the same one,
    because the transposition table is ALWAYS used in a "if not present then add" fashion anyway...
    this way, you'll often find something like:
    if (Transposer.transpose(node))
        expanded = node.expand()
*/
    public static boolean transpose (Node n) throws CloneNotSupportedException {
        Long nodeHash = n.hash();
        if (!transpositionTable.containsKey(nodeHash)) {
            transpositionTable.put(nodeHash, n.getPathCost());
            return true;
        }

        int oldDepth = transpositionTable.get(nodeHash);
        if ((SokobanSolver.getConfiguration().getStrategy().equals(Strategy.IDDFS) ||
            (SokobanSolver.getConfiguration().getStrategy().equals(Strategy.IDASTAR)))
            && oldDepth > n.getPathCost()
        ) {
            transpositionTable.put(nodeHash, n.getPathCost());
            return true;
        }
        else
            return false;
    }

/*
    This method inserts a node into the accounting table
    It returns true if the node was accounted, false if the node was already present with a better or equal label.
*/
    public static boolean saveLabel(InformedNode n) throws CloneNotSupportedException {

        if (!accountingTable.containsKey(n.getHash())) {
            accountingTable.put(n.getHash(), n.getLabel());
            return true;
        }

        int oldLabel = accountingTable.get(n.getHash());
        if (oldLabel > n.getLabel()) {
            accountingTable.put(n.getHash(), n.getLabel());
            return true;
        }
        else
            return false;
    }

/*
    Removes a node from the accounting table
*/
    public static void removeLabel(InformedNode n) throws CloneNotSupportedException {
        accountingTable.remove(n.getHash());
    }

/*
    Checks if a node represents an already known state but improves on the label
*/
    public static boolean hasBetterLabel (InformedNode n) throws CloneNotSupportedException {
        int oldLabel;

        if (!accountingTable.containsKey(n.getHash())) return false;
        else oldLabel = accountingTable.get(n.getHash());

        return n.getLabel() < oldLabel;
    }

    public static long getExaminedNodes() {
        return transpositionTable.size();
    }

    public static void resetSearchSpace() {
        transpositionTable.clear();
        accountingTable.clear();
    }
}
