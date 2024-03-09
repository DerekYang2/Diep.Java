abstract class Deletable {
    public int id;
    public void setId(int i) {
        id = i;
    }
    public int getId() {
        return id;
    }
    public abstract void delete();
}
