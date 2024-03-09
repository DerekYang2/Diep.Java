import java.util.HashMap;
import java.util.Stack;

public class Pool <T extends Deletable> {
    private final int MAX_OBJS = (int)1e4;
    private Stack<Integer> availableIds;  // stack of available ids
    private HashMap<Integer, T> objects;  // id -> pointer to object
    private Stack<Integer> pendingDeletes;  // stack of ids to waiting to be deleted

    public Pool() {
        availableIds = new Stack<>();
        objects = new HashMap<>();
        // Fill the availableIds stack with max objects
        for (int i = 0; i < MAX_OBJS; i++) {
            availableIds.push(i);
        }
    }

    public void addObj(T obj) {
        int id = availableIds.pop();
        obj.setId(id);
        objects.put(id, obj);
    }

    public void deleteObj(int id) {
        pendingDeletes.push(id);
    }

    // Handles the pending deletes
    public void refreshPool() {
        while (!pendingDeletes.isEmpty()) {
            int id = pendingDeletes.pop();
            objects.remove(id);  // remove the object from the pool
            availableIds.push(id);  // add the id back to the availableIds stack
        }
    }

    // Return hashmap.values()
    public Iterable<T> getObjects() {
        return objects.values();
    }
}
