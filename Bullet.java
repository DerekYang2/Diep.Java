import java.util.*;
import java.awt.*;

public class Bullet implements Drawable, Updatable  {
  protected double x, y, vx, vy;
  protected int id;
  
  protected double direction, vt;
  Stopwatch stopwatch;
  float friction = 1 / 1.3f;
  float xAcceleration = 0.4f, yAcceleration = 0.4f;
 
  public Bullet(double tankX, double tankY) {
    createId();
    addToPools();
    
    stopwatch = new Stopwatch();
    stopwatch.start();
      
    double deltaY = tankY - Main.inputInfo.mouseY;
    double deltaX = tankX - Main.inputInfo.mouseX;
    direction = Math.atan2(deltaY, deltaX);
    vt = 5;

    /* 4 Quadrants:
     * case : sign of cos, sign of sin -> proper bullet velocity sign
     * 0 -> pi / 2 : cos +, sin + -> x: - y: -
     * pi / 2-> pi : cos -, sin + -> x: + y: -
     * ... always flip the sign   
     */
    vx = vt * -Math.cos(direction);
    vy = vt * -Math.sin(direction);
    
    // System.out.println("Direction: " + direction);
    // System.out.println("vX: " + vx);
    // System.out.println("vY: " + vy);
    // System.out.println("cos: " + Math.cos(direction)); 
    // System.out.println("sin: " + Math.sin(direction)); 

    // The head of the tank is slightly off front the coordinate (x, y)
    double tankHeadDirection;
    // TODO: fix float comparisent?aw  
    if (direction > Math.PI) {
          
    }
    
    
    // x and y positions set based on of tank head  
    x = tankX + 50 * -Math.cos(direction); 
    y = tankY + 50 * -Math.sin(direction);

  } 
  public void update() {
    y += vy;
    x += vx;
  }

  public void draw(Graphics g) {
    g.setColor(Color.red);
    g.fillOval((int) x - 8, (int) y - 8, 16, 16);
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
    Main.idServer.returnId(this.getId());
  }
    
}
