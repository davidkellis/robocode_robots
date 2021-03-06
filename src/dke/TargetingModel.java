package dke;

public interface TargetingModel<StateSequenceType,StateTreeType> {
  // This method returns the gun heading that the fire control system need to turn the gun to, and then fire.
  public Double target(String targetRobotName, MovementModel<StateSequenceType,StateTreeType> movementModel, double desiredFirepower);
}