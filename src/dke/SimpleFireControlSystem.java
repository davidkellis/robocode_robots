package dke;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.Bullet;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class SimpleFireControlSystem implements FireControlSystem {
  public enum State {
    Initial, ScanningForTarget, TargetAquired
  }

  public DkeRobot robot;
  public State currentState;

  public long timeTargetLastSeen;
  public long fireTime;
  public double firePower;
  public double radarScanWidthMultiplier;
  public String currentTarget;

  public SimpleFireControlSystem(DkeRobot robot) {
    this.robot = robot;
    currentState = State.Initial;
    fireTime = 0;
    firePower = 1.001;
    radarScanWidthMultiplier = 1.7;
    timeTargetLastSeen = 0;
    currentTarget = null;
  }
  
  public void run() {
    switch(currentState) {
    case Initial:
      scanForTargets();
      currentState = State.ScanningForTarget;
      break;
    case ScanningForTarget:
      scanForTargets();
      break;
    case TargetAquired:
      long currentTime = robot.getTime();
      
      if(currentTime - timeTargetLastSeen > 1) {          // target lost, so re-acquire
        scanForTargets();
        currentState = State.ScanningForTarget;
      } 
      break;
    }
//    robot.execute();
  }
  
  public void scanForTargets() {
    robot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
  }
  
  public void onScannedRobot(ScannedRobotEvent e) {
    if(currentTarget == null) {
      target(e.getName());
    }
    if(e.getName().equals(currentTarget)) {
      if (robot.getTime() == fireTime &&               // target is acquired, and gun is aimed, so fire
          robot.getGunTurnRemaining() == 0 && 
          robot.getGunHeat() == 0) {
        fireGun();
      }
      timeTargetLastSeen = e.getTime();
      trackTarget(e.getBearingRadians());
      currentState = State.TargetAquired;
    }
  }
  
  public void trackTarget(double targetBearing) {
    double targetHeading = robot.currentAbsoluteHeading() + targetBearing;
    double radarBearingToTarget = targetHeading - robot.getRadarHeadingRadians();
    robot.setTurnRadarRightRadians(radarScanWidthMultiplier * Utils.normalRelativeAngle(radarBearingToTarget));
    aimAtHeading(targetHeading);
  }
  
  public void aimAtRobotBearing(double bearing) {
    double heading = robot.currentAbsoluteHeading() + bearing;
    aimAtHeading(heading);
  }
  
  public void aimAtHeading(double heading) {
    double gunBearing = heading - robot.getGunHeadingRadians();
    aimAtGunBearing(gunBearing);
  }
  
  public void aimAtGunBearing(double bearing) {
    robot.setTurnGunRightRadians(Utils.normalRelativeAngle(bearing));
    // Don't need to check whether gun turn will complete in single turn because
    // we check that gun is finished turning before calling setFire(...).
    // This is simpler since the precise angle your gun can move in one tick
    // depends on where your robot is turning.
    fireTime = robot.getTime() + 1;
  }
  
  public void target(String robotName) {
    currentTarget = robotName;
  }
  
  public Bullet fireGun() {
    return robot.setFireBullet(firePower);
  }
  
  public void onPaint(Graphics2D g) {
    int x = (int)robot.getX();
    int y = (int)robot.getY();
    Point2D.Double pt;

    pt = robot.pointAtHeading(robot.getGunHeadingRadians(), 800);
    g.setColor(new Color(0xff, 0x14, 0x93, 0x80));      // deep pink
    g.drawLine(x, y, (int)pt.getX(), (int)pt.getY());
  }
}
