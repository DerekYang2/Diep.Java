import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

// NOTE: must be called after TankDefinitions.loadTankDefinitions() is called
public class NameGenerator {
    private static ArrayList<String> adjList, nounList, agentNounList;

    public static void initialize() {
        File adjFile = new File("assets/dictionaries/adjectives.txt"), nounFile = new File("assets/dictionaries/nouns.txt");
        adjList = new ArrayList<>();
        nounList = new ArrayList<>();
        agentNounList = new ArrayList<>();

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

            for (String noun : nounList) {
                if (noun.endsWith("er")) {
                    agentNounList.add(noun);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateUsername() {
        //String randomBuildName = TankBuild.getRandomBuildName();
        String randomName;
        do {
            double rand = Math.random();
            if (rand < 0.1) {
                randomName = nounList.get(Graphics.randInt(0, nounList.size()));
                if (rand < 0.06) {
                    randomName = randomName.toUpperCase();
                }
            } else if (rand < 0.1 + 0.4) {
                randomName = adjList.get(Graphics.randInt(0, adjList.size()));
                String randomNoun = nounList.get(Graphics.randInt(0, nounList.size()));
                randomName += Character.toUpperCase(randomNoun.charAt(0)) + randomNoun.substring(1);  // Capitalize first letter of noun
            } else if (rand < 0.1 + 0.4 + 0.1) {
                randomName = adjList.get(Graphics.randInt(0, adjList.size()));
                String tankName = TankBuild.getRandomBuildName();
                randomName += Character.toUpperCase(tankName.charAt(0)) + tankName.substring(1);  // Capitalize first letter of noun
            } else {
                randomName = adjList.get(Graphics.randInt(0, adjList.size()));
                String randomNoun = agentNounList.get(Graphics.randInt(0, agentNounList.size()));
                randomName += Character.toUpperCase(randomNoun.charAt(0)) + randomNoun.substring(1);  // Capitalize first letter of noun
            }

            randomName = randomName.replace(" ", "");
            randomName = randomName.replace("-", "");

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
