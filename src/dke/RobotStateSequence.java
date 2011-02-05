package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class RobotStateSequence extends ArrayList<RobotStateTuple> {
  public ArrayList<Point2D.Double> getPositions(int count) {
    ArrayList<Point2D.Double> retval = new ArrayList<Point2D.Double>();
    for(int i = size() - count; i < size(); i++) {
      retval.add(get(i).position);
    }
    return retval;
  }

  public ArrayList<Double> getHeadings(int count) {
    ArrayList<Double> retval = new ArrayList<Double>();
    for(int i = size() - count; i < size(); i++) {
      retval.add(get(i).heading);
    }
    return retval;
  }

  public ArrayList<Double> getVelocities(int count) {
    ArrayList<Double> retval = new ArrayList<Double>();
    for(int i = size() - count; i < size(); i++) {
      retval.add(get(i).velocity);
    }
    return retval;
  }

  public RobotStateTuple last() {
    return nthLast(1);
  }
  
  // nthLast returns the nth-to-last object.
  // offset is 1-based.
  // Example: list.nthLast(1) is the last item in the list and is equivalent to list.get(list.size() - 1)
  //          list.nthLast(2) is the second-to-last item in the list and is equivalent to list.get(list.size() - 2)
  //          list.nthLast(list.size()) is the 0th item in the list and is equivalent to list.get(list.size() - list.size()) => list(0).
  public RobotStateTuple nthLast(int offset) {
    return get(size() - offset);
  }
}
