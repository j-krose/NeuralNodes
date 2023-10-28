package display;

import utils.OptionWithSchedulableChange;

public class DisplayOptions {
  public static class DisplayOption<T> extends OptionWithSchedulableChange<T> {
    public DisplayOption(T value) {
      super(value);
    }

    // At some point there may be a need to schedule DisplayOptions between draw calls, but for now
    // the changes are just applied immediately
    public void scheduleOptionChange(T newValue) {
      setValue(newValue);
    }
  }

  // -- Show matings --
  public static DisplayOption<Boolean> SHOW_BIASES = new DisplayOption<>(false);

  public static boolean shouldShowBiases() {
    return SHOW_BIASES.getValue();
  }

  // -- Show matings --
  public static DisplayOption<Boolean> SHOW_MATINGS = new DisplayOption<>(false);

  public static boolean shouldShowMatings() {
    return SHOW_MATINGS.getValue();
  }
}
