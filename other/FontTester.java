package other;

import com.raylib.java.textures.Image;
import com.raylib.java.textures.rTextures;

import java.io.IOException;

public class FontTester {
    public static void main(String[] args) throws IOException {


            Image image = rTextures.LoadImage("assets/Circle.png");
            System.out.println("Image loaded: " + image.width + "x" + image.height);
/*            String fileName = "assets/dejavu.fnt";
            int lineTracker = 1;
            String fileText = null;
            String imFileName = null;

            try {
                fileText = FileIO.LoadFileText(fileName);
            } catch (IOException var23) {
                IOException e = var23;
                e.printStackTrace();
            }
            fileText = fileText.replace("\r", "");
            String[] fileLines = fileText.split("\n");  // Needs to be split by \r\n for Windows
            int fontSize = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("lineHeight=") + 11, fileLines[lineTracker].indexOf("base=") - 1));
            int imWidth = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("scaleW=") + 7, fileLines[lineTracker].indexOf("scaleH=") - 1));
            int imHeight = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("scaleH=") + 7, fileLines[lineTracker].indexOf("pages=") - 1));
            ++lineTracker;
            System.out.println("FONT: [" + fileName + "] Loaded font info:");
            System.out.println("    > Base size: " + fontSize);
            System.out.println("    > Texture scale: " + imWidth + "x" + imHeight);
            imFileName = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("file=\"") + 6, fileLines[lineTracker].lastIndexOf("\""));
            ++lineTracker;
            System.out.println("    > Texture filename: " + imFileName);
            int charsCount = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("count=") + 6));
            ++lineTracker;
            System.out.println("    > Chars count: " + charsCount);
            String imPath = fileName.substring(0, fileName.lastIndexOf(47) + 1) + imFileName;*/

    }
}
