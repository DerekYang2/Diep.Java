package other;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class CarriageReturnRemover {
    public static void main(String[] args) throws FileNotFoundException {
        String fileName = "assets/dejavu.fnt";

        // Remove all /r from the file
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        StringBuilder newText = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            newText.append(line.replace("\r", ""));
            newText.append("\n");
        }
        scanner.close();

        PrintWriter writer = new PrintWriter(fileName);
        writer.print(newText);
        writer.close();

        System.out.println("Done");
    }
}
