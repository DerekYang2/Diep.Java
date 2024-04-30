public class Stopwatch {
    long initialTime;
    Stopwatch() {
        initialTime = -(long)1e9;
    }
    public void start() {
        initialTime = System.currentTimeMillis();
    }
    public double s() {
        return (System.currentTimeMillis() - initialTime) / 1e3;
    }
    public long ms() {
        return (System.currentTimeMillis() - initialTime);
    }
}
