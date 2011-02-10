package dke;

import java.awt.geom.Point2D;

public class Utils {
  public static double PI = Math.PI;
  public static double halfPI = Math.PI / 2;
  public static double quarterPI = Math.PI / 4;
  public static double twoPI = 2 * Math.PI;
  public static double threePIoverTwo = 3 * Math.PI / 2.0;

  public static double headingToPoint(Point2D.Double destinationPoint, Point2D.Double originPoint) {
    double rise = destinationPoint.x - originPoint.x;
    double run = destinationPoint.y - originPoint.y;
    double angleRelativeToAbsoluteZero = Math.atan2(rise, run); // this is a bearing: -PI <= angle < PI
    if (angleRelativeToAbsoluteZero < 0) {
      return angleRelativeToAbsoluteZero + twoPI;
    } else {
      return angleRelativeToAbsoluteZero;
    }
  }
  
  public static double bearingToPoint(Point2D.Double destinationPoint, Point2D.Double originPoint, double originHeading) {
    double diff = headingToPoint(destinationPoint, originPoint) - originHeading;
    if(diff > PI) {
      return diff - twoPI;
    } else if(diff < -PI) {
      return diff + twoPI;
    } else {
      return diff;
    }
  }
  
  public static Point2D.Double pointAtHeading(double heading, double distance, Point2D.Double originPosition) {
    double pX = originPosition.getX() + distance * Math.sin(heading);
    double pY = originPosition.getY() + distance * Math.cos(heading);
    return new Point2D.Double(pX, pY);
  }
  
  public static Point2D.Double pointAtBearing(double bearing, double distance, Point2D.Double originPosition, double originHeading) {
    return pointAtHeading(originHeading + bearing, distance, originPosition);
  }
  
  public static CardinalDirection cardinalDirectionNearestHeading(double absHeading) {
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
}
