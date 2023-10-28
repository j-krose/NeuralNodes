package bugs;

import utils.KDTree2d;
import utils.Vector2d;

public class KillerBug extends Bug {
  private boolean killedThisRound_ = false;
  private int sinceLastKill_ = 0;
  private int numKills_ = 0;
  private int numKillsSinceLastReproduction_ = 0;

  public KillerBug(Vector2d position) {
    super(position);
  }

  public KillerBug(KillerBug other) {
    super(other);
    killedThisRound_ = other.killedThisRound_;
    sinceLastKill_ = other.sinceLastKill_;
    numKills_ = other.numKills_;
  }

  public KillerBug(KillerBug bug1, KillerBug bug2, Vector2d position) {
    super(bug1, bug2, position);
  }

  @Override
  public KillerBug clone() {
    return new KillerBug(this);
  }

  public boolean killedThisRound() // TODO: `madeAKillThisRound()`
      {
    return killedThisRound_;
  }

  public int getNumKills() {
    return numKills_;
  }

  public boolean hasKilledEnoughToReproduce() {
    return numKillsSinceLastReproduction_ >= GameStates.getKillerNKillsToReproduce();
  }

  @Override
  public BugType getBugType() {
    return BugType.KILLER;
  }

  @Override
  protected void onTickStart() {
    killedThisRound_ = false;
    sinceLastKill_++;
  }

  @Override
  protected double calculateReproductionScore(KDTree2d<BugType> bugTree, int round) {
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
        killedThisRound_ = true;
        sinceLastKill_ = 0;
        numKills_++;
        numKillsSinceLastReproduction_++;
        return false;
      }
    }
    if (sinceLastKill_
        > 60 /* ~ fps */ * GameStates.getKillerStarvationSeconds()) // TODO: put this somewhere else
    {
      return true;
    }
    return false;
  }
}
