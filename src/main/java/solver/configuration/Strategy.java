package solver.configuration;

public enum Strategy {
    BFS, IDDFS, SMASTAR, IDASTAR;

    public static Strategy mapString(String toMap) {
        switch (toMap) {
            case ("BFS") : {
                return Strategy.BFS;
            }
            case "Iterative Deepening DFS" : {
                return Strategy.IDDFS;
            }
            case "Simplified Memory Bounded A*" : {
                return Strategy.SMASTAR;
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
            case IDASTAR : {
                return ("Iterative Deepening A*");
            }
            case SMASTAR : {
                return ("Simplified Memory Bounded A*");
            }
        }
        return null;
    }
}
