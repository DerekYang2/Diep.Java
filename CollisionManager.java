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
    final public static float sectorSize = 125;
    private static int sectorsX;
    private static float minX, minY, maxX, maxY;
    private static HashMap<Integer, ArrayList<Integer>> collisionGroups = new HashMap<>();
    private static HashSet<Integer> collidedPairs = new HashSet<>();

    public static int getSectorX(float x) {
        return (int) ((x - minX) / sectorSize);
    }

    public static int getSectorY(float y) {
        return (int) ((y - minY) / sectorSize);
    }

    public static void updateSectors() {
        collisionGroups.clear();

        minX = Float.MAX_VALUE; maxX = Float.MIN_VALUE; minY = Float.MAX_VALUE; maxY = Float.MIN_VALUE;  // Initialize to extreme values
        // Find the minimum and maximum x, y positions of all game objects
        for (GameObject object : Main.gameObjectPool.getObjects()) {
            Rectangle boundingBox = object.boundingBox();
            minX = Math.min(minX, boundingBox.x);
            minY = Math.min(minY, boundingBox.y);
            maxX = Math.max(maxX, boundingBox.x + boundingBox.width);
            maxY = Math.max(maxY, boundingBox.y + boundingBox.height);
        }
        sectorsX = (int) ((maxX - minX) / sectorSize) + 2;

        for (GameObject object : Main.gameObjectPool.getObjects()) {
            Rectangle boundingBox = object.boundingBox();
            int xiMin = getSectorX(boundingBox.x), xiMax = getSectorX(boundingBox.x + boundingBox.width);
            int yiMin = getSectorY(boundingBox.y), yiMax = getSectorY(boundingBox.y + boundingBox.height);

            for (int xi = xiMin; xi <= xiMax; xi++) {
                for (int yi = yiMin; yi <= yiMax; yi++) {
                    int sectorHash = xi + sectorsX * yi;
                    if (!collisionGroups.containsKey(sectorHash)) {  // Initialize if current sector group does not exist
                        collisionGroups.put(sectorHash, new ArrayList<>());
                    }
                    collisionGroups.get(sectorHash).add(object.id);  // Add object id to the sector group
                }
            }
        }
    }

    public static void updateCollision() {
        updateSectors();
        collidedPairs.clear();
        int minId = Main.idServer.peekFrontId();
        int idRange = Main.idServer.peekBackId() - minId + 1;  // Maximum value of id - minimum value of id

        for (ArrayList<Integer> group : collisionGroups.values()) {
            if (group.size() == 1) continue;
            for (int id : group) {
                for (int id2 : group) {
                    if (id != id2) {
                        GameObject obj1 = Main.gameObjectPool.getObj(id), obj2 = Main.gameObjectPool.getObj(id2);
                        if (obj1.group != obj2.group) {  // Collision only if groups are different
                            int objHash = (obj1.id - minId) * idRange + (obj2.id - minId);  // Unique hash for a pair of ids
                            if (obj1.checkCollision(obj2) && !collidedPairs.contains(objHash)) {
                                obj1.receiveKnockback(obj2);
                                collidedPairs.add(objHash);
                            }
                        }
                    }
                }
            }
        }
    }
}
