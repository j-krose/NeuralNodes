package bugs;

import utils.Vector2d;

public class TraditionalBug extends Bug {
  private static int ALLOWED_MILLIS_SLOW = 1000; // Dies after 1s slow

  private int millisAlive_ = 0;
  private int millisSlow_ = 0;
  private final boolean isFromInitialBatch_;

  public TraditionalBug(Vector2d position, boolean isFromInitialBatch) {
    super(position);
    isFromInitialBatch_ = isFromInitialBatch;
  }

  public TraditionalBug(TraditionalBug other) {
    super(other);
    millisAlive_ = other.millisAlive_;
    millisSlow_ = other.millisSlow_;
    isFromInitialBatch_ = other.isFromInitialBatch_;
  }

  public TraditionalBug(
      TraditionalBug bug1, TraditionalBug bug2, Vector2d position) {
    super(bug1, bug2, position);
    // Bugs from reproduction are never from initial batch
    isFromInitialBatch_ = false;
  }

  @Override
  public TraditionalBug clone() {
    return new TraditionalBug(this);
  }

  @Override
  public BugType getBugType() {
    return BugType.TRADITIONAL;
  }

  protected void onTickStart(long millisElapsed) {
    millisAlive_ += millisElapsed;
    // Add time to slow time by default. If the round is not slow, it is reset to 0 in
    // killedBySpeed(...)
    millisSlow_ += millisElapsed;
  }

  @Override
  protected double calculateReproductionScore() {
    // Bugs from the initial batch can reproduce immediately (without this basically all bugs die
    // immediately without creating more)
    double requiredSeconds =
        isFromInitialBatch_ ? 0 : GameStates.getTraditionalReproductionSeconds();

    // When millisecondsAlive_ reaches threshold, bug goes positive and can reproduce
    return (millisAlive_ / 1000.) - requiredSeconds;
  }

  @Override
  protected boolean killedBySpeed(double rawSpeed) {
    // millisSlow_ is already incremented in onTickStart(...). Reset it if bug is not going slow.
    if (Math.abs(rawSpeed) > .1) {
      millisSlow_ = 0;
    }
    
    return GameStates.getTraditionalMustMove() && (millisSlow_ > ALLOWED_MILLIS_SLOW);
  }

  @Override
  protected boolean killedByBug(boolean touchingClosestBug, BugType closestBugType) {
    return touchingClosestBug;
  }
}
