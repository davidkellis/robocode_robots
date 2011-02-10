package dke;

import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Rules;

public class DkeRobot extends AdvancedRobot {
  public static double ROBOT_WIDTH = 16;
  public static double ROBOT_HEIGHT = 16;
  
  public double eastWall;
  public double northWall;
  
  public void initialize() {
    eastWall = getBattleFieldWidth();
    northWall = getBattleFieldHeight();

    setAdjustGunForRobotTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    setMaxVelocity(Rules.MAX_VELOCITY);
  }
  
  public boolean isMoving() {
    return isTurning() || isTraveling(); 
  }
  
  public boolean isTurning() {
    return getTurnRemaining() != 0.0;
  }

  public boolean isTraveling() {
    return getDistanceRemaining() != 0.0;
  }

  public boolean isGunTurning() {
    return getGunTurnRemaining() != 0.0;
  }

  public boolean isRadarTurning() {
    return getRadarTurnRemaining() != 0.0;
  }

  public double currentAbsoluteHeading() {
    return getHeadingRadians();
  }
  
  // atan2(y, x) is the angle in radians between the positive x-axis of a plane and the point given by the coordinates (x, y) on it.
  // The x-axis in robocode points straight up, which is what we normally consider the y-axis. So, our computation of rise and run
  // must take into account our coordinate system and clockwise direction convention.
  public double headingToPoint(Point2D.Double destinationPoint) {
    return Utils.headingToPoint(destinationPoint, currentCoords());
  }

  // Returns a rotation amount in Radians.
  //   A positive return value indicates that the robot should turn right.
  //   A negative return value indicates that the robot should turn left.
  public double bearingToPoint(Point2D.Double destinationPoint) {
    return bearingToPoint(destinationPoint, currentCoords());
  }
  
  public double bearingToPoint(Point2D.Double destinationPoint, Point2D.Double originPoint) {
    return Utils.bearingToPoint(destinationPoint, originPoint, currentAbsoluteHeading());
  }

  public Point2D.Double pointAtHeading(double heading, double distance) {
    return Utils.pointAtHeading(heading, distance, currentCoords());
  }
  
  public Point2D.Double pointAtBearing(double bearing, double distance) {
    return pointAtHeading(currentAbsoluteHeading() + bearing, distance);
  }

  public double distanceFromWall(CardinalDirection directionOfWall) {
    Point2D.Double pos = currentCoords();
    switch (directionOfWall) {
    case North:
      return northWall - pos.y;
    case East:
      return eastWall - pos.x;
    case South:
      return pos.y;
    case West:
      return pos.x;
    }
    return 0.0;
  }

  public Point2D.Double currentCoords() {
    return new Point2D.Double(getX(), getY());
  }

  public MoveInAStraightLine moveInAStraightLine(double x, double y) {
    return new MoveInAStraightLine(this, new Point2D.Double(x, y));
  }
  
  public MoveInAnArc moveInAnArc(double x, double y, Direction directionOfConcavity, double sagittaLength) {
    return new MoveInAnArc(this, new Point2D.Double(x, y), directionOfConcavity, sagittaLength);
  }

  public MoveInAnArcBackward moveInAnArcBackward(double x, double y, Direction directionOfConcavity, double sagittaLength) {
    return new MoveInAnArcBackward(this, new Point2D.Double(x, y), directionOfConcavity, sagittaLength);
  }
  
  public void setMaxTurnRateRadians(double newMaxTurnRateInRadians) {
    setMaxTurnRate(Math.toDegrees(newMaxTurnRateInRadians));
  }
}
