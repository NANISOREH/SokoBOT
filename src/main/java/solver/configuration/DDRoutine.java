package solver.configuration;

public enum DDRoutine {
    DEAD_POSITIONS, LOOKUP_TABLES, NO_DEADLOCK_DETECTION, ALL_ROUTINES;

    public static DDRoutine mapString(String toMap) {
        switch (toMap) {
            case "Check for dead positions" : {
                return DDRoutine.DEAD_POSITIONS;
            }
            case "Check in precomputed deadlock table" : {
                return DDRoutine.LOOKUP_TABLES;
            }
            case "All available routines" : {
                return DDRoutine.ALL_ROUTINES;
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
                return "All available routines";
            }
            case ALL_ROUTINES : {
                return "No deadlock detection";
            }
        }
        return null;
    }
}
