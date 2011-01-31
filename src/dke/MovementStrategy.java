package dke;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;

public interface MovementStrategy {
  public void moveRobot();
  public void onHitWall(HitWallEvent e);
  public void onHitRobot(HitRobotEvent e);
  public void onHitByBullet(HitByBulletEvent e);
}