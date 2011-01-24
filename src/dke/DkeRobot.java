package dke;

import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;

public class DkeRobot extends AdvancedRobot {
  public static double PI = Math.PI;
  public static double halfPI = Math.PI / 2;
  public static double quarterPI = Math.PI / 4;
  public static double twoPI = 2 * Math.PI;
  
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
    Point2D.Double currentPos = currentCoords();
    double rise = destinationPoint.x - currentPos.x;
    double run = destinationPoint.y - currentPos.y;
    double angleRelativeToAbsoluteZero = Math.atan2(rise, run);
    if(angleRelativeToAbsoluteZero < 0) {
      return angleRelativeToAbsoluteZero + twoPI;
    } else {
      return angleRelativeToAbsoluteZero;
    }
  }
  
  public double bearingToPoint(Point2D.Double destinationPoint) {
    double diff = headingToPoint(destinationPoint) - currentAbsoluteHeading();
    if(diff > PI) {
      return diff - twoPI;
    } else if(diff < -PI) {
      return diff + twoPI;
    } else {
      return diff;
    }
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

//  public boolean isNearWall(double bufferZoneWidth) {
//    Point2D.Double pos = currentCoords();
//    if (pos.x < bufferZoneWidth) {
//      return true;
//    } else if (pos.x > (eastWall - bufferZoneWidth)) {
//      return true;
//    } else if (pos.y < bufferZoneWidth) {
//      return true;
//    } else if (pos.y > (northWall - bufferZoneWidth)) {
//      return true;
//    }
//    return false;
//  }

  public Point2D.Double currentCoords() {
    return new Point2D.Double(getX(), getY());
  }

  // points the robot toward the cardinal direction that most closely
  // represents the direction of the given absolute heading
//  public void turnToCardinalDirectionNearestHeading(double absHeading) {
//    double headingRemainder = absHeading % halfPI;
//    if (headingRemainder < quarterPI) { // turn left by headingRemainder radians
//      setTurnLeftRadians(headingRemainder);
//    } else { // turn right by (halfPI - headingRemainder) radians
//      setTurnRightRadians(halfPI - headingRemainder);
//    }
//    lastMovement = MovementInstruction.Turn;
//  }

  public CardinalDirection cardinalDirectionNearestHeading(double absHeading) {
    int headingQuotient = (int) Math.floor(absHeading / halfPI);
    double headingRemainder = absHeading % halfPI;
    if (headingRemainder < quarterPI) {
      switch (headingQuotient) {
      case 0:
        return CardinalDirection.North;
      case 1:
        return CardinalDirection.East;
      case 2:
        return CardinalDirection.South;
      case 3:
        return CardinalDirection.West;
      }
    } else {
      switch (headingQuotient) {
      case 0:
        return CardinalDirection.East;
      case 1:
        return CardinalDirection.South;
      case 2:
        return CardinalDirection.West;
      case 3:
        return CardinalDirection.North;
      }
    }
    return null;
  }
  
  public MoveInAStraightLine moveInAStraightLine(double x, double y) {
    return new MoveInAStraightLine(this, new Point2D.Double(x, y));
  }
}
