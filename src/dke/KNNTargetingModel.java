package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import robocode.Rules;

public class KNNTargetingModel implements TargetingModel {
  DkeRobot robot;
  public int k;
  public int numberOfStatesToProjectIntoFuture;
  
  public KNNTargetingModel(DkeRobot robot, int kNumberOfNeighbors) {
    this.robot = robot;
    this.k = kNumberOfNeighbors;
    this.numberOfStatesToProjectIntoFuture = 15;
  }
  
  // Returns the index of the EnvironmentStateTuple that represents the nearest neighbor of 
  // the current environment state tuple (i.e. last/most recent environment state tuple) 
  public int identifyNearestNeighbor(EnvironmentStateSequence eseq, int numberOfStatesToProjectIntoFuture) {
    EnvironmentStateTuple currentState = eseq.last();
    
    int indexOfNearestNeighbor = 0;
    double minDistance = Double.MAX_VALUE;
    double distance = 0;
    
    // find the historical state that has the smallest Euclidean distance from the current state (this is the 1-nearest-neighbor; k=1).
    int upperBound = eseq.size() - numberOfStatesToProjectIntoFuture;
    for(int i = 0; i < upperBound; i++) {
      distance = currentState.euclideanDistance(eseq.get(i));
      if(distance < minDistance) {
        indexOfNearestNeighbor = i;
        minDistance = distance;
      }
    }
    
    return indexOfNearestNeighbor;
  }
  
  // Returns a list of coordinates that represents the enemy's projected position in the future.
  //   Assuming time t represents the game time in the current turn (i.e. t = NOW):
  //     The first set of coordinates (in position 0 of the return value) represents the projected enemy position in the very next step of the game, at time t+1 = t+1+0.
  //     The second set of coordinates (in position 1 of the return value) represents the projected enemy position two steps in the future, at time t+2 = t+1+1.
  //     The i-th set of coordinates (in position i-1 of the return value) represents the projected enemy position at time t+1+i.
  //
  // projectedFutureStates is a list of EnvironmentStateTuples that represent historical movement/position info. that we believe (hope) matches the enemy robot's next few steps.
  //   The 0th element in projectedFutureStates represents the historical EnvironmentStateTuple that is the nearest neighbor of the current environment state tuple.
  //   The 1st element through the last element in projectedFutureStates represents the historical enemy states that we use to "play out" from the current state.
  public ArrayList<Point2D.Double> projectEnemyFuturePositions(EnvironmentStateTuple currentState, List<EnvironmentStateTuple> projectedFutureStates) {
    ArrayList<Point2D.Double> projectedPositions = new ArrayList<Point2D.Double>();
    Point2D.Double currentEnemyPosition = currentState.enemyRobot.position;
    Point2D.Double prevPos, nextPos;
    double distanceToNextPosition;
    double headingToNextPosition;
    
    for(int i = 1; i < projectedFutureStates.size(); i++) {   // start at index 1 because the first element is the nearest neighbor to the current state (the most recent state).
      prevPos = projectedFutureStates.get(i - 1).enemyRobot.position;
      nextPos = projectedFutureStates.get(i).enemyRobot.position;
      
      distanceToNextPosition = prevPos.distance(nextPos);
      headingToNextPosition = robot.headingToPoint(nextPos, prevPos);
      
      currentEnemyPosition = robot.pointAtHeading(headingToNextPosition, distanceToNextPosition, currentEnemyPosition);
      
      projectedPositions.add(currentEnemyPosition);
    }
    
    return projectedPositions;
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
    
    return projectedEnemyPositions.get(projectedEnemyPositions.size() - 1);
  }
  
  // This method returns the gun heading that the fire control system need to turn the gun to, and then fire.
  @Override
  public Double target(EnvironmentStateSequence eseq, double desiredFirepower) {
    EnvironmentStateTuple currentState = eseq.last();
    
    // 1. find the environment state tuple that most closely resembles (i.e. is the nearest neighbor of) the current/most-recent one.
    int indexOfNearestNeighbor = identifyNearestNeighbor(eseq, numberOfStatesToProjectIntoFuture);

    // 2. identify the enemy robot states that immediately follow the environment state tuple found in step 1.
    List<EnvironmentStateTuple> historicalStatesWithWhichToPredictFuture = eseq.slice(indexOfNearestNeighbor, numberOfStatesToProjectIntoFuture);
    
    // 3. project the historical enemy movements identified in step 2 onto the enemey's current state
    ArrayList<Point2D.Double> projectedEnemyPositions = projectEnemyFuturePositions(currentState, historicalStatesWithWhichToPredictFuture);
    
    // 4. identify where the enemy will be at the time my bullet is whizzing by.
    Point2D.Double projectedFutureEnemyCoordinates = estimateEnemyPositionInFuture(projectedEnemyPositions, desiredFirepower);
    
    // 5. compute the gun heading of the enemy's future coordinates and return that heading
    double gunHeading = robot.headingToPoint(projectedFutureEnemyCoordinates);
    
    return gunHeading;
  }
}
