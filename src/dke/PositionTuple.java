package dke;

import java.awt.geom.Point2D;

public class PositionTuple {
  public Point2D.Double position;
  public double heading;
  public double velocity;
  public long time;

  public PositionTuple() {
    position = null;
    heading = 0;
    velocity = 0;
    time = 0;
  }

  public PositionTuple(Point2D.Double position, double heading, double velocity, long time) {
    this.position = position;
    this.heading = heading;
    this.velocity = velocity;
    this.time = time;
  }
}
