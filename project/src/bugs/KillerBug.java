package bugs;

import utils.Vector2d;

public class KillerBug extends Bug {
  private int millisSinceLastKill_ = 0;
  private int numKillsSinceLastReproduction_ = 0;

  public KillerBug(Vector2d position) {
    super(position);
  }

  public KillerBug(KillerBug other) {
    super(other);
    millisSinceLastKill_ = other.millisSinceLastKill_;
    numKillsSinceLastReproduction_ = other.numKillsSinceLastReproduction_;
  }

  public KillerBug(KillerBug bug1, KillerBug bug2, Vector2d position) {
    super(bug1, bug2, position);
  }

  @Override
  public KillerBug clone() {
    return new KillerBug(this);
  }

  @Override
  public BugType getBugType() {
    return BugType.KILLER;
  }

  @Override
  protected void onTickStart(long millisElapsed) {
    millisSinceLastKill_ += millisElapsed;
  }

  @Override
  protected double calculateReproductionScore() {
    // Once bug reaches GameStates.getKillerNKillsToReproduce() kills, it should have a reproduction
    // score of `1`, so that it is allowed to reproduce
    return numKillsSinceLastReproduction_ - (GameStates.getKillerNKillsToReproduce() - 1);
  }

  @Override
  protected void updateStateAfterReproduction() {
    numKillsSinceLastReproduction_ = 0;
  }

  @Override
  public boolean isAlive() {
    if (!GameStates.getKillersExist()) {
      return false;
    }
    return super.isAlive();
  }

  @Override
  protected boolean killedByBug(boolean touchingClosestBug, BugType closestBugType) {
    if (touchingClosestBug) {
      if (closestBugType == BugType.KILLER) {
        return true;
      } else {
        millisSinceLastKill_ = 0;
        numKillsSinceLastReproduction_++;
        return false;
      }
    }
    // TODO: put this check somewhere else
    if ((millisSinceLastKill_ / 1000.0) > GameStates.getKillerStarvationSeconds()) 
    {
      return true;
    }
    return false;
  }
}
