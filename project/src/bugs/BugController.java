package bugs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;
import utils.ConcurrentTimers;
import utils.KDTree2d;
import utils.Sizes;
import utils.Vector2d;

public class BugController {
  public static final Random SHARED_RANDOM = new Random();

  private static final int MIN_TRADITIONAL_BUGS = 4;
  private static final int MAX_TRADITIONAL_BUGS = 10000;
  private static final int MIN_KILLER_BUGS = 4;
  private static final int MAX_KILLER_BUGS = 10000;
  // Need some genetic diversity as a seed
  private static final int START_TRADITIONAL_BUGS = 100;
  private static final int START_KILLER_BUGS = 100;
  private final Timer timer_;
  private long currMillis_;
  private int round_;

  List<TraditionalBug> traditionalBugList_;
  List<KillerBug> killerBugList_;
  // bugTree_ needs to hold clones of the bugs in the above lists, otherwise there
  // may be race conditions while all the bugs are
  // updating against each other. If they are stored as clones, all the bugs are
  // updating agains the previous state.
  KDTree2d<BugType> bugTree_;
  List<Bug> bugsWhichReproducedThisRound_;
  SubmissionPublisher<List<Bug>> tickCompletedPublisher_;

  public BugController() {
    reset();

    tickCompletedPublisher_ = new SubmissionPublisher<>();

    timer_ = new Timer();
    timer_.schedule(
        new TimerTask() {
          @Override
          public void run() {
            tick();
            round_++;
          }
        },
        0,
        1000 / 59);
  }

  private void reset() {
    round_ = 0;

    bugTree_ = new KDTree2d<>();

    // Bug lists will populate themselves on first tick
    traditionalBugList_ = new ArrayList<>();
    killerBugList_ = new ArrayList<>();

    bugsWhichReproducedThisRound_ = new ArrayList<>();

    currMillis_ = System.currentTimeMillis();
  }

  public void tick() {
    updateBugs();
    publishCopiedBugs();
  }

  private void updateBugs() {
    // Make any necessary changes at the top of the round
    GameStates.runScheduledChanges();
    if (GameStates.checkResetScheduled()) {
      reset();
    }

    // Execute the round
    ConcurrentTimers.Checkpoint checkpoint = new ConcurrentTimers.Checkpoint();
    long millis = System.currentTimeMillis();
    long ellapsed = millis - currMillis_;
    currMillis_ = millis;

    List<Future<?>> bugFutures = new LinkedList<>();
    for (TraditionalBug bug : traditionalBugList_) {
      bugFutures.add(bug.tickBug(bugTree_, ellapsed));
    }
    for (KillerBug bug : killerBugList_) {
      bugFutures.add(bug.tickBug(bugTree_, ellapsed));
    }
    for (Future<?> f : bugFutures) {
      try {
        f.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    checkpoint = ConcurrentTimers.addToTimer("bug solves", checkpoint);

    // The bug tree must first be populated with the new locations of all surviving
    // bugs before creating any new bugs, or the new bugs may overlap when they try
    // to use getRandomAvailableLocation
    bugTree_.clear();
    // No sort necessary, oldest should be first
    pruneDeadBugsAndSortBugList(traditionalBugList_, null);
    // Sort by number of kills, with highest being first in the list
    pruneDeadBugsAndSortBugList(
        killerBugList_,
        (KillerBug bug1, KillerBug bug2) -> (bug2.getNumKills() - bug1.getNumKills()));

    bugsWhichReproducedThisRound_.clear();
    makeNewTraditionalBugs();
    makeNewKillerBugs();

    for (Bug bug : bugsWhichReproducedThisRound_) {
      bug.updateStateAfterReproduction();
    }

    checkpoint = ConcurrentTimers.addToTimer("update lists", checkpoint);

    printTimers();
  }

  @SuppressWarnings("unused")
  private void printTimers() {
    if (ConcurrentTimers.USE_TIMERS && round_ % 250 == 0) {
      System.out.println("==========");
      System.out.println("Bug solves   " + ConcurrentTimers.getTimer("bug solves"));
      System.out.println("Update lists " + ConcurrentTimers.getTimer("update lists"));
      System.out.println();
      System.out.println("BugSolve1    " + ConcurrentTimers.getTimer("BugSolve1"));
      System.out.println("BugSolve2    " + ConcurrentTimers.getTimer("BugSolve2"));
      System.out.println("BugSolve3    " + ConcurrentTimers.getTimer("BugSolve3"));
      System.out.println("BugSolve4    " + ConcurrentTimers.getTimer("BugSolve4"));
    }
  }

  private <SpecificBug extends Bug> void addBugToEcosystem(
      SpecificBug newBug, List<SpecificBug> relevantBugList) {
    relevantBugList.add(newBug);
    // See documentation on initial declaration of bugTree_ for why we have to clone
    bugTree_.addLocation(newBug.getPosition(), newBug.getBugType());
  }

  private <SpecificBug extends Bug> void addBugToEcosystem(
      SpecificBug newBug,
      SpecificBug parent1,
      SpecificBug parent2,
      List<SpecificBug> relevantBugList) {
    addBugToEcosystem(newBug, relevantBugList);
    bugsWhichReproducedThisRound_.add(parent1);
    bugsWhichReproducedThisRound_.add(parent2);
  }

  private <SpecificBug extends Bug> void pruneDeadBugsAndSortBugList(
      List<SpecificBug> relevantBugList, Comparator<SpecificBug> comparator) {
    List<SpecificBug> oldList = new LinkedList<>(relevantBugList);
    relevantBugList.clear();
    if (comparator != null) {
      Collections.sort(oldList, comparator);
    }
    for (SpecificBug bug : oldList) {
      if (bug.isAlive()) {
        addBugToEcosystem(bug, relevantBugList);
      }
    }
  }

  private <SpecificBug extends Bug> void makeRandomBugsUpToMinimum(
      List<SpecificBug> relevantBugList,
      int startMinBugs,
      int inPlayMinBugs,
      Function<Void, SpecificBug> bugConstructor) {
    // We can have no bugs for a few reasons.
    // 1) The beginning of the game
    // 2) They all managed to coincidentally die in the same round
    // 3) The user deactivated and reactivated a specific kind of bug
    int minBugs = (relevantBugList.size() == 0) ? startMinBugs : inPlayMinBugs;
    while (relevantBugList.size() < minBugs) {
      SpecificBug newBug = bugConstructor.apply(null);
      addBugToEcosystem(newBug, relevantBugList);
    }
  }

  private void makeNewTraditionalBugs() {
    List<TraditionalBug> reproduceCandidates = new ArrayList<>();
    int oldestBugBirthRound = -1;
    for (TraditionalBug bug : traditionalBugList_) {
      if (oldestBugBirthRound == -1) {
        oldestBugBirthRound = bug.getBirthRound();
      }

      // Oldest 5 bugs (or as many as are tied for first) which qualify to reproduce
      if (bug.isOldEnoughToReproduct(round_)
          && (reproduceCandidates.size() < 5 || bug.getBirthRound() == oldestBugBirthRound)) {
        reproduceCandidates.add(bug);
        bug.setIsReproducer(true);
      } else {
        bug.setIsReproducer(false);
      }
    }

    // Make one new bug from reproduction per round
    if (reproduceCandidates.size() != 0 && traditionalBugList_.size() < MAX_TRADITIONAL_BUGS) {
      TraditionalBug parent1 =
          reproduceCandidates.get(SHARED_RANDOM.nextInt(reproduceCandidates.size()));
      TraditionalBug parent2 =
          reproduceCandidates.get(SHARED_RANDOM.nextInt(reproduceCandidates.size()));
      TraditionalBug newBug =
          new TraditionalBug(parent1, parent2, getRandomAvailableLocation(), round_);
      addBugToEcosystem(newBug, parent1, parent2, traditionalBugList_);
    }

    // Refill with random bugs as necessary
    makeRandomBugsUpToMinimum(
        traditionalBugList_,
        START_TRADITIONAL_BUGS,
        MIN_TRADITIONAL_BUGS,
        (vOiD) -> new TraditionalBug(getRandomAvailableLocation(), round_));
  }

  private void makeNewKillerBugs() {
    if (!GameStates.getKillersExist()) {
      return;
    }

    int numTopKills = -1;
    List<KillerBug> topKillers = new ArrayList<>();
    List<KillerBug> readyToReproduce = new ArrayList<>();
    for (KillerBug bug : killerBugList_) {
      if (numTopKills == -1) {
        numTopKills = bug.getNumKills();
      }

      bug.setIsReproducer(false);
      if (bug.getNumKills() == numTopKills) {
        topKillers.add(bug);
        bug.setIsReproducer(true);
      }
      if (bug.hasKilledEnoughToReproduce()) {
        readyToReproduce.add(bug);
        bug.setIsReproducer(true);
      }
    }

    int readyToReproduceIndex = 0;
    while (killerBugList_.size() < MAX_KILLER_BUGS
        && readyToReproduceIndex < readyToReproduce.size()) {
      // All bugs that are ready to reproduce will reproduce with the top killers
      // (unless we have reached the maximum)
      KillerBug reproducer = readyToReproduce.get(readyToReproduceIndex);
      KillerBug topKillerToReproduceWith = topKillers.get(SHARED_RANDOM.nextInt(topKillers.size()));
      KillerBug newBug =
          new KillerBug(reproducer, topKillerToReproduceWith, getRandomAvailableLocation());
      addBugToEcosystem(newBug, reproducer, topKillerToReproduceWith, killerBugList_);

      readyToReproduceIndex++;
    }

    // Refill with random bugs as necessary
    makeRandomBugsUpToMinimum(
        killerBugList_,
        START_KILLER_BUGS,
        MIN_KILLER_BUGS,
        (vOiD) -> new KillerBug(getRandomAvailableLocation()));
  }

  private void publishCopiedBugs() {
    List<Bug> copy = new LinkedList<>();
    for (TraditionalBug bug : traditionalBugList_) {
      // deep copy
      copy.add(new TraditionalBug(bug));
    }
    for (KillerBug bug : killerBugList_) {
      // deep copy
      copy.add(new KillerBug(bug));
    }
    tickCompletedPublisher_.offer(copy, (subscriber, msg) -> false);
  }

  public Publisher<List<Bug>> getTickCompletedPublisher() {
    return tickCompletedPublisher_;
  }

  // TODO: getRandomAvailableLocationNearExistingLocation() for reproductions
  private Vector2d getRandomAvailableLocation() {
    Vector2d boardSize = Sizes.getBoardSize();

    int steps = 0;
    while (steps < 100) {
      Vector2d loc =
          new Vector2d(
              SHARED_RANDOM.nextDouble() * boardSize.getX(),
              SHARED_RANDOM.nextDouble() * boardSize.getY());
      Vector2d nearest = bugTree_.findNearestLocation(loc);
      if (nearest == null
          || (loc.subtract(nearest).normSquared() > (GameStates.getBugRadiusSquared() * 4.0))) {
        return loc;
      }
      steps++;
    }
    System.err.println("Could not find a valid location");
    return new Vector2d(boardSize.getX() / 2.0, boardSize.getY() / 2.0);
  }
}
