package solver.configuration;

public enum DDRoutine {
    ALL_ROUTINES,
    LOOKUP_TABLES,
    DEAD_POSITIONS,
    FROZEN_BOXES,
    NO_DEADLOCK_DETECTION;

    public static DDRoutine mapString(String toMap) {
        switch (toMap) {
            case "All available routines" : {
                return DDRoutine.ALL_ROUTINES;
            }
            case "Check for dead positions" : {
                return DDRoutine.DEAD_POSITIONS;
            }
            case "Check for frozen boxes" : {
                return DDRoutine.FROZEN_BOXES;
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
            case FROZEN_BOXES : {
                return "Check for frozen boxes";
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
