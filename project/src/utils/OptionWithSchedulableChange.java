package utils;

public abstract class OptionWithSchedulableChange<T> {
  private T value_;

  protected OptionWithSchedulableChange(T value) {
    value_ = value;
  }

  public T getValue() {
    return value_;
  }

  protected void setValue(T newValue) {
    value_ = newValue;
  }

  public abstract void scheduleOptionChange(T newValue);
}
