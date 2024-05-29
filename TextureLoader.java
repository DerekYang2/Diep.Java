import com.raylib.java.core.Color;
import com.raylib.java.textures.Image;
import com.raylib.java.textures.Texture2D;

import java.util.ArrayList;
import java.util.HashMap;

public class TextureLoader {
    private static class TextureInfo {
        String buildName;
        Color fillCol, strokeCol;
        public TextureInfo(String buildName, Color fillCol, Color strokeCol) {
            this.buildName = buildName;
            this.fillCol = fillCol;
            this.strokeCol = strokeCol;
        }
    }

    private static HashMap<Color, HashMap<String, Texture2D>> tankTextures = new HashMap<>(), iconTextures = new HashMap<>();
    private static ArrayList<TextureInfo> pendingTankTexture = new ArrayList<>();

    private static void createAndInsertTexture(String buildName, Color fillCol, Color strokeCol) {
        if (!tankTextures.containsKey(fillCol)) {  // If fill color not in map, add it
            tankTextures.put(fillCol, new HashMap<>());
            iconTextures.put(fillCol, new HashMap<>());
        }

        if (!tankTextures.get(fillCol).containsKey(buildName)) {  // If build name not in sub-map, add it
            Image tankImage = Graphics.createTankImage(buildName, fillCol, strokeCol);
            tankTextures.get(fillCol).put(buildName, Graphics.loadTextureFromImage(tankImage));
            Graphics.imageResize(tankImage, 0.097f);
            iconTextures.get(fillCol).put(buildName, Graphics.loadTextureFromImage(tankImage));
        }
    }

    public static void pendingAdd(String buildName, Color fillCol, Color strokeCol) {
        pendingTankTexture.add(new TextureInfo(buildName, fillCol, strokeCol));
        System.out.println("Pending add: " + buildName + " " + fillCol);
    }

    public static Texture2D getTankTexture(String buildName, Color fillCol) {
        return tankTextures.get(fillCol).get(buildName);
    }

    public static Texture2D getIconTexture(String buildName, Color fillCol) {
        return iconTextures.get(fillCol).get(buildName);
    }

    public static void refreshTankTextures() {
        for (TextureInfo texInfo : pendingTankTexture) {
            switch (texInfo.buildName) {
                case "auto gunner" -> createAndInsertTexture("gunner", texInfo.fillCol, texInfo.strokeCol);
                case "auto trapper" -> createAndInsertTexture("trapper", texInfo.fillCol, texInfo.strokeCol);
            }
            createAndInsertTexture(texInfo.buildName, texInfo.fillCol, texInfo.strokeCol);
        }
        pendingTankTexture.clear();
    }

    public static void clear() {
        for (HashMap<String, Texture2D> map : tankTextures.values()) {
            for (Texture2D texture : map.values()) {
                Graphics.unloadTexture(texture);
            }
        }
        for (HashMap<String, Texture2D> map : iconTextures.values()) {
            for (Texture2D texture : map.values()) {
                Graphics.unloadTexture(texture);
            }
        }

        tankTextures.clear();
        iconTextures.clear();
        pendingTankTexture.clear();
    }
}
