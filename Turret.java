import java.awt.*;
import java.awt.geom.AffineTransform;


public class Turret implements Drawable, Updatable {
  double x, y;
  double xNew, yNew;
  double xOrigin, yOrigin;
  double prevAngle, curAngle;
  double rotateBy;
  int id;

  Stopwatch stopwatch;

  int width, length;
  double offset;



  // Debug
  double lastPrint;
  int timesPrinted = 0;

  Turret(double xOrigin, double yOrigin, int width, int length, int offset, double theta) {
    createId();
    addToPools();
    this.width = width;
    this.length = length;

    this.offset = offset;

    this.xOrigin = xOrigin;
    this.yOrigin = yOrigin;
    // turretDirection = theta;

    stopwatch = new Stopwatch();
    stopwatch.start();

    // Default spawn point, along the x axis with an offset from the origin 
    x = xOrigin + offset;
    y = yOrigin;

    lastPrint = stopwatch.ms(); 

    prevAngle = -Math.PI / 2; // spawn pointing upwards


  }  

  public void draw(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(Color.WHITE);

    // Create an AffineTransform object
    AffineTransform at = new AffineTransform();
    at.translate(x, y);
    // Rotate to the cursor
    at.rotate(Math.atan2(Main.inputInfo.mouseY - yOrigin, Main.inputInfo.mouseX - xOrigin));
    // Centering graphic draw origin
    at.translate(0, -width / 2.f);

    // Apply the transform to the Graphics2D object
    g2d.setTransform(at);

    // Draw the rectangle
    g2d.fillRect(0, 0, length, width);

    // Reset the transformations
    g2d.setTransform(new AffineTransform());
    g.setColor(Color.GREEN);
    g.fillOval((int) x - 4, (int) y - 4, 8, 8);
  }

  public void update() {
   }

  public void updatePos(double tankX, double tankY, double tankAngle) {
    // Redraw Turret in new position

    // Change position based on new position of the tank
    x = tankX + offset;
    y = tankY;

    prevAngle = -Math.PI / 2; // spawn pointing upwards
    
    tankAngle = Math.atan2(Main.inputInfo.mouseY - tankY, Main.inputInfo.mouseX - tankX);

    rotateBy = tankAngle - prevAngle;

    // Calculating new spawn points
    xNew = (x - tankX) * Math.cos(rotateBy) - (y - tankY) * Math.sin(rotateBy) + tankX;
    yNew = (x - tankX) * Math.sin(rotateBy) + (y - tankY) * Math.cos(rotateBy) + tankY;


    // Setting coordinates to new spawn points, we need the xNew and yNew variables because if we calulate x = ..., the next calculation of y = ... will use the updated x instead of the old one
    x = xNew;
    y = yNew;

    // Set the origin to the new origin of the tank so that the tank barrel can be drawn properly
    xOrigin = tankX;
    yOrigin = tankY;
    curAngle = tankAngle;
        
    // Set the current angle to the prev angle for the next cycle
    prevAngle = tankAngle;
  }

  public void shoot() {
    new Bullet(x, y, curAngle, length);
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

