package bugs;

import utils.KDTree2d;
import utils.Vector2d;

public class TraditionalBug extends Bug {
  // TODO: Should be a game state (and should also have a dropdown to control distance vs time or
  // whatever)
  private static final double DISTANCE_NEEDED_TO_REPRODUCE = 25.;

  private final int birthRound_;
  private int slowRounds = 0;

  public TraditionalBug(Vector2d position, int birthRound) {
    super(position);
    birthRound_ = birthRound;
  }

  public TraditionalBug(TraditionalBug other) {
    super(other);
    birthRound_ = other.birthRound_;
  }

  public TraditionalBug(
      TraditionalBug bug1, TraditionalBug bug2, Vector2d position, int birthRound) {
    super(bug1, bug2, position);
    birthRound_ = birthRound;
  }

  @Override
  public TraditionalBug clone() {
    return new TraditionalBug(this);
  }

  public int getBirthRound() {
    return birthRound_;
  }

  //  public boolean isOldEnoughToReproduce(int currRound) {
  //    if (getNeuralNet().getResultLayer()[2] < 0.95) {
  //      return false;
  //    }
  //
  //    // In addition to all the bugs that are actually old enough to reproduce,
  //    // consider all bugs born at the beginning of the game
  //    // "old enough to reproduce"
  //    int minReproductionAge = 60 /* ~ fps */ * GameStates.getTraditionalReproductionSeconds();
  //    return (currRound - birthRound_ > minReproductionAge) || birthRound_ < minReproductionAge;
  //  }

  @Override
  public BugType getBugType() {
    return BugType.TRADITIONAL;
  }

  @Override
  protected double calculateReproductionScore(KDTree2d<BugType> bugTree, int round) {
    Vector2d nearest = bugTree.findNearestExcludingSame(getPosition()).getLocation();
    double centerDistance = getPosition().subtract(nearest).norm();
    double gap = centerDistance - 2 * GameStates.getBugRadius();
    // Must have at least DISTANCE_NEEDED_TO_REPRODUCE gap in order to reproduce
    return gap - DISTANCE_NEEDED_TO_REPRODUCE;
  }

  @Override
  protected boolean killedBySpeed(double rawSpeed) {
    if (GameStates.getTraditionalMustMove()) {
      if (Math.abs(rawSpeed) < .1) {
        slowRounds++;
      } else {
        slowRounds = 0;
      }
      if (slowRounds > 100) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean killedByBug(boolean touchingClosestBug, BugType closestBugType) {
    return touchingClosestBug;
  }
}
