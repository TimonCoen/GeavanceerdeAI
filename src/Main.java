import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String filePath = "instances/umps8.txt"; // Update with actual file path
        try {
            readTUPInput(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readTUPInput(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));

        int nTeams = 0;
        int[][] distanceMatrix;
        int[][] tournamentSchedule;

        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("nTeams")) {
                nTeams = Integer.parseInt(line.split("=")[1].trim().replace(";", ""));
                break; // Assumes the number of teams is declared at the beginning
            }
        }

        // Skip to the distance matrix
        br.readLine(); // Read the dist= line
        distanceMatrix = new int[nTeams][nTeams];
        for (int i = 0; i < nTeams; i++) {
            line = br.readLine().trim().replace("[", "").replace("]", "");
            String[] distances = line.split("\\s+");
            for (int j = 0; j < distances.length; j++) {
                System.out.println(distances[j]);
                distanceMatrix[i][j] = Integer.parseInt(distances[j]);
            }
        }

        // Skip to the tournament schedule
        br.readLine(); // Read the opponents= line
        tournamentSchedule = new int[2 * nTeams - 2][nTeams]; // R = 2N - 2
        for (int r = 0; r < 2 * nTeams - 2; r++) {
            line = br.readLine().trim().replace("[", "").replace("]", "");
            String[] opponents = line.split("\\s+");
            for (int i = 0; i < opponents.length; i++) {
                tournamentSchedule[r][i] = Integer.parseInt(opponents[i]);
            }
        }

        br.close();

        // Print matrices for verification
        System.out.println("Distance Matrix:");
        printMatrix(distanceMatrix);
        System.out.println("\nTournament Schedule:");
        printMatrix(tournamentSchedule);
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

}
