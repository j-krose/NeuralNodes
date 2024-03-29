package utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentTimers {
  public static final boolean USE_TIMERS = false;

  private static ConcurrentMap<String, AtomicLong> timers_;

  public static void initialize() {
    timers_ = new ConcurrentHashMap<>();
  }

  public static class Checkpoint {
    private long time_;

    public Checkpoint() {
      if (USE_TIMERS) {
        time_ = System.nanoTime();
      } else {
        time_ = 0;
      }
    }
  }

  public static Checkpoint addToTimer(String timer, Checkpoint startCheckpoint) {
    timers_.putIfAbsent(timer, new AtomicLong(0));

    Checkpoint now = new Checkpoint();
    timers_.get(timer).addAndGet(now.time_ - startCheckpoint.time_);

    return now;
  }

  public static long getTimer(String timer) {
    AtomicLong total = timers_.get(timer);
    return total != null ? total.get() : 0;
  }
}
