package dke;

import dke.KdTree.SqrEuclid;

public class EnvironmentStateSequenceTree extends SqrEuclid<Integer> {
  public EnvironmentStateSequenceTree(int numberOfStatesPerObservation) {
    super(EnvironmentStateTuple.DIMENSION_COUNT * numberOfStatesPerObservation, null);
  }

  public EnvironmentStateSequenceTree(int numberOfStatesPerObservation, Integer maximumObservationCount) {
    super(EnvironmentStateTuple.DIMENSION_COUNT * numberOfStatesPerObservation, maximumObservationCount);
  }
}
