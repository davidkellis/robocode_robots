package dke;

import robocode.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Herb extends DkeRobot {
  public MovementStrategy movementStrategy;
  public FireControlSystem fireControlSystem;

  @Override
  public void run() {
    initialize();
    while (true) {
      movementStrategy.moveRobot();
      fireControlSystem.run();
      execute();
    }
  }

  public void initialize() {
    super.initialize();
    
//    System.out.println(this);
    
    setColors(Color.orange, Color.black, Color.yellow, Color.green, Color.white); // body,gun,radar

    //movementStrategy = new RectangularMovementStrategy(this);
    movementStrategy = new RandomMovementStrategy(this);
    
    fireControlSystem = new StateLoggingFireControlSystem(this);
  }

  /*
   * IMPORTANT NOTE: It seems that the onScannedRobot() event handler fires **before** the action loop is given the opportunity to run.
   *                 This affects how the robot should schedule its gun shots.
   */
  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    fireControlSystem.onScannedRobot(e);
  }

  @Override
  public void onHitByBullet(HitByBulletEvent e) {
  }

  @Override
  public void onHitWall(HitWallEvent e) {
    movementStrategy.onHitWall(e);
  }

  @Override
  public void onHitRobot(HitRobotEvent e) {
    fireControlSystem.acquireTarget(e.getName());
    fireControlSystem.trackTarget(e.getBearingRadians());
    movementStrategy.onHitRobot(e);
  }

  @Override
  public void onSkippedTurn(SkippedTurnEvent e) {
    System.out.println("Skipping a turn!");
  }
  
  public void onPaint(Graphics2D g) {
    // Set the paint color to a red half transparent color
    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));

    try {
      Method m = movementStrategy.getClass().getMethod("onPaint", new Class[]{Graphics2D.class});
      m.invoke(movementStrategy, new Object[]{g});
      m = fireControlSystem.getClass().getMethod("onPaint", new Class[]{Graphics2D.class});
      m.invoke(fireControlSystem, new Object[]{g});
    } catch (SecurityException e) {
      System.out.println(e.getStackTrace());
    } catch (NoSuchMethodException e) {
      System.out.println(e.getStackTrace());
    } catch (IllegalArgumentException e) {
      System.out.println(e.getStackTrace());
    } catch (IllegalAccessException e) {
      System.out.println(e.getStackTrace());
    } catch (InvocationTargetException e) {
      System.out.println(e.getStackTrace());
    }
  }
}