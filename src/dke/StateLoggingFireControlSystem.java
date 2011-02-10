package dke;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;

import robocode.Bullet;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class StateLoggingFireControlSystem implements FireControlSystem {
  public enum State {
    Initial, ScanningForTarget, TargetAquired
  }

  public DkeRobot robot;
  public State currentState;

  public long timeTargetLastSeen;
  public long timeLastShotFired;
  public long fireTime;
  public double firePower;
  public double radarScanWidthMultiplier;
  public String currentTarget;
  public MovementModel movementModel;
  public TargetingModel targetingModel;

  public StateLoggingFireControlSystem(DkeRobot robot) {
    this.robot = robot;
//    targetingModel = new LinearTargetingModel(robot, 2);
    movementModel = new KNNMovementModel(robot, 1);
    targetingModel = new KNNTargetingModel(robot);
    currentState = State.Initial;
    fireTime = 10000;
    firePower = 2.001;
    radarScanWidthMultiplier = 1.7;
    timeTargetLastSeen = 0;
    timeLastShotFired = 0;
    currentTarget = null;
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
    String enemyRobotName = e.getName();
    
    // acquire a target if we don't currently have one
    if(currentTarget == null || isCurrentTargetDeadOrLost()) {
      acquireTarget(enemyRobotName);
    }
    
    // if we've just scanned the acquired target, log the enemy position information, then target them, and fire.
    if(enemyRobotName.equals(currentTarget)) {
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
    String enemyRobotName = e.getName();
    
    RobotStateTuple enemyRobotState = new RobotStateTuple(robot.pointAtBearing(e.getBearingRadians(), e.getDistance()),
                                                          e.getHeadingRadians(),
                                                          e.getVelocity());
    RobotStateTuple selfRobotState = new RobotStateTuple(robot.currentCoords(),
                                                         robot.currentAbsoluteHeading(),
                                                         robot.getVelocity(),
                                                         e.getTime() - timeLastShotFired);
    EnvironmentStateTuple env = new EnvironmentStateTuple(enemyRobotState, selfRobotState, e.getTime());
    movementModel.logStateObservation(enemyRobotName, env);
  }
  
  public Bullet tryToFireGun() {
    if (robot.getTime() == fireTime &&                       // target is acquired, and gun is aimed, so fire
        robot.getGunTurnRemainingRadians() == 0 && 
        robot.getGunHeat() == 0) {
      return fireGun();
    }
    return null;
  }
  
  public void trackTarget(double targetBearing) {
    double targetHeading = robot.currentAbsoluteHeading() + targetBearing;
    double radarBearingToTarget = targetHeading - robot.getRadarHeadingRadians();
    robot.setTurnRadarRightRadians(radarScanWidthMultiplier * Utils.normalRelativeAngle(radarBearingToTarget));
  }
  
  // this method tries to figure out where the enemy robot will be in the future, aims at that position, then fires.
  public void aimGun() {
    EnvironmentStateSequence stateSeq = movementModel.getStateSequence(currentTarget);
    if(stateSeq != null && stateSeq.last() != null) {
      setFirepower(robot.currentCoords().distance(stateSeq.last().enemyRobot.position));
      
      Double gunHeading = targetingModel.target(currentTarget, movementModel, firePower);
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
    timeLastShotFired = robot.getTime();
    return robot.setFireBullet(firePower);
  }
  
  public void setFirepower(double distanceToEnemy) {
    if(distanceToEnemy < 200) {
      firePower = 3.0;
    } else if (firePower < 400) {
      firePower = 2.0;
    } else {
      firePower = 1.01;
    }
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
