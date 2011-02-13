package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public interface MovementModel<StateSequenceType,StateTreeType> {
  public void logStateObservation(String robotName, EnvironmentStateTuple observation);
  public ArrayList<Point2D.Double> predictFutureMovement(String enemyRobotName, int numberOfPositionsToPredict);
  public StateSequenceType getStateSequence(String enemyRobotName);
  public StateTreeType getStateTree(String enemyRobotName);
}
