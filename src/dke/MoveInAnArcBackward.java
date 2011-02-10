package dke;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.Rules;

public class MoveInAnArcBackward implements MovementInstruction {
  public enum State {
    Initial, TurningToInitialAngle, Driving, Finished
  }
  
  public DkeRobot robot;
  public Point2D.Double destination;
  public State currentState;
  public Direction directionOfConcavity;
  public double sagittaLength;
  
  public double circleRadius;
  public double halfOfSectorAngle;
  public double fullSectorAngle;
  public double arcLength;
  
  // debug
  public Point2D.Double origin;
  Point2D.Double pointOnTangentLine;
  
  /**
   * @param robot is a DkeRobot
   * @param destination is a Point2D.Double representing the point on the board that the robot should travel to.
   * @param directionOfConcavity is a direction, either left or right. Direction.Left indicates that
   *                             arc will be formed concave left (i.e. the arc will bulge on the left side of the curve).
   *                             Direction.Right indicates taht the arc will be concave right (i.e. the arc will bulge
   *                             on the right side of the curve).
   * @param sagittaLength is the length of the sagitta in pixels. See http://mathworld.wolfram.com/Sagitta.html.
   *                      Note: sagittaLength <= half the distance between the robot and the destination point.
   */
  public MoveInAnArcBackward(DkeRobot robot, Point2D.Double destination, Direction directionOfConcavity, double sagittaLength) {
    this.robot = robot;
    this.destination = destination;
    this.origin = robot.currentCoords();
    this.directionOfConcavity = directionOfConcavity;
    double chordLength = robot.currentCoords().distance(destination);
    if(sagittaLength > chordLength / 2.0) {
      this.sagittaLength = chordLength / 2.0;
    } else {
      this.sagittaLength = sagittaLength;
    }
    computeArcPath();
    currentState = State.Initial;
  }
  
  public void computeArcPath() {
    computeRotationAmount();      // this must be done before computeArcLength(), as the arc length computation uses the rotation amount.
    computeArcLength();
  }
  
  public void computeRotationAmount() {
    double lengthOfChord = robot.currentCoords().distance(destination);
    circleRadius = Math.pow(lengthOfChord, 2) / (8 * sagittaLength) + sagittaLength / 2;
    double apothemLength = circleRadius - sagittaLength;
    halfOfSectorAngle = Math.acos(apothemLength / circleRadius);
    fullSectorAngle = 2 * halfOfSectorAngle;
  }
  
  public void computeArcLength() {
    arcLength = fullSectorAngle * circleRadius;
  }
  
  @Override
  public boolean isComplete() {
    return currentState == State.Finished;
  }
  
  @Override
  public void move() {
    switch(currentState) {
    case Initial:
      turnToInitialAngle();
      break;
    case TurningToInitialAngle:
      if(!robot.isTurning()) {
        driveToDestination();
      }
      break;
    case Driving:
      updateMaximumAngularAndBackwardVelocity();
      if(!robot.isTraveling() && !robot.isTurning()) {
        // reset the max turn velocity and forward velocity
        robot.setMaxTurnRate(Rules.MAX_TURN_RATE);
        robot.setMaxVelocity(Rules.MAX_VELOCITY);
        
        currentState = State.Finished;
      }
      break;
    case Finished:
      break;
    }
  }
  
  public void turnToInitialAngle() {
    double bearingToDestinationInRadians = robot.bearingToPoint(destination);
//    double angleBetweenChordAndTangentLine = (robot.halfPI - (robot.halfPI - halfOfSectorAngle));
    double angleBetweenChordAndTangentLine = halfOfSectorAngle;
    double bearingToStartOfTurnAngle;
    if(directionOfConcavity == Direction.Left) {
      bearingToStartOfTurnAngle = robocode.util.Utils.normalRelativeAngle(bearingToDestinationInRadians - angleBetweenChordAndTangentLine + Math.PI);
    } else {
      bearingToStartOfTurnAngle = robocode.util.Utils.normalRelativeAngle(bearingToDestinationInRadians + angleBetweenChordAndTangentLine + Math.PI);
    }

    // DEBUG LINE BELOW!
    pointOnTangentLine = Utils.pointAtBearing(bearingToStartOfTurnAngle, 50, robot.currentCoords(), robot.currentAbsoluteHeading());
    
    // turn the robot so that it is facing the direction of the start of the turn (if necessary)
    if(bearingToStartOfTurnAngle != 0.0) {
      robot.setTurnRightRadians(bearingToStartOfTurnAngle);
      currentState = State.TurningToInitialAngle;
    } else {
      driveToDestination();
    }
  }
  
  public void driveToDestination() {
    // drive toward the destination point on the arc.
    robot.setBack(arcLength);
    if(directionOfConcavity == Direction.Left) {    // concave left
      robot.setTurnRightRadians(fullSectorAngle);
    } else {                                        // concave right
      robot.setTurnLeftRadians(fullSectorAngle);
    }
    updateMaximumAngularAndBackwardVelocity();
    currentState = State.Driving;
  }
  
  // This method sets the max angular velocity and the max forward velocity so that the ratio
  //   of ahead-velocity to angular-velocity is equal to the ratio of arcLength to rotationAmount.
  // This is done to ensure that as the robot accelerates or decelerates, it continues to
  //   travel along the path of the arc.
  // A side effect of this behavior is that we can only modulate the robot's forward velocity (and only sometimes), as
  //   its angular velocity will be automatically modulated so that the the ratio
  //   of ahead-velocity to angular-velocity is equal to the ratio of arcLength to rotationAmount.
  public void updateMaximumAngularAndBackwardVelocity() {
    double currentVelocity = robot.getVelocity();
    
    double desiredVelocityNextTurn = Math.max(currentVelocity - Rules.ACCELERATION, -Rules.MAX_VELOCITY);
    double desiredAngularVelocityNextTurn = (fullSectorAngle * Math.abs(desiredVelocityNextTurn)) / arcLength;
    
    double maximumRateOfRotationInDegrees = 10 - 0.75 * Math.abs(desiredVelocityNextTurn);
    double maximumRateOfRotationInRadians = Math.toRadians(maximumRateOfRotationInDegrees);
    
    // if we want to make too tight of a turn (by Robocode Physics rules), then we need to slow down, instead of turn harder.
    if(desiredAngularVelocityNextTurn > maximumRateOfRotationInRadians) {
      // slow down and turn at maximum angular velocity
      // we compute the maximum forward velocity like this:
      // (rotationAmount * V) / arcLength <= (PI/180)(10 - 0.75 * abs(V))
      // and then solve for V: V = (10 * Math.PI * arcLength) / ((180 * rotationAmount) + (Math.PI * arcLength * 0.75))
      double maximumBackwardVelocityToMaintainTurn = (10 * Math.PI * arcLength) / ((180 * fullSectorAngle) + (Math.PI * arcLength * 0.75));
      double angularVelocityToMaintainTurn = (fullSectorAngle * maximumBackwardVelocityToMaintainTurn) / arcLength;
      robot.setMaxVelocity(maximumBackwardVelocityToMaintainTurn);
      robot.setMaxTurnRateRadians(angularVelocityToMaintainTurn);
    } else {
      // we get to use our desired maximum angular velocity and forward velocity!
      robot.setMaxVelocity(desiredVelocityNextTurn);
      robot.setMaxTurnRateRadians(desiredAngularVelocityNextTurn);
    }
  }
  
  public void onPaint(Graphics2D g) {
//    g.drawOval(x, y, 2 * circleRadius, 2 * circleRadius);
    int x = (int)robot.getX();
    int y = (int)robot.getY();
    Point2D.Double pt;
    
    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));      // red
    pt = robot.pointAtBearing(0, 50);
//    pt = robot.pointAtHeading(robot.currentAbsoluteHeading(), 50);
    g.drawLine(x, y, (int)pt.x, (int)pt.y);
//    g.drawLine(x, y, x, y+50);
    
    g.setColor(new Color(0x00, 0xff, 0x00, 0x80));      // green
    pt = robot.pointAtBearing(Utils.halfPI, 50);
//    pt = robot.pointAtHeading(robot.currentAbsoluteHeading() + robot.halfPI, 50);
    g.drawLine(x, y, (int)pt.x, (int)pt.y);
//    g.drawLine(x, y, x+50, y);

    g.setColor(new Color(0x00, 0x00, 0xff, 0x80));      // blue
    pt = robot.pointAtBearing(Utils.PI, 50);
//    pt = robot.pointAtHeading(robot.currentAbsoluteHeading() + robot.PI, 50);
    g.drawLine(x, y, (int)pt.x, (int)pt.y);
//    g.drawLine(x, y, x, y-50);

    g.setColor(new Color(0xff, 0xff, 0x00, 0x80));      // yellow
    pt = robot.pointAtBearing(Utils.threePIoverTwo, 50);
//    pt = robot.pointAtHeading(robot.currentAbsoluteHeading() + robot.threePIoverTwo, 50);
    g.drawLine(x, y, (int)pt.x, (int)pt.y);
//    g.drawLine(x, y, x-50, y);

    g.setColor(new Color(0x00, 0xff, 0xff, 0x80));      // cyan
    g.drawLine((int)origin.x, (int)origin.y, (int)pointOnTangentLine.getX(), (int)pointOnTangentLine.getY());

    g.setColor(new Color(0xff, 0x45, 0x00, 0x80));      // orange
    g.drawLine(x, y, (int)destination.getX(), (int)destination.getY());
  }
}
