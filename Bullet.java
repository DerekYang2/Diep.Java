import java.awt.*;

public class Bullet implements Drawable, Updatable  {
  protected double x, y, vx, vy;
  protected int id;
  
  protected double direction, vt;
  Stopwatch stopwatch;
  float friction = 1 / 1.3f;
  float xAcceleration = 0.4f, yAcceleration = 0.4f;
 
  // The bullet trajectory will be determined based on the position where it spawns
  public Bullet(double spawnX, double spawnY, double direction, int cannonLength) {
    createId();
    addToPools();
    
    stopwatch = new Stopwatch();
    stopwatch.start();
      
    vt = 5; // temp, will be based off speed eventually

    /* 4 Quadrants:
     * case : sign of cos, sign of sin -> proper bullet velocity sign
     * 0 -> pi / 2 : cos +, sin + -> x: - y: -
     * pi / 2-> pi : cos -, sin + -> x: + y: -
     * ... always flip the sign   
     */
    vx = vt * Math.cos(direction);
    vy = vt * Math.sin(direction);
    
    // System.out.println("vX: " + vx);
    // System.out.println("vY: " + vy);
    // System.out.println("cos: " + Math.cos(direction)); 
    // System.out.println("sin: " + Math.sin(direction)); 

    
    // x and y positions set based on of tank head  
    x = spawnX + cannonLength * Math.cos(direction); 
    y = spawnY + cannonLength * Math.sin(direction);

  } 
  public void update() {
    y += vy;
    x += vx;
  }

  public void draw(Graphics g) {
    g.setColor(Color.red);
    
    g.fillOval((int) x - 6, (int) y - 6, 12, 12);
  }

  public void createId() {
    this.id = Main.idServer.getId();
  }

  public int getId() {
    return this.id;
  }

  public void addToPools() {
    Main.drawablePool.addObj(this);
    Main.updatablePool.addObj(this);
  }

  public void delete() {
    Main.drawablePool.deleteObj(this.getId());
    Main.updatablePool.deleteObj(this.getId());
  }
    
}
