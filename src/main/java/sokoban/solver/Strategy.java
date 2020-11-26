package sokoban.solver;

public enum Strategy {
    BFS, IDDFS, IDDFS_MO, IDASTAR;

    public static Strategy mapString(String toMap) {
        switch (toMap) {
            case ("BFS") : {
                return Strategy.BFS;
            }
            case "Iterative Deepening DFS" : {
                return Strategy.IDDFS;
            }
            case "Iterative Deepening DFS with move ordering" : {
                return Strategy.IDDFS_MO;
            }
            case "Iterative Deepening A*" : {
                return Strategy.IDASTAR;
            }
        }
        return null;
    }

    public static String mapStrategy (Strategy strategy) {
        switch (strategy) {
            case BFS : {
                return ("BFS");
            }
            case IDDFS : {
                return ("Iterative Deepening DFS");
            }
            case IDDFS_MO : {
                return ("Iterative Deepening DFS with move ordering");
            }
            case IDASTAR : {
                return ("Iterative Deepening A*");
            }
        }
        return null;
    }
}
