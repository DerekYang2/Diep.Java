import java.awt.Graphics;
import javax.swing.*;

public class Turret implements Drawable, Updatable {
  double x, y, xOrigin, yOrigin;
  int width, length;
  double theta, angle;
  double offset;

  Turret(double xOrigin, double yOrigin, int width, int length, double angle, double offset) {
    // Rotate spawn based of origin 

    // Default values, spawn the turret pointing horizontally right
    theta = 0; // Spawn turret at theta 0
    x = offset;
    y = 0;

    this.xOrigin = xOrigin;
    this.yOrigin = yOrigin;

    this.angle = angle;
    // Rotate rectangle based on diff in angles



  }  
  public void draw(Graphics g) {
    // TODO Auto-generated method stub
    
    Graphics2D g2d = Main.drawRectCustom(g, x, y, width, length, theta);
    



  }
  public void update() {
    double c = Math.cos(angle);
    double s = Math.sin(angle);

    x = c * (xOrigin - x) - s * (yOrigin - y);
    y = c * (yOrigin - y) + s *(xOrigin - x);


    
  }
  public void delete() {
    // TODO Auto-generated method stub
    
  }
  public int getId() {
    // TODO Auto-generated method stub
    return 0;
  }
  public void createId() {
    // TODO Auto-generated method stub
    
  }
  public void addToPools() {
    // TODO Auto-generated method stub
    
  }
}
