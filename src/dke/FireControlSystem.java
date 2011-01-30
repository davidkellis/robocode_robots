package dke;

import robocode.ScannedRobotEvent;

public interface FireControlSystem {
  public void onScannedRobot(ScannedRobotEvent e);
  public void run();
  public void target(String robotName);
}