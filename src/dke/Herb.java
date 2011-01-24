package dke;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

public class Herb extends DkeRobot {
  public long fireTime;
  public double firePower;
  public long timeTargetLastSeen;
  public MovementStrategy movementStrategy;

  @Override
  public void run() {
    initialize();
    while (true) {
      movementStrategy.moveRobot();
    }
  }

  public void initialize() {
    super.initialize();
    
    movementStrategy = new RectangularMovementStrategy(this);

    setColors(Color.orange, Color.black, Color.yellow); // body,gun,radar

    fireTime = 0;
    firePower = 1.001;
    timeTargetLastSeen = 0;

    beginRadarSweeping();
  }

  public void beginRadarSweeping() {
    setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    execute();
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent e) {
    double badGuyHeading = currentAbsoluteHeading() + e.getBearingRadians();
    double radarTurn = badGuyHeading - getRadarHeadingRadians();
    setTurnRadarRightRadians(1.7 * Utils.normalRelativeAngle(radarTurn));

    aimFire(badGuyHeading);
  }

  public void aimFire(double heading) {
    if (fireTime == getTime() && getGunTurnRemaining() == 0) {
      setFire(firePower);
      execute();
    }

    double gunTurn = heading - getGunHeadingRadians();
    setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
    // Don't need to check whether gun turn will complete in single turn because
    // we check that gun is finished turning before calling setFire(...).
    // This is simpler since the precise angle your gun can move in one tick
    // depends on where your robot is turning.
    fireTime = getTime() + 1;
  }

  @Override
  public void onHitByBullet(HitByBulletEvent e) {
    setBack(20);
    execute();
  }

  @Override
  public void onHitWall(HitWallEvent e) {
    setBack(20);
    execute();
  }

  @Override
  public void onSkippedTurn(SkippedTurnEvent e) {
    System.out.println("Skipping a turn!");
  }
}