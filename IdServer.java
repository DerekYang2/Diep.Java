import java.util.Stack;

public class IdServer {
    private Stack<Integer> availableIds;  // stack of available ids
    private Stack<Integer> pendingReturns;  // stack of ids waiting to be returned
    final int MAX_IDS = (int)1e5;

    public IdServer() {
        availableIds = new Stack<>();
        pendingReturns = new Stack<>();
        for (int i = 0; i < MAX_IDS; i++) {
            availableIds.push(i);
        }
    }

    public int getId() {
        return availableIds.pop();
    }

    public void returnId(int id) {
        pendingReturns.push(id);
    }

    public void refresh() {
        while (!pendingReturns.isEmpty()) {
            availableIds.push(pendingReturns.pop());
        }
    }
}
