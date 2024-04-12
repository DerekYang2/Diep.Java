public interface Controller {
    public void setHost(Tank host);
    public float barrelDirection();
    public float moveDirection();
    public boolean fire();
    public boolean unload();
}
