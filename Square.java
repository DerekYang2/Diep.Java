import java.awt.*;
// Add back updatadable 
public class Square implements Drawable{
  protected double x, y, vx, vy;
  protected int id;
  Stopwatch stopwatch;
  public Square() {
      createId();  // Remember to create an id on creation
      addToPools();  // Remember to add object to pool on creation

      stopwatch = new Stopwatch();
      stopwatch.start();

      x = Math.random() * Main.windowWidth;
      y = Math.random() * Main.windowHeight;
  }

  public void update() {
      
  }

  public void draw(Graphics g) {
    int sqaureLength = 10;
    int sqaureWidth = 10;
    // this is not good probably
    g.setColor(Color.BLUE);
    g.fillRect((int)x, (int)y, sqaureWidth, sqaureLength);
  }

  // Deletable Methods
  public void createId() {
      this.id = Main.idServer.getId();
  }

  public int getId() {
      return this.id;
  }

  public void addToPools() {
      Main.drawablePool.addObj(this);
  }

  public void delete() {
      // All added to wait lists
      Main.drawablePool.deleteObj(this.getId());
      Main.updatablePool.deleteObj(this.getId());
      Main.idServer.returnId(this.getId());
  }
  
}
