package dke;

import java.math.BigDecimal;

public class EnvironmentStateTuple {
  public static int DIMENSION_COUNT = 11;
  
  RobotStateTuple enemyRobot;
  RobotStateTuple self;   // this field represent "me"/"self" from the perspective of the robot that is making use of this class.
  long time;

  public EnvironmentStateTuple() {
    enemyRobot = null;
    self = null;
    time = 0;
  }

  public EnvironmentStateTuple(RobotStateTuple enemyStateTuple, RobotStateTuple selfStateTuple, long time) {
    enemyRobot = enemyStateTuple;
    self = selfStateTuple;
    time = 0;
  }
  
  public double[] featureVector() {
    return new double[]{enemyRobot.heading,
                        enemyRobot.position.x, 
                        enemyRobot.position.y, 
                        enemyRobot.velocity, 
                        enemyRobot.timeSinceLastShotFired,
                        self.heading, 
                        self.position.x, 
                        self.position.y, 
                        self.velocity, 
                        self.timeSinceLastShotFired,
                        time};
  }
  
  // This returns the Euclidean distance between feature vectors, where an EnvironmentStateTuple represents a feature vector
  public double euclideanDistance(EnvironmentStateTuple other) {
    BigDecimal sumOfSquares = new BigDecimal(0);
    BigDecimal tempDifference;
    BigSquareRoot rootFinder = new BigSquareRoot();
    
    tempDifference = new BigDecimal(enemyRobot.position.distance(other.enemyRobot.position));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(enemyRobot.heading).subtract(new BigDecimal(other.enemyRobot.heading));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(enemyRobot.velocity).subtract(new BigDecimal(other.enemyRobot.velocity));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(enemyRobot.timeSinceLastShotFired).subtract(new BigDecimal(other.enemyRobot.timeSinceLastShotFired));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(self.position.distance(other.self.position));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(self.heading).subtract(new BigDecimal(other.self.heading));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(self.velocity).subtract(new BigDecimal(other.self.velocity));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(self.timeSinceLastShotFired).subtract(new BigDecimal(other.self.timeSinceLastShotFired));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    tempDifference = new BigDecimal(time).subtract(new BigDecimal(other.time));
    sumOfSquares = sumOfSquares.add(tempDifference.pow(2));
    
    return rootFinder.sqrt(sumOfSquares).doubleValue();
  }
}
