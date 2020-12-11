package solver.configuration;

public enum DDRoutine {
    ALL_ROUTINES,
    LOOKUP_TABLES,
    DEAD_POSITIONS,
    NO_DEADLOCK_DETECTION;

    public static DDRoutine mapString(String toMap) {
        switch (toMap) {
            case "All available routines" : {
                return DDRoutine.ALL_ROUTINES;
            }
            case "Check for dead positions" : {
                return DDRoutine.DEAD_POSITIONS;
            }
            case "Check in precomputed deadlock table" : {
                return DDRoutine.LOOKUP_TABLES;
            }
            case "No deadlock detection" : {
                return DDRoutine.NO_DEADLOCK_DETECTION;
            }
        }
        return null;
    }

    public static String mapDDRoutine(DDRoutine toMap) {
        switch (toMap) {
            case DEAD_POSITIONS : {
                return "Check for dead positions";
            }
            case LOOKUP_TABLES : {
                return "Check in precomputed deadlock table";
            }
            case NO_DEADLOCK_DETECTION : {
                return "No deadlock detection";
            }
            case ALL_ROUTINES : {
                return "All available routines";
            }
        }
        return null;
    }
}
