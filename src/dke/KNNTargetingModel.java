package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.Rules;

public class KNNTargetingModel implements TargetingModel {
  DkeRobot robot;
  public int numberOfPositionsToProjectIntoFuture;
  
  public KNNTargetingModel(DkeRobot robot) {
    this.robot = robot;
    this.numberOfPositionsToProjectIntoFuture = 25;
  }
  
  // This method returns the gun heading that the fire control system need to turn the gun to, and then fire.
  public Double target(String targetRobotName, MovementModel movementModel, double desiredFirepower) {
    // 3. project the historical enemy movements identified in step 2 onto the enemey's current state
    ArrayList<Point2D.Double> projectedEnemyPositions = movementModel.predictFutureMovement(targetRobotName, numberOfPositionsToProjectIntoFuture);
//    System.out.println(projectedEnemyPositions.size());
    
    // 4. identify where the enemy will be at the time my bullet is whizzing by.
    Point2D.Double projectedFutureEnemyCoordinates = estimateEnemyPositionInFuture(projectedEnemyPositions, desiredFirepower);
    
    if(projectedFutureEnemyCoordinates != null) {
      // 5. compute the gun heading of the enemy's future coordinates and return that heading
      double gunHeading = robot.headingToPoint(projectedFutureEnemyCoordinates);
      
      return gunHeading;
    }
    
    return null;
  }
  
  // projectedEnemyPositions is a list of coordinates that represents the enemy's projected position in the future.
  //   Assuming time t represents the game time in the current turn (i.e. t = NOW):
  //     The first set of coordinates (in position 0 of the return value) represents the projected enemy position in the very next step of the game, at time t+1 = t+1+0.
  //     The second set of coordinates (in position 1 of the return value) represents the projected enemy position two steps in the future, at time t+2 = t+1+1.
  //     The i-th set of coordinates (in position i-1 of the return value) represents the projected enemy position at time t+1+i.
  public Point2D.Double estimateEnemyPositionInFuture(ArrayList<Point2D.Double> projectedEnemyPositions, double desiredFirepower) {
    Point2D.Double currentPos = robot.currentCoords();
    
    double bulletVelocity = Rules.getBulletSpeed(desiredFirepower);
    
    for(int i = 0; i < projectedEnemyPositions.size(); i++) {
      // if the bullet would have traveled a distance greater than or equal to the distance from the enemy's projected position, fire at that projected position. 
      if(bulletVelocity * (i+1) >= currentPos.distance(projectedEnemyPositions.get(i))) {
        // TODO: If this doesn't work well, we should probably return a point somewhere between projected points i-1 and i.
        //       Since the bullet would have flown past projected point i, we can safely assume it should hit the robot
        //       at some point between projected points i-1 and i.
        return projectedEnemyPositions.get(i);
      }
    }
    
    if(projectedEnemyPositions.size() > 0) {
      return projectedEnemyPositions.get(projectedEnemyPositions.size() - 1);
    }
    
    return null;
  }
}
