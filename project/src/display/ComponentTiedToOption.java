package display;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import utils.OptionWithSchedulableChange;

public abstract class ComponentTiedToOption {
  public static JCheckBox checkBoxTiedToBoolean(
      String label, OptionWithSchedulableChange<Boolean> booleanOption) {
    JCheckBox checkBox = new JCheckBox(label, booleanOption.getValue());

    checkBox.addItemListener(
        new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            booleanOption.scheduleOptionChange(
                e.getStateChange() == ItemEvent.SELECTED ? true : false);
          }
        });

    return checkBox;
  }

  public static JSpinner spinnerTiedToIntegers(
      OptionWithSchedulableChange<Integer> integerOption, List<Integer> options) {
    JSpinner intSpinner = new JSpinner(new SpinnerListModel(options));
    intSpinner.setValue(integerOption.getValue());

    intSpinner.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            integerOption.scheduleOptionChange((Integer) intSpinner.getValue());
          }
        });

    return intSpinner;
  }
}
