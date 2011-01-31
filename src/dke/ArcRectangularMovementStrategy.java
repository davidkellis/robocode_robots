package dke;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;

public class ArcRectangularMovementStrategy implements MovementStrategy {
  public enum State {
    Initial, DrivingNorth, DrivingEast, DrivingSouth, DrivingWest
  }
  
  public Random rand;
  public DkeRobot robot;
  public MovementInstruction lastMovement;
  public boolean randomizeVelocity;
  public boolean collidedWithWall;
  public boolean collidedWithRobot;
  public double wallBufferWidth = 100;
  public State currentState;
  public MovementInstruction currentMovementInstruction;

  public ArcRectangularMovementStrategy(DkeRobot robot) {
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
      currentMovementInstruction = robot.moveInAnArcBackward(pos.x, robot.northWall - wallBufferWidth, Direction.Right, 100);
      currentState = State.DrivingNorth;
      break;
    case South:
      currentMovementInstruction = robot.moveInAnArcBackward(pos.x, wallBufferWidth, Direction.Right, 100);
      currentState = State.DrivingSouth;
      break;
    case East:
      currentMovementInstruction = robot.moveInAnArc(robot.eastWall - wallBufferWidth, pos.y, Direction.Right, 100);
      currentState = State.DrivingEast;
      break;
    case West:
      currentMovementInstruction = robot.moveInAnArc(wallBufferWidth, pos.y, Direction.Right, 100);
      currentState = State.DrivingWest;
      break;
    }
    currentMovementInstruction.move();
  }
  
  public void driveToWallNearestCurrentHeading() {
    CardinalDirection dir = robot.cardinalDirectionNearestHeading(robot.currentAbsoluteHeading());
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

  public void onPaint(Graphics2D g) {
    try {
      Method m = currentMovementInstruction.getClass().getMethod("onPaint", new Class[]{Graphics2D.class});
      m.invoke(currentMovementInstruction, new Object[]{g});
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
    }
  }
}
