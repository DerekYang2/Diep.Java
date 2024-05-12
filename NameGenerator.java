import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

// NOTE: must be called after TankDefinitions.loadTankDefinitions() is called
public class NameGenerator {
    private static ArrayList<String> adjList;

    public static void initialize() {
        File adjFile = new File("assets/dictionaries/adjectives.txt");
        adjList = new ArrayList<>();

        try {
            Scanner sc = new Scanner(adjFile);
            while (sc.hasNextLine()) {
                adjList.add(sc.nextLine());
            }
            sc.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateUsername() {
        String randomBuildName = TankBuild.getRandomBuildName();
        String randomAdj = adjList.get((int) (Math.random() * adjList.size()));
        return formatNameCase(randomAdj + " " + randomBuildName);
    }

    /**
     * Format a name to have each word capitalized (a word is separated by a space)
     * Example: test-a1 test becomes Test-a1 Test
     * @param name Name to format
     * @return Formatted name
     */
    public static String formatNameCase(String name) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(Character.toUpperCase(name.charAt(0)));  // Capitalize first character

        for (int i = 1; i < name.length(); i++) {
            if (name.charAt(i-1) == ' ') {  // If previous character is a space, capitalize
                nameBuilder.append(Character.toUpperCase(name.charAt(i)));
            } else {
                nameBuilder.append(name.charAt(i));
            }
        }
        return nameBuilder.toString();
    }
}
