package dke;

import java.awt.geom.Point2D;

public class MoveInAStraightLine implements MovementInstruction {
  public enum State {
    Initial, Turning, Driving, Finished
  }
  
  public DkeRobot robot;
  public Point2D.Double destination;
  public State currentState;
  
  public MoveInAStraightLine(DkeRobot robot, Point2D.Double destination) {
    this.robot = robot;
    this.destination = destination;
    currentState = State.Initial;
  }
  
  @Override
  public boolean isComplete() {
    return currentState == State.Finished;
  }
  
  @Override
  public void move() {
    switch(currentState) {
    case Initial:
      turnToDestination();
      break;
    case Turning:
      if(!robot.isTurning()) {
        driveToDestination();
      }
      break;
    case Driving:
      if(!robot.isTraveling()) {
        currentState = State.Finished;
      }
      break;
    case Finished:
      break;
    }
  }
  
  public void turnToDestination() {
    double bearingToDestinationInRadians = robot.bearingToPoint(destination);
    
    // turn to the destination point, if necessary
    if(bearingToDestinationInRadians != 0.0) {
      robot.setTurnRightRadians(bearingToDestinationInRadians);
      currentState = State.Turning;
    } else {
      driveToDestination();
    }
  }
  
  public void driveToDestination() {
    // drive toward the destination point
    robot.setAhead(robot.currentCoords().distance(destination));
    currentState = State.Driving;
  }
}
