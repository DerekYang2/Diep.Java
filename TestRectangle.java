import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class TestRectangle implements Drawable, Updatable{
  protected int id;
  double x, y;
  double scaleFactor;

  // When you spawn the rectangle, pass in values of unscaled position
  TestRectangle(int xAbsolute, int yAbsolute) { 
    createId();  // Remember to create an id on creation
    addToPools();  // Remember to add object to pool on creation
    // Testing position
    x = xAbsolute;
    y = yAbsolute;

    new TestTwin();
  }

  @Override
  public void draw(Graphics g) {
    int scaledX = (int) ((x) * GamePanel.scaleFactor) + GamePanel.cameraX;
    int scaledY = (int) ((y) * GamePanel.scaleFactor) + GamePanel.cameraY;

    int c1x = (int) ((0) * GamePanel.scaleFactor) + GamePanel.cameraX;
    int c1y = (int) ((100) * GamePanel.scaleFactor) + GamePanel.cameraY;
    int c2x = (int) ((100) * GamePanel.scaleFactor) + GamePanel.cameraX;
    int c2y = (int) ((100) * GamePanel.scaleFactor) + GamePanel.cameraY;
    int c3x = (int) ((200) * GamePanel.scaleFactor) + GamePanel.cameraX;
    int c3y = (int) ((100) * GamePanel.scaleFactor) + GamePanel.cameraY;
    // on every frame update, change the position to the 

    
    g.setColor(Color.pink);
    g.fillRect(GamePanel.cameraX, GamePanel.cameraY, GamePanel.scaledWidth, GamePanel.scaledHeight);
    
    g.setColor(Color.BLUE);
    g.fillRect(scaledX , scaledY ,(int) (40 * GamePanel.scaleFactor),(int) (60 * GamePanel.
    scaleFactor));
    
    g.setColor(Color.WHITE);
    g.fillOval(GamePanel.cameraX, GamePanel.cameraY, 10, 10);

    g.fillOval(c1x, c1y, 10, 10);
    g.fillOval(c2x, c2y, 10, 10);
    g.fillOval(c3x, c3y, 10, 10);

    // Expect some scale factor, and in main change the screen size -> in draw change everything by that scale factor 
    // The position will be (rx - cx) * scaleFactor , (ry - cy) * scaleFactor




    

    
  }
  @Override
  public void update() {
    // change position to be     
  }
  @Override
  public void delete() {
    Main.drawablePool.deleteObj(this.getId());
    Main.updatablePool.deleteObj(this.getId());
    
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public void createId() {
    this.id = Main.idServer.getId();    
  }

  @Override
  public void addToPools() {
    // TODO Auto-generated method stub
    Main.drawablePool.addObj(this);
    Main.updatablePool.addObj(this);
    
  }
}
