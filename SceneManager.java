import java.util.HashMap;

public class SceneManager {
    // Scene management
    public static Scene pendingSceneChange = null;
    public static Scene scene = Scene.MENU;
    public static HashMap<Scene, Runnable> sceneUpdateMap = new HashMap<>(), sceneDrawMap = new HashMap<>();

    public static void setSceneUpdate(Scene scene, Runnable update) {
        sceneUpdateMap.put(scene, update);
    }

    public static void setSceneDraw(Scene scene, Runnable draw) {
        sceneDrawMap.put(scene, draw);
    }

    public static void updateScene() {
        if (sceneUpdateMap.containsKey(scene)) {
            sceneUpdateMap.get(scene).run();
        }
    }

    public static void drawScene() {
        if (sceneDrawMap.containsKey(scene)) {
            sceneDrawMap.get(scene).run();
        }
    }

    public static void refreshScene() {
        if (pendingSceneChange != null) {
            scene = pendingSceneChange;
            pendingSceneChange = null;
        }
    }

    public static int getScene() {
        return scene.ordinal();
    }

    public static void setScene(Scene scene) {
        pendingSceneChange = scene;
    }
}
