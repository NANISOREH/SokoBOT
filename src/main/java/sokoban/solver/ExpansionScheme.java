package sokoban.solver;

public enum ExpansionScheme {
    PUSH_BASED, MOVE_BASED;

    public static ExpansionScheme mapString(String toMap) {
        switch (toMap) {
            case "Move-based expansion" : {
                return ExpansionScheme.MOVE_BASED;
            }
            case "Push-based expansion" : {
                return ExpansionScheme.PUSH_BASED;
            }
        }
        return null;
    }

    public static String mapExpansionScheme(ExpansionScheme toMap) {
        switch (toMap) {
            case MOVE_BASED : {
                return "Move-based expansion";
            }
            case PUSH_BASED : {
                return "Push-based expansion";
            }
        }
        return null;
    }

}
