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
  public static double threePIoverTwo = 3 * Math.PI / 2.0;
  
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
  
  // Returns a rotation amount in Radians.
  //   A positive return value indicates that the robot should turn right.
  //   A negative return value indicates that the robot should turn left.
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
  
  
  public Point2D.Double pointAtHeading(double heading, double distance) {
    return pointAtHeading(heading, distance, currentCoords());
  }
  
  public Point2D.Double pointAtHeading(double heading, double distance, Point2D.Double originPosition) {
    heading = heading % twoPI;     // normalize heading to something in the range: 0 <= heading < 2*PI
    double x, y;
    
    // domain of Tangent function is undefined at 90, 270
    if(heading == halfPI) {                 // heading is 90, move right
      x = originPosition.x + distance;
      y = originPosition.y;
    } else if (heading == threePIoverTwo) { // heading is 270, move left
      x = originPosition.x - distance;
      y = originPosition.y;
    } else {                                // domain of Tangent function is defined at all other angles.
      double ratioOppOverAdj = Math.tan(heading);
      
      double adj = Math.sqrt(Math.pow(distance, 2) / (Math.pow(ratioOppOverAdj, 2) + 1));
      double opp = Math.sqrt(Math.pow(distance, 2) - Math.pow(adj, 2));
      
      // ****** NOTE ******
      // The adjacent length, adj, represents the change in Y
      // The opposite length, opp, represents the change in X
      
      if(heading > PI) {                    // destination point is left of the bot's current position
        if(heading > threePIoverTwo) {      // destination point is above the bot's current position
          x = originPosition.x - opp;
          y = originPosition.y + adj;
        } else {                            // destination point is below the bot's current position
          x = originPosition.x - opp;
          y = originPosition.y - adj;
        }
      } else {                              // destination point will be to the right of the bot's current position
        if(heading < halfPI) {              // destination point is above the bot's current position
          x = originPosition.x + opp;
          y = originPosition.y + adj;
        } else {                            // destination point is below the bot's current position
          x = originPosition.x + opp;
          y = originPosition.y - adj;
        }
      }
    }
    return new Point2D.Double(x, y);
  }
  
  public Point2D.Double pointAtBearing(double bearing, double distance) {
    return pointAtHeading(getHeading() + bearing, distance);
  }

  public Point2D.Double pointAtBearing(double bearing, double distance, Point2D.Double originPosition, double originHeading) {
    return pointAtHeading(originHeading + bearing, distance, originPosition);
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
  
  public MoveInAnArc moveInAnArc(double x, double y, Direction directionOfConcavity, double sagittaLength) {
    return new MoveInAnArc(this, new Point2D.Double(x, y), directionOfConcavity, sagittaLength);
  }
  
  public void setMaxTurnRateRadians(double newMaxTurnRateInRadians) {
    setMaxTurnRate(Math.toDegrees(newMaxTurnRateInRadians));
  }
}
