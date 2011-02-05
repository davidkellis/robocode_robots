package dke;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentStateSequence extends ArrayList<EnvironmentStateTuple> {
  private static final long serialVersionUID = 8595917754565517052L;

  public ArrayList<RobotStateTuple> getEnemyStateTuples(int count) {
    ArrayList<RobotStateTuple> retval = new ArrayList<RobotStateTuple>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).enemyRobot);
    }
    return retval;
  }

  public ArrayList<RobotStateTuple> getSelfStateTuples(int count) {
    ArrayList<RobotStateTuple> retval = new ArrayList<RobotStateTuple>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).self);
    }
    return retval;
  }

  public ArrayList<Long> getTimes(int count) {
    ArrayList<Long> retval = new ArrayList<Long>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).time);
    }
    return retval;
  }
  
  public EnvironmentStateTuple last() {
    return nthLast(1);
  }
  
  // nthLast returns the nth-to-last object.
  // offset is 1-based.
  // Example: list.nthLast(1) is the last item in the list and is equivalent to list.get(list.size() - 1)
  //          list.nthLast(2) is the second-to-last item in the list and is equivalent to list.get(list.size() - 2)
  //          list.nthLast(list.size()) is the 0th item in the list and is equivalent to list.get(list.size() - list.size()) => list(0).
  public EnvironmentStateTuple nthLast(int offset) {
    return get(size() - offset);
  }
  
  public List<EnvironmentStateTuple> slice(int startIndex, int count) {
    return subList(startIndex, Math.min(startIndex + count, size()));
  }

  //************************** Enemy Attribute Getters ********************************

  public ArrayList<Point2D.Double> getEnemyPositions(int count) {
    ArrayList<Point2D.Double> retval = new ArrayList<Point2D.Double>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).enemyRobot.position);
    }
    return retval;
  }

  public ArrayList<Double> getEnemyHeadings(int count) {
    ArrayList<Double> retval = new ArrayList<Double>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).enemyRobot.heading);
    }
    return retval;
  }

  public ArrayList<Double> getEnemyVelocities(int count) {
    ArrayList<Double> retval = new ArrayList<Double>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).enemyRobot.velocity);
    }
    return retval;
  }

  public RobotStateTuple lastEnemyRobotState() {
    return last().enemyRobot;
  }
  
  public RobotStateTuple nthLastEnemyRobotState(int offset) {
    return nthLast(offset).enemyRobot;
  }

  //************************** Self/Me Attribute Getters ********************************

  public ArrayList<Point2D.Double> getSelfPositions(int count) {
    ArrayList<Point2D.Double> retval = new ArrayList<Point2D.Double>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).self.position);
    }
    return retval;
  }

  public ArrayList<Double> getSelfHeadings(int count) {
    ArrayList<Double> retval = new ArrayList<Double>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).self.heading);
    }
    return retval;
  }

  public ArrayList<Double> getSelfVelocities(int count) {
    ArrayList<Double> retval = new ArrayList<Double>();
    for(int i = Math.max(0, size() - count); i < size(); i++) {
      retval.add(get(i).self.velocity);
    }
    return retval;
  }

  public RobotStateTuple lastSelfRobotState() {
    return last().self;
  }
  
  public RobotStateTuple nthLastSelfRobotState(int offset) {
    return nthLast(offset).self;
  }
}
