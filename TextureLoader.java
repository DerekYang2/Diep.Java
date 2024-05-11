import com.raylib.java.core.Color;
import com.raylib.java.textures.Texture2D;

import java.util.ArrayList;
import java.util.HashMap;

public class TextureLoader {
    private static HashMap<Color, HashMap<String, Texture2D>> tankTextures = new HashMap<>();
    private static ArrayList<Tank> pendingTankTexture = new ArrayList<>();  // TODO: move to new class

    private static void createAndInsertTexture(String buildName, Color fillCol, Color strokeCol) {
        if (!tankTextures.containsKey(fillCol)) {  // If fill color not in map, add it
            tankTextures.put(fillCol, new HashMap<>());
        }
        if (!tankTextures.get(fillCol).containsKey(buildName)) {  // If build name not in sub-map, add it
            tankTextures.get(fillCol).put(buildName, Graphics.createTankTexture(buildName, fillCol, strokeCol));
        }
    }

    public static void pendingAdd(Tank tank) {
        pendingTankTexture.add(tank);
    }

    public static Texture2D getTankTexture(String buildName, Color fillCol) {
        return tankTextures.get(fillCol).get(buildName);
    }

    public static void refreshTankTextures() {
        for (Tank tank : pendingTankTexture) {
            String buildName = tank.tankBuild.name;
            switch (buildName) {
                case "auto gunner" -> createAndInsertTexture("gunner", tank.fillCol, tank.strokeCol);
                case "auto trapper" -> createAndInsertTexture("trapper", tank.fillCol, tank.strokeCol);
            }
            createAndInsertTexture(buildName, tank.fillCol, tank.strokeCol);
        }
        pendingTankTexture.clear();
    }

    public static void clear() {
        for (HashMap<String, Texture2D> map : tankTextures.values()) {
            for (Texture2D texture : map.values()) {
                Graphics.unloadTexture(texture);
            }
        }
        tankTextures.clear();
        pendingTankTexture.clear();
    }
}
