package dke;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

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
  public ArrayList<PositionTuple> targetPositionLog;
  public int numberOfObservationsToAverage;
  public LinearTargetingModel targetingModel;

  public SimpleFireControlSystem(DkeRobot robot) {
    this.robot = robot;
    targetingModel = new LinearTargetingModel(robot);
    currentState = State.Initial;
    fireTime = 10000;
    firePower = 2.001;
    radarScanWidthMultiplier = 1.7;
    timeTargetLastSeen = 0;
    currentTarget = null;
    targetPositionLog = new ArrayList<PositionTuple>();
    numberOfObservationsToAverage = 2;
  }
  
  /*
   * IMPORTANT NOTE: It seems that the onScannedRobot() event handler fires **before** the action loop is given the opportunity to run.
   *                 This affects how the robot should schedule its gun shots.
   */
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
      
      if(currentTime - timeTargetLastSeen > 1) {    // target lost, so re-acquire
        scanForTargets();
        currentState = State.ScanningForTarget;
      }
      break;
    }
  }
  
  public void scanForTargets() {
    robot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
  }
  
  public void onScannedRobot(ScannedRobotEvent e) {
    // acquire a target if we don't currently have one
    if(currentTarget == null || isCurrentTargetDeadOrLost()) {
      acquireTarget(e.getName());
      targetPositionLog = new ArrayList<PositionTuple>();
    }
    
    // if we've just scanned the acquired target, log the enemy position information, then target them, and fire.
    if(e.getName().equals(currentTarget)) {
      currentState = State.TargetAquired;
      timeTargetLastSeen = e.getTime();
      
      logTargetPosition(e);
      tryToFireGun();
      trackTarget(e.getBearingRadians());
      aimGun();     // aim gun every time we scan the enemy robot.
    }
  }
  
  public boolean isCurrentTargetDeadOrLost() {
    return currentState == State.ScanningForTarget;
  }
  
  public void logTargetPosition(ScannedRobotEvent e) {
    targetPositionLog.add(new PositionTuple(robot.pointAtBearing(e.getBearingRadians(), e.getDistance()),
                                            e.getHeadingRadians(),
                                            e.getVelocity(),
                                            e.getTime()));
  }
  
  public void trackTarget(double targetBearing) {
    double targetHeading = robot.currentAbsoluteHeading() + targetBearing;
    double radarBearingToTarget = targetHeading - robot.getRadarHeadingRadians();
    robot.setTurnRadarRightRadians(radarScanWidthMultiplier * Utils.normalRelativeAngle(radarBearingToTarget));
  }
  
  public Bullet tryToFireGun() {
    if (robot.getTime() == fireTime &&                       // target is acquired, and gun is aimed, so fire
        robot.getGunTurnRemainingRadians() == 0 && 
        robot.getGunHeat() == 0) {
      return fireGun();
    }
    return null;
  }
  
  // this method tries to figure out where the enemy robot will be in the future, aims at that position, then fires.
  public void aimGun() {
    if (targetPositionLog.size() > 0) {
      ArrayList<Double> mostRecentEnemyHeadings = new ArrayList<Double>();
      for(int i = Math.max(targetPositionLog.size() - numberOfObservationsToAverage, 0); i < targetPositionLog.size(); i++) {
        mostRecentEnemyHeadings.add(Double.valueOf(targetPositionLog.get(i).heading));
      }
      double averageHeadingOfBadGuy = averageAngles(mostRecentEnemyHeadings);
      
      // assuming that the bad guy stays at his average heading, then we compute his future position as follows.
      PositionTuple lastKnownPositionInfo = targetPositionLog.get(targetPositionLog.size() - 1);
      
      Double gunHeading = targetingModel.target(lastKnownPositionInfo.position, averageHeadingOfBadGuy, lastKnownPositionInfo.velocity, firePower);
      if(gunHeading != null /*&& robot.getGunTurnRemainingRadians() == 0*/) {
        aimAndFireAtHeading(gunHeading);
      }
    }
  }
  
  public void aimAndFireAtRobotBearing(double bearing) {
    double heading = robot.currentAbsoluteHeading() + bearing;
    aimAndFireAtHeading(heading);
  }
  
  public void aimAndFireAtHeading(double heading) {
    double gunBearing = heading - robot.getGunHeadingRadians();
    aimAndFireAtGunBearing(gunBearing);
  }
  
  public void aimAndFireAtGunBearing(double bearing) {
    robot.setTurnGunRightRadians(Utils.normalRelativeAngle(bearing));
    // Don't need to check whether gun turn will complete in single turn because
    // we check that gun is finished turning before calling setFire(...).
    // This is simpler since the precise angle your gun can move in one tick
    // depends on where your robot is turning.
    fireTime = robot.getTime() + 1;
  }
  
  public void acquireTarget(String robotName) {
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
  
  // found this formula at: http://stackoverflow.com/questions/491738/how-do-you-calculate-the-average-of-a-set-of-angles
  public double averageAngles(ArrayList<Double> anglesInRadians) {
    double sumOfSines = 0;
    double sumOfCosines = 0;
    for (Double angle : anglesInRadians) {
      sumOfCosines += Math.cos(angle);
      sumOfSines += Math.sin(angle);
    }
    
    // The line below would be atan2(sumOfCosines, sumOfSines) if the signature of atan2 were atan2(x, y), 
    // but Java's atan2 method signature is atan2(y, x), so we have to call the function with the 
    // arguments reversed: atan2(sumOfSines, sumOfCosines).
    return Math.atan2(sumOfSines, sumOfCosines);
  }
}
