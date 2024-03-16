import java.awt.*;
import java.awt.geom.AffineTransform;


public class Turret implements Drawable, Updatable {
  double x, y;
  double xNew, yNew;
  double xOrigin, yOrigin;
  double prevAngle, newAngle;
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
    
    x = xOrigin + offset;
    y = yOrigin;

    lastPrint = stopwatch.ms(); 

    prevAngle = -Math.PI / 2; // spawn pointing upwards


  }  

  public void draw(Graphics g) {
    // Graphics2D g2d = (Graphics2D) g;
    // g2d.setColor(Color.WHITE);

    // // Create an AffineTransform object
    // AffineTransform at = new AffineTransform();
    // at.translate(x, y);
    // // Rotate to the cursor
    // at.rotate(Math.atan2(Main.inputInfo.mouseY - y, Main.inputInfo.mouseX - x));
    // // Centering graphic draw origin
    // at.translate(0, -width / 2.f);

    // // Apply the transform to the Graphics2D object
    // g2d.setTransform(at);

    // // Draw the rectangle
    // g2d.fillRect(0, 0, length, width);

    // // Reset the transformations
    // g2d.setTransform(new AffineTransform());
    g.setColor(Color.RED);
    g.fillOval((int) x - 4, (int) y - 4, 8, 8);
  }

  public void update() {
    if (timesPrinted < 2) {
      System.out.println(x);
      System.out.println(y);
      timesPrinted++;
      newAngle =  Math.atan2(Main.inputInfo.mouseY - yOrigin, Main.inputInfo.mouseX - xOrigin);
      System.out.println("newAngle: " + newAngle);
      System.out.println("prevAngle: " + prevAngle);
      System.out.println("rotate by: " + (newAngle - prevAngle));

    }
    // System.out.println("from tur 2: " + turretDirection);
    newAngle = Math.atan2(Main.inputInfo.mouseY - yOrigin, Main.inputInfo.mouseX - xOrigin);

    rotateBy = newAngle - prevAngle;

    // Calculating new spawn points
    xNew = (x - xOrigin) * Math.cos(rotateBy) - (y - yOrigin) * Math.sin(rotateBy) + xOrigin;
    yNew = (x - xOrigin) * Math.sin(rotateBy) + (y - yOrigin) * Math.cos(rotateBy) + yOrigin;


    // Setting coordinates to new spawn points, we need the xNew and yNew variables because if we calulate x = ..., the next calculation of y = ... will use the updated x instead of the old one
    x = xNew;
    y = yNew;
    
    if (stopwatch.ms() - lastPrint >= 3000 ) {
      System.out.format("%8s %8s %8s %8s %8s %8s %8s", "rotateBy: ", rotateBy, "x: ", x , "y: ", y, "\n");
      System.out.format("%8s %8s %8s %8s %8s",  "xOrigin: ", xOrigin, "yOrigin: ", yOrigin, "\n");

      
      lastPrint = stopwatch.ms();
      
    }

        
    // Set the current angle to the prev angle for the next cycle
    prevAngle = newAngle;
    
    

    // System.out.println(turretDirection);
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

