public abstract class Updatable extends Deletable {
    public Updatable() {
        Main.updatablePool.addObj(this);
    }
    public abstract void update();
    @Override
    public void delete() {
        Main.updatablePool.deleteObj(this.getId());
    }
}
