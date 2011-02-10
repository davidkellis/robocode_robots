package dke;

import java.awt.geom.Point2D;
import java.util.Random;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;

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
  public boolean collidedWithWall;
  public boolean collidedWithRobot;
  public MovementInstruction currentMovementInstruction;

  public RectangularMovementStrategy(DkeRobot robot) {
    this.robot = robot;
    
    rand = new Random();
    randomizeVelocity = false;
    currentState = State.Initial;
    collidedWithWall = false;
    collidedWithRobot = false;
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
    if(currentMovementInstruction.isComplete() ||
       collidedWithRobot ||
       collidedWithWall) {
      resetCollisionFlags();
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
    CardinalDirection dir = Utils.cardinalDirectionNearestHeading(robot.currentAbsoluteHeading());
    driveToWall(dir);
  }
  
  public void onHitWall(HitWallEvent e) {
    collidedWithWall = true;
    robot.setMaxTurnRateRadians(Rules.MAX_TURN_RATE_RADIANS);
    robot.setMaxVelocity(Rules.MAX_VELOCITY);
    moveRobot();
  }
  public void onHitRobot(HitRobotEvent e) {
    collidedWithRobot = true;
    robot.setMaxTurnRateRadians(Rules.MAX_TURN_RATE_RADIANS);
    robot.setMaxVelocity(Rules.MAX_VELOCITY);
    moveRobot();
  }
  public void onHitByBullet(HitByBulletEvent e) {
  }

  public void resetCollisionFlags() {
    collidedWithWall = false;
    collidedWithRobot = false;
  }
}
