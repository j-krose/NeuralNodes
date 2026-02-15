package bugs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import utils.OptionWithSchedulableChange;

public class GameStates {
  public static class GameStateHolder<T> extends OptionWithSchedulableChange<T> {
    private GameStateHolder(T value) {
      super(value);
    }

    public void scheduleOptionChange(T newValue) {
      scheduledChanges_.add(
          new Runnable() {
            @Override
            public void run() {
              setValue(newValue);
            }
          });
    }
  }

  // -- Traditional must move --
  public static GameStateHolder<Boolean> TRADITIONAL_MUST_MOVE = new GameStateHolder<>(true);

  public static boolean getTraditionalMustMove() {
    return TRADITIONAL_MUST_MOVE.getValue();
  }

  // -- Traditional reproduction rate --
  public static final List<Integer> TRADITIONAL_REPRODUCTION_SECONDS_OPTIONS =
      IntStream.rangeClosed(1, 60).boxed().collect(Collectors.toList());
  public static GameStateHolder<Integer> TRADITIONAL_REPRODUCTION_SECONDS =
      new GameStateHolder<>(10);

  public static int getTraditionalReproductionSeconds() {
    return TRADITIONAL_REPRODUCTION_SECONDS.getValue();
  }

  // -- Killers exist --
  public static GameStateHolder<Boolean> KILLERS_EXIST = new GameStateHolder<>(true);

  public static boolean getKillersExist() {
    return KILLERS_EXIST.getValue();
  }

  // -- Killer starvation --
  public static final List<Integer> KILLER_STARVATION_SECONDS_OPTIONS =
      IntStream.rangeClosed(1, 60).boxed().collect(Collectors.toList());
  public static GameStateHolder<Integer> KILLER_STARVATION_SECONDS = new GameStateHolder<>(20);

  public static int getKillerStarvationSeconds() {
    return KILLER_STARVATION_SECONDS.getValue();
  }

  // -- Killer reproduction --

  public static final List<Integer> KILLER_N_KILLS_TO_REPRODUCE_OPTIONS =
      IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList());
  public static GameStateHolder<Integer> KILLER_N_KILLS_TO_REPRODUCE = new GameStateHolder<>(3);

  public static int getKillerNKillsToReproduce() {
    return KILLER_N_KILLS_TO_REPRODUCE.getValue();
  }

  // -- Bug radius --
  public static final List<Integer> BUG_RADIUS_OPTIONS =
      IntStream.rangeClosed(3, 20).boxed().collect(Collectors.toList());
  public static GameStateHolder<Integer> BUG_RADIUS = new GameStateHolder<>(7);

  public static double getBugRadius() {
    return BUG_RADIUS.getValue();
  }

  public static double getBugRadiusSquared() {
    return getBugRadius() * getBugRadius();
  }

  // -- Changes to state must be scheduled so that they can occur between ticks --
  private static List<Runnable> scheduledChanges_;
  private static boolean scheduledReset_;

  public static void initialize() {
    scheduledChanges_ = new ArrayList<>();
    scheduledReset_ = false;
  }

  public static void runScheduledChanges() {
    for (Runnable change : scheduledChanges_) {
      change.run();
    }
  }

  public static void scheduleReset() {
    scheduledReset_ = true;
  }

  // Check whether we need to do a reset, and set scheduledReset_ back to false.
  // Should be called once per tick.
  public static boolean checkResetScheduled() {
    boolean retValue = scheduledReset_;
    scheduledReset_ = false;
    return retValue;
  }
}
