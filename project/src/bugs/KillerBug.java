package bugs;

import utils.Vector2d;

public class KillerBug extends Bug
{
    private static final int LIFE_FORCE = 1200;
    private boolean killedThisRound_ = false;
    private int sinceLastKill_ = 0;
    private int numKills_ = 0;

    public KillerBug(Vector2d position)
    {
        super(position);
    }

    public KillerBug(KillerBug other)
    {
        super(other);
        killedThisRound_ = other.killedThisRound_;
        sinceLastKill_ = other.sinceLastKill_;
        numKills_ = other.numKills_;
    }

    public KillerBug(KillerBug bug1, KillerBug bug2, Vector2d position)
    {
        super(bug1, bug2, position);
    }

    public boolean killedThisRound()
    {
        return killedThisRound_;
    }

    public int getNumKills()
    {
        return numKills_;
    }

    @Override
    public BugType getBugType()
    {
        return BugType.KILLER;
    }

    @Override
    protected void onTickStart()
    {
        killedThisRound_ = false;
        sinceLastKill_++;
    }

    @Override
    protected boolean killedByBug(boolean touchingClosestBug, Bug closestBug)
    {
        if (touchingClosestBug)
        {
            if (closestBug.getBugType() == BugType.KILLER)
            {
                return true;
            }
            else
            {
                killedThisRound_ = true;
                sinceLastKill_ = 0;
                numKills_++;
                return false;
            }
        }
        if (sinceLastKill_ > LIFE_FORCE)
        {
            return true;
        }
        return false;
    }
}
