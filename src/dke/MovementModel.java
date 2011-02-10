package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public interface MovementModel {
  public void logStateObservation(String robotName, EnvironmentStateTuple observation);
  public ArrayList<Point2D.Double> predictFutureMovement(String enemyRobotName, int numberOfPositionsToPredict);
  public EnvironmentStateSequence getStateSequence(String enemyRobotName);
  public EnvironmentStateTree getStateTree(String enemyRobotName);
}
