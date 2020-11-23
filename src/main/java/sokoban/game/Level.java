package sokoban.game;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/*
This class reads, parses and holds information about the levels
*/
public class Level {
    Logger log = Logger.getLogger("level");
    CellContent[][] content;
    int bestSolution;

/*
    Level constructor. Reads and parses the starting layout of a level and the number of moves in the best solution.
    The client can select which level to load between the ones available via an integer parameter.
*/
    public Level(int level) {
        //Reading the json file into a string
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("levels/level" + level + ".json")))
        {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                jsonBuilder.append(sCurrentLine).append("\n");
            }
        }
        catch (IOException e)
        {
            log.warning("The selected level was not found");
            e.printStackTrace();
        }
        String jsonLevel = jsonBuilder.toString();

        //Parsing the json and mapping it to a matrix of the enum CellContent
        Gson gson = new Gson();
        content = gson.fromJson(jsonLevel, CellContent[][].class);

        //Parsing the file with the best solutions for the levels and picking the right one for this level
        StringBuilder levelData = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("leveldata")))
        {
            String sCurrentLine;
            int countLine = 1;
            while ((sCurrentLine = br.readLine()) != null)
            {
                if (countLine == level) {
                    levelData.append(sCurrentLine);
                    break;
                }
                countLine++;
            }
        }
        catch (IOException e)
        {
            log.warning("The selected level was not found");
            e.printStackTrace();
        }
        String number = levelData.toString();

        if (number != null)
            bestSolution = Integer.parseInt(number);
    }

    public CellContent[][] getContent() {
        return content;
    }

    public int getBestSolution() {
        return bestSolution;
    }
}
