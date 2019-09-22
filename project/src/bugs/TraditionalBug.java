package bugs;

import utils.Vector2d;

public class TraditionalBug extends Bug
{
    private static final int MIN_REPRODUCTION_AGE = 60 /* ~ fps */ * 20 /* seconds */;
    private final int birthRound_;
    private int slowRounds = 0;

    public TraditionalBug(Vector2d position, int birthRound)
    {
        super(position);
        birthRound_ = birthRound;
    }

    public TraditionalBug(TraditionalBug other)
    {
        super(other);
        birthRound_ = other.birthRound_;
    }

    public TraditionalBug(TraditionalBug bug1, TraditionalBug bug2, Vector2d position, int birthRound)
    {
        super(bug1, bug2, position);
        birthRound_ = birthRound;
    }

    public TraditionalBug clone()
    {
        return new TraditionalBug(this);
    }

    public int getBirthRound()
    {
        return birthRound_;
    }

    public boolean isOldEnoughToReproduct(int currRound)
    {
        // In addition to all the bugs that are actually old enough to reproduce,
        // consider all bugs born at the beginning of the game
        // "old enough to reproduce"
        return (currRound - birthRound_ > MIN_REPRODUCTION_AGE) || birthRound_ < MIN_REPRODUCTION_AGE;
    }

    @Override
    public BugType getBugType()
    {
        return BugType.TRADITIONAL;
    }

    @Override
    protected boolean killedBySpeed(double rawSpeed)
    {
        if (Math.abs(rawSpeed) < .1)
        {
            slowRounds++;
        }
        else
        {
            slowRounds = 0;
        }
        if (slowRounds > 100)
        {
            return true;
        }
        return false;
    }

    @Override
    protected boolean killedByBug(boolean touchingClosestBug, BugType closestBugType)
    {
        return touchingClosestBug;
    }
}
