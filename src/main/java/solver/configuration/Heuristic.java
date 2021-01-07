package solver.configuration;

public enum Heuristic {
    NAIVE_MATCHING, MINIMUM_PERFECT_MATCHING;

    public static Heuristic mapString(String toMap) {
        switch (toMap) {
            case "Minimum B-G Matching" : {
                return Heuristic.MINIMUM_PERFECT_MATCHING;
            }
            case "Naive B-G Matching" : {
                return Heuristic.NAIVE_MATCHING;
            }
        }
        return null;
    }

    public static String mapHeuristic(Heuristic toMap) {
        switch (toMap) {
            case MINIMUM_PERFECT_MATCHING : {
                return "Minimum B-G Matching";
            }
            case NAIVE_MATCHING : {
                return "Naive B-G Matching";
            }
        }
        return null;
    }
}
