package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.Rules;

public class LinearTargetingModel implements TargetingModel {
  DkeRobot robot;
  public int numberOfObservationsToAverage;
  
  public LinearTargetingModel(DkeRobot robot, int numberOfObservationsToAverage) {
    this.robot = robot;
    this.numberOfObservationsToAverage = numberOfObservationsToAverage;
  }
  
  // This method was taken verbatim from http://robowiki.net/wiki/Linear_Targeting
  // This method returns the gun heading that the fire control system need to turn the gun to, and then fire.
  @Override
  public Double target(EnvironmentStateSequence eseq, double desiredFirepower) {
    ArrayList<Double> mostRecentEnemyHeadings = eseq.getEnemyHeadings(numberOfObservationsToAverage);
    RobotStateTuple lastKnown = eseq.lastEnemyRobotState();
    Point2D.Double enemyPosition = lastKnown.position;
    
    double enemyRobotHeading = averageAngles(mostRecentEnemyHeadings);
    
    // compute the bad guy's average velocity
    double enemyVelocity = 0;
    for(double v : eseq.getEnemyVelocities(numberOfObservationsToAverage)) {
      enemyVelocity += v;
    }
    enemyVelocity /= numberOfObservationsToAverage;
    
    // Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to robot
    final double rX = robot.getX(),
                 rY = robot.getY(),
                 bV = Rules.getBulletSpeed(desiredFirepower);
    final double eX = enemyPosition.getX(),
                 eY = enemyPosition.getY(),
                 eV = enemyVelocity;
    
    // These constants make calculating the quadratic coefficients below easier
    final double A = (eX - rX) / bV;
    final double B = (eV / bV) * Math.sin(enemyRobotHeading);
    final double C = (eY - rY) / bV;
    final double D = (eV / bV) * Math.cos(enemyRobotHeading);
    
    // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
    final double a = A*A + C*C;
    final double b = 2 * (A*B + C*D);
    final double c = (B*B + D*D - 1);
    final double discrim = b*b - 4*a*c;
    
    if (discrim >= 0) {                     // discriminant must be >= 0 since Math.sqrt(<negative number>) is undefined in the Reals
      // Reciprocal of quadratic formula
      final double t1 = 2*a/(-b - Math.sqrt(discrim));
      final double t2 = 2*a/(-b + Math.sqrt(discrim));
      final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
      
      // Assume enemy stops at walls
      double halfRobotWidth = DkeRobot.ROBOT_WIDTH / 2;
      double halfRobotHeight = DkeRobot.ROBOT_HEIGHT / 2;
      final double endX = limit(eX + eV * t * Math.sin(enemyRobotHeading),
                                halfRobotWidth,
                                robot.eastWall - halfRobotWidth);
      final double endY = limit(eY + eV*t*Math.cos(enemyRobotHeading),
                                halfRobotHeight,
                                robot.northWall - halfRobotHeight);
      double gunBearingRelativeToAbsoluteZero = Math.atan2(endX - rX, endY - rY);     // this is a bearing: -PI <= angle < PI
      if(gunBearingRelativeToAbsoluteZero < 0) {
        return Double.valueOf(gunBearingRelativeToAbsoluteZero + DkeRobot.twoPI);        // return an absolute heading
      } else {
        return Double.valueOf(gunBearingRelativeToAbsoluteZero);                      // return an absolute heading
      }
    }
    return null;
  }
  
  public double limit(double value, double min, double max) {
    return Math.min(max, Math.max(min, value));
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
