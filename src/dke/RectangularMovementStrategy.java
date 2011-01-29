package dke;

import java.awt.geom.Point2D;
import java.util.Random;

public class RectangularMovementStrategy implements MovementStrategy {
  public enum State {
    Initial, DrivingNorth, DrivingEast, DrivingSouth, DrivingWest
  }
  
  public Random rand;
  public DkeRobot robot;
  public MovementInstruction lastMovement;
  public boolean randomizeVelocity;
  public double wallBufferWidth = 150;
  public State currentState;
  public MovementInstruction currentMovementInstruction;

  public RectangularMovementStrategy(DkeRobot robot) {
    this.robot = robot;
    
    rand = new Random();
    randomizeVelocity = false;
    currentState = State.Initial;
  }
  
  public void moveRobot() {
    switch(currentState) {
    case Initial:
      driveToWallNearestCurrentHeading();
      break;
    case DrivingNorth:
      tryToDriveToWall(CardinalDirection.East);
      break;
    case DrivingEast:
      tryToDriveToWall(CardinalDirection.South);
      break;
    case DrivingSouth:
      tryToDriveToWall(CardinalDirection.West);
      break;
    case DrivingWest:
      tryToDriveToWall(CardinalDirection.North);
      break;
    }
  }
  
  public void tryToDriveToWall(CardinalDirection dir) {
    if(currentMovementInstruction.isComplete()) {
      driveToWall(dir);
    } else {
      currentMovementInstruction.move();
    }
  }
  
  public void driveToWall(CardinalDirection dir) {
    Point2D.Double pos = robot.currentCoords();
    switch(dir) {
    case North:
      currentMovementInstruction = robot.moveInAStraightLine(pos.x, robot.northWall - wallBufferWidth);
      currentState = State.DrivingNorth;
      break;
    case South:
      currentMovementInstruction = robot.moveInAStraightLine(pos.x, wallBufferWidth);
      currentState = State.DrivingSouth;
      break;
    case East:
      currentMovementInstruction = robot.moveInAStraightLine(robot.eastWall - wallBufferWidth, pos.y);
      currentState = State.DrivingEast;
      break;
    case West:
      currentMovementInstruction = robot.moveInAStraightLine(wallBufferWidth, pos.y);
      currentState = State.DrivingWest;
      break;
    }
    currentMovementInstruction.move();
  }
  
  public void driveToWallNearestCurrentHeading() {
    CardinalDirection dir = robot.cardinalDirectionNearestHeading(robot.currentAbsoluteHeading());
    driveToWall(dir);
  }
}