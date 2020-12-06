package sokoban.solver.configuration;

/*
This class encapsulates a configuration of the solver
*/
public class Configuration {

    private ExpansionScheme expansionScheme;
    private Strategy strategy;

    private Configuration (ExpansionScheme e, Strategy s) {
        this.expansionScheme = e;
        this.strategy = s;
    }

    public static Configuration getInstance(String expansionScheme, String strategy) {
        Configuration c = new Configuration(ExpansionScheme.mapString(expansionScheme), Strategy.mapString(strategy));
        return c;
    }

    public static Configuration getInstance(ExpansionScheme expansionScheme, Strategy strategy) {
        Configuration c = new Configuration(expansionScheme, strategy);
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

}
