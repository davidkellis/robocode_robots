package dke;

public interface TargetingModel {
  // This method returns the gun heading that the fire control system need to turn the gun to, and then fire.
  public abstract Double target(EnvironmentStateSequence pseq, double desiredFirepower);

}