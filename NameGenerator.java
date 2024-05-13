import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

// NOTE: must be called after TankDefinitions.loadTankDefinitions() is called
public class NameGenerator {
    private static ArrayList<String> adjList, nounList;

    public static void initialize() {
        File adjFile = new File("assets/dictionaries/adjectives.txt"), nounFile = new File("assets/dictionaries/english-nouns.txt");
        adjList = new ArrayList<>();
        nounList = new ArrayList<>();

        try {
            Scanner sc = new Scanner(adjFile);
            while (sc.hasNextLine()) {
                adjList.add(sc.nextLine());
            }
            sc.close();

            sc = new Scanner(nounFile);
            while (sc.hasNextLine()) {
                nounList.add(sc.nextLine());
            }
            sc.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateUsername() {
        //String randomBuildName = TankBuild.getRandomBuildName();
        String randomName;
        do {
            randomName = adjList.get(Graphics.randInt(0, adjList.size()));
            String randomNoun = nounList.get(Graphics.randInt(0, nounList.size()));
            randomName += Character.toUpperCase(randomNoun.charAt(0)) + randomNoun.substring(1);  // Capitalize first letter of noun
        } while (randomName.length() > 15);
        return formatNameCase(randomName);
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
