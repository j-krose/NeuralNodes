package bugs;

import utils.Vector2d;

public class TraditionalBug extends Bug
{
    private final int bornInRound_;

    public TraditionalBug(Vector2d position, int bornInRound)
    {
        super(position);
        bornInRound_ = bornInRound;
    }

    public TraditionalBug(TraditionalBug other)
    {
        super(other);
        bornInRound_ = other.bornInRound_;
    }

    public TraditionalBug(TraditionalBug bug1, TraditionalBug bug2, Vector2d position, int bornInRound)
    {
        super(bug1, bug2, position);
        bornInRound_ = bornInRound;
    }

    public int getBornInRound()
    {
        return bornInRound_;
    }

    @Override
    public BugType getBugType()
    {
        return BugType.TRADITIONAL;
    }

    @Override
    protected boolean killedBySpeed(double rawSpeed)
    {
        return rawSpeed < 0.1;
    }

    @Override
    protected boolean killedByBug(boolean touchingClosestBug, Bug closestBug)
    {
        return touchingClosestBug;
    }
}
