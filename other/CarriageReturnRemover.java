package other;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.TreeSet;

public class CarriageReturnRemover {
    public static void main(String[] args) throws IOException {
        String fileName = "assets/dictionaries/adjectives.txt";

        // Remove all /r from the file
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        TreeSet<String> lines = new TreeSet<>();

        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine().toLowerCase());
        }

        scanner.close();

        // Rewrite the file
        FileWriter writer = new FileWriter(fileName);
        for (String line : lines) {
            writer.write(line + "\n");
        }
        writer.close();
    }
}
