package solver;

public class DeadlockDetector {

    public static boolean isDeadState (Node node) throws CloneNotSupportedException {
/*
        ArrayList<Long> transpositionTable = new ArrayList<>();

        if (node.getLastMovedBox() == null)
            return false;

        int boxNumber = node.getLastMovedBox();
        HashMap<Integer, Cell> boxCells = node.getGame().getBoxCells();
        for (Integer key : boxCells.keySet()) {
            if (key != boxNumber)
                boxCells.remove(key);
        }

        ArrayList<Node> states = new ArrayList<>();
        states.add(node);
        while (true) {
            ArrayList<Node> expanded = new ArrayList<>();
            Node temp;

            for (Node n : states) {

                if (n.getGame().getBoxCells().get(boxNumber).isGoal())
                    return true;

                temp = tryPush ((Node) n.clone(), Action.MOVE_UP, boxNumber);
                if (temp != null && !states.contains(temp))
                    expanded.add(temp);
                temp = tryPush ((Node) n.clone(), Action.MOVE_DOWN, boxNumber);
                if (temp != null && !states.contains(temp))
                    expanded.add(temp);
                temp = tryPush ((Node) n.clone(), Action.MOVE_LEFT, boxNumber);
                if (temp != null && !states.contains(temp))
                    expanded.add(temp);
                temp = tryPush ((Node) n.clone(), Action.MOVE_RIGHT, boxNumber);
                if (temp != null && !states.contains(temp))
                    expanded.add(temp);
            }

            if (expanded.isEmpty()) return false;
            else states.addAll(expanded);
        }
*/

        return false;
    }

/*    private static Node tryPush(Node node, Action move, int boxNumber) {



    }*/
}
