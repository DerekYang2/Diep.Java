import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages collisions between objects
 * Spatial hashing is used to reduce the number of collision checks
 */
public class CollisionManager {
    final private static float sectorSize = 150;
    final private static int sectorsX = 500, sectorsY = 500;
    static HashMap<Integer, ArrayList<Integer>> collisionGroups = new HashMap<>();
    static HashSet<Integer> collidedPairs = new HashSet<>();

    public static int hash(Vector2 pos) {
        return (int) (pos.x / sectorSize) + (int) (pos.y / sectorSize) * sectorsX;
    }

    public static void updateSectors() {
        collisionGroups.clear();
        for (GameObject object : Main.gameObjectPool.getObjects()) {
            Rectangle boundingBox = object.boundingBox();
            int xiMin = (int) (boundingBox.x / sectorSize), xiMax = (int) ((boundingBox.x + boundingBox.width) / sectorSize);
            int yiMin = (int) (boundingBox.y / sectorSize), yiMax = (int) ((boundingBox.y + boundingBox.height) / sectorSize);

            for (int xi = xiMin; xi <= xiMax; xi++) {
                for (int yi = yiMin; yi <= yiMax; yi++) {
                    int sector = xi + yi * sectorsX;
                    if (!collisionGroups.containsKey(sector)) {
                        collisionGroups.put(sector, new ArrayList<>());
                    }
                    collisionGroups.get(sector).add(object.id);
                }
            }
        }
    }

    public static void updateCollision() {
        updateSectors();
        collidedPairs.clear();
        int minId = Main.idServer.peekFrontId();
        int numObjects = Main.gameObjectPool.getObjects().size();

        for (ArrayList<Integer> group : collisionGroups.values()) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = 0; j < group.size(); j++) {
                    if (i != j) {
                        GameObject obj1 = Main.gameObjectPool.getObj(group.get(i)),
                                   obj2 = Main.gameObjectPool.getObj(group.get(j));
                        if (obj1.group != obj2.group) {
                            int objHash = (obj1.id - minId) * (numObjects + 1) + (obj2.id - minId);
                            if (!collidedPairs.contains(objHash) && obj1.checkCollision(obj2)) {
                                obj1.receiveKnockback(obj2);
                                collidedPairs.add(objHash);
                            }
                        }
                    }
                }
            }
            if (Main.counter % 60 == 0) {
                System.out.print(group.size() + " ");
            }
        }
        if (Main.counter % 60 == 0) {
            System.out.println();
        }
    }
}
