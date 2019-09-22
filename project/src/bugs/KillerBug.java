package bugs;

import utils.Vector2d;

public class KillerBug extends Bug
{
    private static final int LIFE_FORCE = 60 /* ~ fps */ * 20 /* seconds */;
    private static final int NUM_KILLS_TO_REPRODUCE = 5;
    private boolean killedThisRound_ = false;
    private int sinceLastKill_ = 0;
    private int numKills_ = 0;
    private int numKillsSinceLastReproduction_ = 0;

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

    public KillerBug clone()
    {
        return new KillerBug(this);
    }

    public boolean killedThisRound() // TODO: `madeAKillThisRound()`
    {
        return killedThisRound_;
    }

    public int getNumKills()
    {
        return numKills_;
    }

    public boolean hasKilledEnoughToReproduce()
    {
        return numKillsSinceLastReproduction_ >= NUM_KILLS_TO_REPRODUCE;
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
    protected void updateStateAfterReproduction()
    {
        numKillsSinceLastReproduction_ = 0;
    }

    @Override
    protected boolean killedByBug(boolean touchingClosestBug, BugType closestBugType)
    {
        if (touchingClosestBug)
        {
            if (closestBugType == BugType.KILLER)
            {
                return true;
            }
            else
            {
                killedThisRound_ = true;
                sinceLastKill_ = 0;
                numKills_++;
                numKillsSinceLastReproduction_++;
                return false;
            }
        }
        if (sinceLastKill_ > LIFE_FORCE) // TODO: put this somewhere else
        {
            return true;
        }
        return false;
    }
}
