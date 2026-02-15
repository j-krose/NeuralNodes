package bugs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.BiFunction;
import java.util.function.Function;
import utils.ConcurrentTimers;
import utils.KDTree2d;
import utils.Pair;
import utils.Sizes;
import utils.Vector2d;

public class BugController {
  public static final Random SHARED_RANDOM = new Random();

  private static final int START_BUGS = 100; // Need some genetic diversity as a seed
  private static final BugCountConfig TRADITIONAL_BUG_COUNTS =
      new BugCountConfig(START_BUGS, 4, 10000);
  private static final BugCountConfig KILLER_BUG_COUNTS = new BugCountConfig(START_BUGS, 4, 10000);

  private final Timer timer_;
  private int round_;
  private long currMillis_;

  List<TraditionalBug> traditionalBugList_;
  List<KillerBug> killerBugList_;
  // bugTree_ needs to hold clones of the bugs in the above lists, otherwise there
  // may be race conditions while all the bugs are
  // updating against each other. If they are stored as clones, all the bugs are
  // updating agains the previous state.
  KDTree2d<BugType> bugTree_;
  List<Bug> bugsWhichReproducedThisRound_;
  SubmissionPublisher<TickCompletedMessage> tickCompletedPublisher_;

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
        0, 1000 / 60
    );
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
    long elapsed = millis - currMillis_;
    currMillis_ = millis;

    List<Future<?>> bugFutures = new LinkedList<>();
    for (TraditionalBug bug : traditionalBugList_) {
      bugFutures.add(bug.tickBug(bugTree_, elapsed));
    }
    for (KillerBug bug : killerBugList_) {
      bugFutures.add(bug.tickBug(bugTree_, elapsed));
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
    rebuildStateAfterTick(traditionalBugList_);
    rebuildStateAfterTick(killerBugList_);

    bugsWhichReproducedThisRound_.clear();

    makeNewBugs(traditionalBugList_,
        (parent1, parent2) -> new TraditionalBug(parent1, parent2, getRandomAvailableLocation()),
        TRADITIONAL_BUG_COUNTS,
        (isInitialBatch) -> new TraditionalBug(getRandomAvailableLocation(), isInitialBatch));
    if (GameStates.getKillersExist()) {
      makeNewBugs(killerBugList_,
          (parent1, parent2) -> new KillerBug(parent1, parent2, getRandomAvailableLocation()),
          KILLER_BUG_COUNTS, (isInitialBatch) -> new KillerBug(getRandomAvailableLocation()));
    }

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

  // Does a few important things:
  // 1. Removes dead bugs
  // 2. Recalculates reproduction scores
  // 3. Re-sorts bug list based on reproduction score
  // 4. Rebuilds KD tree with new locations
  private <SpecificBug extends Bug> void rebuildStateAfterTick(List<SpecificBug> relevantBugList) {
    // Copy to temporary list and clear master list
    List<SpecificBug> tempList = new LinkedList<>(relevantBugList);
    relevantBugList.clear();
    
    // Prune dead bugs
    tempList.removeIf((bug) -> !bug.isAlive());

    // Re-insert into bugTree_ and relevantBugList
    for (SpecificBug bug : tempList) {
      addBugToEcosystem(bug, relevantBugList);
    }

    // Calculate new reproduction scores (must be done after adding all bugs to bugTree_)
    for (SpecificBug bug : relevantBugList) {
      bug.updateReproductionScore();
    }

    // Re-sort relevantBugList by reproduction score
    //    Collections.sort(relevantBugList, Comparator.comparingDouble(Bug::getReproductionScore));
  }

  private <SpecificBug extends Bug> void makeRandomBugsUpToMinimum(
      List<SpecificBug> relevantBugList,
      int startMinBugs,
      int inPlayMinBugs,
      // newRandomBug takes isInitialBatch and returns a bug
      Function<Boolean, SpecificBug> newRandomBug) {
    // We can have no bugs for a few reasons.
    // 1) The beginning of the game
    // 2) They all managed to coincidentally die in the same round
    // 3) The user deactivated and reactivated a specific kind of bug
    boolean isInitialBatch = (relevantBugList.size() == 0);
    int minBugs = isInitialBatch ? startMinBugs : inPlayMinBugs;
    while (relevantBugList.size() < minBugs) {
      SpecificBug newBug = newRandomBug.apply(isInitialBatch);
      addBugToEcosystem(newBug, relevantBugList);
    }
  }

  private static class BugCountConfig {
    private final int initialBugs_;
    private final int minBugs_;
    private final int maxBugs_;

    private BugCountConfig(int initialBugs, int minBugs, int maxBugs) {
      this.initialBugs_ = initialBugs;
      this.minBugs_ = minBugs;
      this.maxBugs_ = maxBugs;
    }
  }

  private <SpecificBug extends Bug> void makeNewBugs(
      List<SpecificBug> relevantBugList,
      BiFunction<SpecificBug, SpecificBug, SpecificBug> newBugByReproduction,
      BugCountConfig bugCountConfig,
      // Function(isInitialBatch) => Bug
      Function<Boolean, SpecificBug> newRandomBug) {
    if (relevantBugList.size() >= bugCountConfig.maxBugs_) {
      return;
    }

    double totalReproductionScore = 0;
    ArrayList<SpecificBug> reproducers = new ArrayList<>();
    for (SpecificBug bug : relevantBugList) {
      double bugReproductionScore = bug.getReproductionScore();
      if (bugReproductionScore > 0.) {
        totalReproductionScore += bug.getReproductionScore();
        reproducers.add(bug);
      }
    }
    
    // TODO: Generic "pick by weight" util
    if (reproducers.size() >= 2) {
      // -- Make one new bug from reproduction per round

      // Find the first parent by picking a random bug, with odds of being chosen weighted by
      // reproduction score
      SpecificBug parent1 = null;
      double parentOnePicker = SHARED_RANDOM.nextDouble() * totalReproductionScore;
      for (SpecificBug bug : reproducers) {
        parentOnePicker -= bug.getReproductionScore();
        if (parentOnePicker <= 0) {
          parent1 = bug;
          break;
        }
      }

      // Find the second parent in the same way, but eliminate the first parent from participation
      // so
      // that there is no asexual reproduction
      SpecificBug parent2 = null;
      double remainingReproductionScore = totalReproductionScore - parent1.getReproductionScore();
      double parentTwoPicker = SHARED_RANDOM.nextDouble() * remainingReproductionScore;
      for (SpecificBug bug : reproducers) {
        if (bug == parent1) {
          continue;
        }
        parentTwoPicker -= bug.getReproductionScore();
        if (parentTwoPicker <= 0) {
          parent2 = bug;
          break;
        }
      }

      SpecificBug newBug = newBugByReproduction.apply(parent1, parent2);
      addBugToEcosystem(newBug, parent1, parent2, relevantBugList);
    }

    // Refill with random bugs as necessary
    makeRandomBugsUpToMinimum(relevantBugList, bugCountConfig.initialBugs_, bugCountConfig.minBugs_,
        newRandomBug);
  }

  public class TickCompletedMessage {
    public final List<Bug> bugList;
    public final List<Pair<Bug, Bug>> matings;

    public TickCompletedMessage(List<Bug> bugList, List<Pair<Bug, Bug>> matings) {
      this.bugList = bugList;
      this.matings = matings;
    }
  }

  private void publishCopiedBugs() {
    List<Bug> copy = new ArrayList<>();
    for (TraditionalBug bug : traditionalBugList_) {
      // deep copy
      copy.add(new TraditionalBug(bug));
    }
    for (KillerBug bug : killerBugList_) {
      // deep copy
      copy.add(new KillerBug(bug));
    }
    List<Pair<Bug, Bug>> matings = new ArrayList<>();
    for (int i = 0; i < bugsWhichReproducedThisRound_.size(); i += 2) {
      // TODO: Introduce "bug id" and publish this as bug id rather than direct reference to bug
      Bug parent1 = bugsWhichReproducedThisRound_.get(i);
      Bug parent2 = bugsWhichReproducedThisRound_.get(i + 1);
      matings.add(new Pair<>(parent1, parent2));
    }

    tickCompletedPublisher_.submit(new TickCompletedMessage(copy, matings));
  }

  public Publisher<TickCompletedMessage> getTickCompletedPublisher() {
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
