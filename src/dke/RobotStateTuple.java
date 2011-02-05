package dke;

import java.awt.geom.Point2D;

public class RobotStateTuple {
  public Point2D.Double position;
  public double heading;
  public double velocity;
  public long timeSinceLastShotFired;

  public RobotStateTuple() {
    position = null;
    heading = 0;
    velocity = 0;
    timeSinceLastShotFired = 0;
  }

  public RobotStateTuple(Point2D.Double position, double heading, double velocity) {
    this.position = position;
    this.heading = heading;
    this.velocity = velocity;
    this.timeSinceLastShotFired = 0;
  }

  public RobotStateTuple(Point2D.Double position, double heading, double velocity, long timeSinceLastShotFired) {
    this.position = position;
    this.heading = heading;
    this.velocity = velocity;
    this.timeSinceLastShotFired = timeSinceLastShotFired;
  }
}
