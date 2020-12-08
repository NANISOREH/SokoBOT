package solver.configuration;

/*
This class encapsulates a configuration of the solver
*/
public class Configuration {

    private ExpansionScheme expansionScheme;
    private Strategy strategy;
    private Heuristic heuristic;
    private DDRoutine routine;

    private Configuration (ExpansionScheme e, Strategy s, Heuristic h, DDRoutine r) {
        this.expansionScheme = e;
        this.strategy = s;
        this.heuristic = h;
        this.routine = r;
    }

    public static Configuration getInstance(String expansionScheme, String strategy, String heuristic, String routine) {
        Configuration c = new Configuration(ExpansionScheme.mapString(expansionScheme), Strategy.mapString(strategy),
                Heuristic.mapString(heuristic), DDRoutine.mapString(routine));
        return c;
    }

    public static Configuration getInstance(ExpansionScheme expansionScheme, Strategy strategy, Heuristic heuristic, DDRoutine routine) {
        Configuration c = new Configuration(expansionScheme, strategy, heuristic, routine);
        return c;
    }

    public ExpansionScheme getExpansionScheme() {
        return expansionScheme;
    }

    public void setExpansionScheme(ExpansionScheme expansionScheme) {
        this.expansionScheme = expansionScheme;
    }

    public void setExpansionScheme(String expansionScheme) {
        this.expansionScheme = ExpansionScheme.mapString(expansionScheme);
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = Strategy.mapString(strategy);
    }

    public Heuristic getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
    }


    public DDRoutine getRoutine() {
        return routine;
    }

    public void setRoutine(DDRoutine routine) {
        this.routine = routine;
    }

}
