package bugs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;

import utils.KDTree2d;
import utils.Vector2d;

public class BugController
{
    public static final Random SHARED_RANDOM = new Random();
    public static final Vector2d BOARD_SIZE = new Vector2d(800, 500);

    private static final int NUM_TRADITIONAL_BUGS = 200;
    private static final int MIN_KILLER_BUGS = 0;
    private static final int MAX_KILLER_BUGS = 0;
    private final Timer timer_;
    private long currMillis_;
    private int round_ = 0;

    List<TraditionalBug> traditionalBugList_;
    List<KillerBug> killerBugList_;
    KDTree2d<Bug> bugTree_;
    SubmissionPublisher<List<Bug>> tickCompletedPublisher_;

    public BugController()
    {
        bugTree_ = new KDTree2d<>();

        // Bug lists will populate themselves on first tick
        traditionalBugList_ = new LinkedList<>();
        killerBugList_ = new LinkedList<>();
        
        tickCompletedPublisher_ = new SubmissionPublisher<>();

        currMillis_ = System.currentTimeMillis();
        timer_ = new Timer();
        timer_.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                tick();
                round_++;
            }
        }, 0, 1000 / 59);
    }

    public void tick()
    {
        updateBugs();
        publishCopiedBugs();
    }

    private void updateBugs()
    {
        long millis = System.currentTimeMillis();
        long ellapsed = millis - currMillis_;
        currMillis_ = millis;

        List<Future<?>> bugFutures = new LinkedList<>();
        for (TraditionalBug bug : traditionalBugList_)
        {
            bugFutures.add(bug.tickBug(bugTree_, ellapsed));
        }
        for (KillerBug bug : killerBugList_)
        {
            bugFutures.add(bug.tickBug(bugTree_, ellapsed));
        }
        for (Future<?> f : bugFutures)
        {
            try
            {
                f.get();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        bugTree_.clear();
        updateTraditionalBugList();
        updateKillerBugList();
    }

    private void updateTraditionalBugList()
    {
        // Kill off dead bugs and find reproducers
        List<TraditionalBug> oldTraditionalBugList = new LinkedList<>(traditionalBugList_);
        traditionalBugList_.clear();
        int reproduceCandidateAge = -1;
        List<TraditionalBug> reproduceCandidates = new ArrayList<>();
        for (TraditionalBug bug : oldTraditionalBugList)
        {
            if (bug.isAlive())
            {
                traditionalBugList_.add(bug);
                bugTree_.addLocation(bug.getPosition(), new TraditionalBug(bug));

                if (reproduceCandidateAge == -1)
                {
                    reproduceCandidateAge = bug.getBornInRound();
                }

                if (bug.getBornInRound() == reproduceCandidateAge || reproduceCandidates.size() < 5)
                {
                    reproduceCandidates.add(bug);
                    bug.setIsReproducer(true);
                }
                else
                {
                    bug.setIsReproducer(false);
                }
            }
        }
        // Make new bugs
        if (traditionalBugList_.size() > 0)
        {
            for (int i = 0; i < (NUM_TRADITIONAL_BUGS - traditionalBugList_.size()); i++)
            {
                int parent1 = SHARED_RANDOM.nextInt(reproduceCandidates.size());
                int parent2 = SHARED_RANDOM.nextInt(reproduceCandidates.size());
                TraditionalBug bug = new TraditionalBug(reproduceCandidates.get(parent1), reproduceCandidates.get(parent2), getRandomAvailableLocation(), round_);
                traditionalBugList_.add(bug);
                bugTree_.addLocation(bug.getPosition(), new TraditionalBug(bug));
            }
        }
        else
        {
            // On first tick, and just in case all bugs die
            for (int i = 0; i < NUM_TRADITIONAL_BUGS; i++)
            {
                TraditionalBug bug = new TraditionalBug(getRandomAvailableLocation(), round_);
                traditionalBugList_.add(bug);
                bugTree_.addLocation(bug.getPosition(), new TraditionalBug(bug));
            }
        }
    }

    private void updateKillerBugList()
    {
        // Kill off old bugs and find reproducers
        List<KillerBug> oldKillerBugList = new LinkedList<>(killerBugList_);
        // Sort such that bugs with most kills are first
        Collections.sort(oldKillerBugList, (KillerBug bug1, KillerBug bug2) -> (bug2.getNumKills() - bug1.getNumKills()));
        killerBugList_.clear();
        int numTopKills = -1;
        List<KillerBug> topKillers = new ArrayList<>();
        List<KillerBug> killedThisRound = new ArrayList<>();
        for (KillerBug bug : oldKillerBugList)
        {
            if (bug.isAlive())
            {
                killerBugList_.add(bug);
                bugTree_.addLocation(bug.getPosition(), new KillerBug(bug));

                if (numTopKills == -1)
                {
                    numTopKills = bug.getNumKills();
                }

                bug.setIsReproducer(false);
                if (bug.getNumKills() == numTopKills)
                {
                    topKillers.add(bug);
                    bug.setIsReproducer(true);
                }
                if (bug.killedThisRound())
                {
                    killedThisRound.add(bug);
                    bug.setIsReproducer(true);
                }
            }
        }
        // Make new bugs
        int killerThisRoundIndex = 0;
        while (killerBugList_.size() < MAX_KILLER_BUGS && killerThisRoundIndex < killedThisRound.size())
        {
            int topKillersIndex = SHARED_RANDOM.nextInt(topKillers.size());

            KillerBug newBug = new KillerBug(topKillers.get(topKillersIndex), killedThisRound.get(killerThisRoundIndex), getRandomAvailableLocation());
            killerBugList_.add(newBug);
            bugTree_.addLocation(newBug.getPosition(), newBug);
            killerThisRoundIndex++;
        }
        // Make random bugs if we drop below minimum
        if (killerBugList_.size() < MIN_KILLER_BUGS)
        {
            for (int i = 0; i < MIN_KILLER_BUGS; i++)
            {
                KillerBug newBug = new KillerBug(getRandomAvailableLocation());
                killerBugList_.add(newBug);
                bugTree_.addLocation(newBug.getPosition(), newBug);
            }
        }
    }

    private void publishCopiedBugs()
    {
        List<Bug> copy = new LinkedList<>();
        for (TraditionalBug bug : traditionalBugList_)
        {
            // deep copy
            copy.add(new TraditionalBug(bug));
        }
        for (KillerBug bug : killerBugList_)
        {
            // deep copy
            copy.add(new KillerBug(bug));
        }
        tickCompletedPublisher_.offer(copy, (subscriber, msg) -> false);
    }

    public Publisher<List<Bug>> getTickCompletedPublisher()
    {
        return tickCompletedPublisher_;
    }

    private Vector2d getRandomAvailableLocation()
    {
        int steps = 0;
        while (steps < 100)
        {
            Vector2d loc = new Vector2d(SHARED_RANDOM.nextDouble() * BOARD_SIZE.getX(), SHARED_RANDOM.nextDouble() * BOARD_SIZE.getY());
            Vector2d nearest = bugTree_.findNearestLocation(loc);
            if (nearest == null || (loc.subtract(nearest).normSquared() > (Bug.BUG_RADIUS_SQUARED * 4.0)))
            {
                return loc;
            }
            steps++;
        }
        System.err.println("Could not find a valid location");
        return new Vector2d(BOARD_SIZE.getX() / 2.0, BOARD_SIZE.getY() / 2.0);
    }
}
