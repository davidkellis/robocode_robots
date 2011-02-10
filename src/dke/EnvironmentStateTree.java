package dke;

import dke.KdTree.SqrEuclid;

public class EnvironmentStateTree extends SqrEuclid<Integer> {
  public EnvironmentStateTree() {
    super(EnvironmentStateTuple.DIMENSION_COUNT, null);
  }

  public EnvironmentStateTree(Integer maximumObservationCount) {
    super(EnvironmentStateTuple.DIMENSION_COUNT, maximumObservationCount);
  }
}
