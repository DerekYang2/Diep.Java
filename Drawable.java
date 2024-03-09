import java.awt.Graphics;
public abstract class Drawable extends Deletable
{
    public Drawable() {
        Main.drawablePool.addObj(this);
    }

    public abstract void draw(Graphics g);

    @Override
    public void delete() {
        Main.drawablePool.deleteObj(this.getId());
    }
}
