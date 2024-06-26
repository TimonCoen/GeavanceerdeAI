import java.io.*;
import java.util.*;

public class InputReader {
    private ArrayList<ArrayList<Integer>> dist;
    private ArrayList<ArrayList<Integer>> opponents;
    private int nTeams;

    public InputReader(String fileName) {
        dist = new ArrayList<>();
        opponents = new ArrayList<>();
        nTeams = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean readingDist = false;
            boolean readingOpponents = false;

            while ((line = br.readLine()) != null) {
                line = line.trim(); // Trim whitespace

                // Check for the start of the matrices
                if(line.contains("nTeams=")){
                    String[] numbers = line.substring(0, line.length()-1).split("=");
                    nTeams = Integer.parseInt(numbers[1]);
                }
                if (line.equals("dist= [")) {
                    readingDist = true;
                } else if (line.equals("opponents=[")) {
                    readingDist = false; // Stop reading dist
                    readingOpponents = true;
                } else if (line.startsWith("[")) {
                    // Process matrix line
                    String[] numbers = line.substring(1, line.length() - 1).trim().split("\\s+");
                    ArrayList<Integer> row = new ArrayList<>();
                    for (String num : numbers) {
                        row.add(Integer.parseInt(num));
                    }

                    if (readingDist) {
                        dist.add(row);
                    } else if (readingOpponents) {
                        opponents.add(row);
                    }
                } else if (line.endsWith("];")) {
                    // End of a matrix
                    if (readingDist) {
                        readingDist = false;
                    } else if (readingOpponents) {
                        readingOpponents = false;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<ArrayList<Integer>> getDist() {
        return this.dist;
    }

    public ArrayList<ArrayList<Integer>> getOpponents() {
        return this.opponents;
    }

    public int getnTeams() {
        return this.nTeams;
    }
}