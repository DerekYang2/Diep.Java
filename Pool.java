import java.util.Map;
import java.util.Stack;
import java.util.Collection;
import java.util.TreeMap;

public class Pool <T extends Deletable> {
    private Map<Integer, T> objects;  // id -> pointer to object
    private Stack<T> pendingAdds; // stack of ids waiting to be added
    private Stack<Integer> pendingDeletes;  // stack of ids to waiting to be deleted

    public Pool() {
        objects = new TreeMap<>();
        pendingAdds = new Stack<>();
        pendingDeletes = new Stack<>();
    }

    public void addObj(T obj) {
        pendingAdds.push(obj);
    }

    public void deleteObj(int id) {
        pendingDeletes.push(id);
    }

    // Handles the pending deletes
    public void refresh() {
        // Update pending adds
        while (!pendingAdds.isEmpty()) {
            T obj = pendingAdds.pop();
            objects.put(obj.getId(), obj);  // add the object to the pool
        }
        // Update pending deletes
        while (!pendingDeletes.isEmpty()) {
            int id = pendingDeletes.pop();
            objects.remove(id);  // remove the object from the pool
        }
    }

    // Return hashmap.values()
    public Collection<T> getObjects() {
        return objects.values();
    }
}
