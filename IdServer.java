import java.util.Stack;

public class IdServer {
    private int counter;

    public IdServer() {
        counter = 0;
    }

    public int getId() {
        return counter++;
    }
}
