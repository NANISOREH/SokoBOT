package game;

import com.google.gson.Gson;

import java.io.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
This class reads, parses and holds information about the levels
*/
public class Level {
    //note that i'm hardcoding the number of levels because of a problem in reading the number of files from a runtime image
    //i might find a way to fix this later
    public static final int NUM_LEVELS = 17;
    private static Logger log = Logger.getLogger("level");
    private CellContent[][] content;
    private int bestSolution;
    private int minPushes;

/*
    Level constructor. Reads and parses the starting layout of a level and the number of moves and pushes in the best solution.
    The client can select which level to load between the ones available via an integer parameter.
*/
    public Level(int level) {
        //Reading the json file into a string
        StringBuilder jsonBuilder = new StringBuilder();
        InputStream is = Level.class.getResourceAsStream("/level" + level + ".json");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
        {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                jsonBuilder.append(replacePattern(sCurrentLine)).append("\n");
            }
        }
        catch (IOException e)
        {
            log.warning("The selected level was not found");
            e.printStackTrace();
        }
        String jsonLevel = jsonBuilder.toString();

        //Parsing the json and mapping it to the object
        Gson gson = new Gson();
        Level parsed = gson.fromJson(jsonLevel, Level.class);
        this.content = parsed.content;
        this.minPushes = parsed.minPushes;
        this.bestSolution = parsed.bestSolution;

    }

    private String replacePattern (String string) {

        Matcher matcher = Pattern.compile("\"W\"").matcher(string);
        string = matcher.replaceAll("\"WALL\"");
        matcher = Pattern.compile("\"E\"").matcher(string);
        string = matcher.replaceAll("\"EMPTY\"");
        matcher = Pattern.compile("\"S\"").matcher(string);
        string = matcher.replaceAll("\"SOKOBAN\"");
        matcher = Pattern.compile("\"G\"").matcher(string);
        string = matcher.replaceAll("\"GOAL\"");
        matcher = Pattern.compile("\"B\"").matcher(string);
        string = matcher.replaceAll("\"BOX\"");

        return string;

    }

    public CellContent[][] getContent() {
        return content;
    }

    public int getBestSolution() {
        return bestSolution;
    }

    public int getMinPushes() {
        return minPushes;
    }
}
