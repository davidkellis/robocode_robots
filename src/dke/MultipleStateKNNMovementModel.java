package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dke.KdTree.Entry;

public class MultipleStateKNNMovementModel implements MovementModel<EnvironmentStateSequence, EnvironmentStateSequenceTree> {
  public DkeRobot robot;
  int k;
  int numberOfStatesPerCompositeFeatureVector;
  int numberOfStatesToDiscard;
  public HashMap<String, Pair<EnvironmentStateSequence, EnvironmentStateSequenceTree>> observationLog;
  
  public MultipleStateKNNMovementModel(DkeRobot robot, int numberOfStatesPerCompositeFeatureVector, int kNearestNeighbors, int numberOfStatesToDiscard) {
    this.robot = robot;
    this.k = kNearestNeighbors;
    this.numberOfStatesPerCompositeFeatureVector = numberOfStatesPerCompositeFeatureVector;
    this.numberOfStatesToDiscard = numberOfStatesToDiscard;
    this.observationLog = new HashMap<String, Pair<EnvironmentStateSequence, EnvironmentStateSequenceTree>>();
  }
  
  public void logStateObservation(String robotName, EnvironmentStateTuple observation) {
    if(!observationLog.containsKey(robotName)) {
      observationLog.put(robotName,
                         new Pair<EnvironmentStateSequence, EnvironmentStateSequenceTree>(new EnvironmentStateSequence(),
                                                                                          new EnvironmentStateSequenceTree(numberOfStatesPerCompositeFeatureVector)));
    }
    
    EnvironmentStateSequence observationSequence = observationLog.get(robotName).first;
    EnvironmentStateSequenceTree observationTree = observationLog.get(robotName).last;
    
    observationSequence.add(observation);
    int indexOfLastObservation = observationSequence.size() - 1;
    double[] compositeFeatureVector = observationSequence.endSliceFeatureVector(indexOfLastObservation, numberOfStatesPerCompositeFeatureVector);
    observationTree.addPoint(compositeFeatureVector, indexOfLastObservation);
//    System.out.println(robot.getTime() + " @ observationSequence[" + indexOfObservation + "] = " + observation);
  }
  
  // Returns a list of points representing the position history of the enemy robot.
  // The first point represents the position that most closely resembles the enemy bot's next position.
  public ArrayList<Point2D.Double> predictFutureMovement(String enemyRobotName, int numberOfPositionsToPredict) {
    EnvironmentStateSequence stateSeq = getStateSequence(enemyRobotName);
    if(stateSeq != null) {
      EnvironmentStateTuple currentState = stateSeq.last();
      double[] currentCompositeFeatureVector = stateSeq.endSliceFeatureVector(stateSeq.size() - 1, numberOfStatesPerCompositeFeatureVector);
      EnvironmentStateSequenceTree stateTree = getStateTree(enemyRobotName);
      
      if(currentState != null && stateTree != null) {
        // 1. find index of the environment state tuple(s) that most closely resemble(s) (i.e. is/are the nearest neighbor(s) of) the current/most-recent environment state.
        List<Entry<Integer>> nearestNeighbors = stateTree.nearestNeighbor(currentCompositeFeatureVector, k + numberOfStatesToDiscard, true);
//        System.out.println(nearestNeighbors.size());
        // NOTE: nearestNeighbors is sorted by neighbor distance, in descending order. So, the nearest neighbors are at the end of the list.
        
        if(nearestNeighbors.size() > 0) {
          // find the nearest neighbor that was observed longer than 'numberOfStatesToDiscard' turns ago.
          Entry<Integer> tempFirstIndex = null;
          
          // We don't want to consider any of the states that were very very recent (and therefore too-near a neighbor) as the nearest neighbor,
          // because we need some history *after* the movement.
          for(int i = nearestNeighbors.size() - 1; i >= 0 ; i--) {
            tempFirstIndex = nearestNeighbors.get(i);
            // if the observation at index i is at least as old or older than (current time - numberOfStatesToDiscard), then we want to consider it the nearest neighbor of interest.
            if(stateSeq.get(tempFirstIndex.value).time <= currentState.time - numberOfStatesToDiscard) {
              break;
            }
          }
//          System.out.println(tempFirstIndex.value + ": " + stateSeq.get(tempFirstIndex.value).time + " " + currentState.time);
          
          // 2. identify the environment state tuples that immediately follow the environment state tuple found in the previous step.
          List<EnvironmentStateTuple> historicalStatesWithWhichToPredictFuture = stateSeq.slice(tempFirstIndex.value, numberOfPositionsToPredict);
//          System.out.println(historicalStatesWithWhichToPredictFuture.size());
          
          // 3. project the historical enemy movements identified in step 2 onto the enemey's current state
          ArrayList<Point2D.Double> projectedEnemyPositions = projectEnemyFuturePositions(currentState, historicalStatesWithWhichToPredictFuture);
          
          return projectedEnemyPositions;
        }
      }
    }
//    System.out.println("empty");
    return new ArrayList<Point2D.Double>();
  }
  
  // Returns a list of coordinates that represents the enemy's projected position in the future.
  //   Assuming time t represents the game time in the current turn (i.e. t = NOW):
  //     The first set of coordinates (in position 0 of the return value) represents the projected enemy position in the very next step of the game, at time t+1 = t+1+0.
  //     The second set of coordinates (in position 1 of the return value) represents the projected enemy position two steps in the future, at time t+2 = t+1+1.
  //     The i-th set of coordinates (in position i-1 of the return value) represents the projected enemy position at time t+1+i.
  //
  // historicalStatesToReplay is a list of EnvironmentStateTuples that represent historical movement/position info. that we believe (hope) matches the enemy robot's next few steps.
  //   The 0th element in historicalStatesToReplay represents the historical EnvironmentStateTuple that is the nearest neighbor of the current environment state tuple.
  //   The 1st element through the last element in historicalStatesToReplay represents the historical enemy states that we use to "play out" from the current state.
  public ArrayList<Point2D.Double> projectEnemyFuturePositions(EnvironmentStateTuple currentState, List<EnvironmentStateTuple> historicalStatesToReplay) {
    ArrayList<Point2D.Double> projectedPositions = new ArrayList<Point2D.Double>();
    Point2D.Double currentEnemyPosition = currentState.enemyRobot.position;
    double currentEnemyHeading = currentState.enemyRobot.heading;
    EnvironmentStateTuple prevState, nextState;
    Point2D.Double prevPos, nextPos;
    double prevHeading;
    double distanceToNextPosition;
    double bearingToNextPosition;
    double differenceInHeading;
    
    for(int i = 1; i < historicalStatesToReplay.size(); i++) {   // start at index 1 because the first element is the nearest neighbor to the current state (the most recent state).
      prevState = historicalStatesToReplay.get(i - 1);
      nextState = historicalStatesToReplay.get(i);
      
      prevHeading = prevState.enemyRobot.heading;
      differenceInHeading = nextState.enemyRobot.heading - prevHeading;
      
      prevPos = prevState.enemyRobot.position;
      nextPos = nextState.enemyRobot.position;
      
      distanceToNextPosition = prevPos.distance(nextPos);
      
      bearingToNextPosition = Utils.bearingToPoint(nextPos, prevPos, prevHeading);
      
      currentEnemyPosition = Utils.pointAtBearing(bearingToNextPosition, distanceToNextPosition, currentEnemyPosition, currentEnemyHeading);
      currentEnemyHeading = currentEnemyHeading + differenceInHeading;
      
      projectedPositions.add(currentEnemyPosition);
    }
    
    return projectedPositions;
  }
  
  public EnvironmentStateSequence getStateSequence(String robotName) {
    if(observationLog.containsKey(robotName)) {
      return observationLog.get(robotName).first;
    }
    return null;
  }

  public EnvironmentStateSequenceTree getStateTree(String robotName) {
    if(observationLog.containsKey(robotName)) {
      return observationLog.get(robotName).last;
    }
    return null;
  }
}
