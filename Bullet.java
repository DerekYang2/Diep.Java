public class Bullet implements Drawable, Updatable  {
  protected double x, y, vx, vy;
  protected int id;
  protected float diameter;
  protected double direction, vt;
  int lifeFrames = 120 * 3;
  float friction = 1 / 1.3f;
  float xAcceleration = 0.4f, yAcceleration = 0.4f;
 
  // The bullet trajectory will be determined based on the position where it spawns
  public Bullet(double spawnX, double spawnY, double direction, float cannonLength, float diameter) {
    createId();
    addToPools();

    this.diameter = diameter;
    vt = 8; // temp, will be based off speed eventually

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
    lifeFrames--;
    if (lifeFrames <= 0) {
      delete();
    }
  }

  public void draw() {
    float radius = diameter * 0.5f;
    Graphics.drawCircle((int) x, (int) y, radius, Graphics.strokeWidth, Graphics.BLUE, Graphics.BLUE_STROKE);
  }

  public void createId() {
    this.id = Main.idServer.getIdFront();
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
