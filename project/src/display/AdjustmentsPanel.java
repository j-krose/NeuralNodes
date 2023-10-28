package display;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import bugs.GameStates;
import utils.OptionWithSchedulableChange;

public class AdjustmentsPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  public AdjustmentsPanel() {
    this.setLayout(new GridBagLayout());

    int row = -1;

    // -- Show matings --
    JCheckBox showMatings =
        ComponentTiedToOption.checkBoxTiedToBoolean("Show matings", DisplayOptions.SHOW_MATINGS);
    addToGrid(showMatings, 0, ++row, 2);

    // -- Wolves exist --
    JCheckBox wolvesExist =
        ComponentTiedToOption.checkBoxTiedToBoolean("Wolves exist", GameStates.KILLERS_EXIST);
    addToGrid(wolvesExist, 0, ++row, 2);

    addToGrid(showMatings, 0, ++row, 2);

    // -- Sheep must move --
    JCheckBox sheepMustMove =
        ComponentTiedToOption.checkBoxTiedToBoolean(
            "Sheep must move to survive", GameStates.KILLERS_EXIST);
    addToGrid(sheepMustMove, 0, ++row, 2);

    // -- Wolf starvation --
    JPanel wolfStarvationPanel = new JPanel();
    JLabel wolfStarvationLabel1 = new JLabel("Wolves starve after");
    wolfStarvationPanel.add(wolfStarvationLabel1);

    JSpinner wolfStarvationSpinner =
        makeIntegerSpinner(
            GameStates.KILLER_STARVATION_SECONDS, GameStates.KILLER_STARVATION_SECONDS_OPTIONS, 2);
    wolfStarvationPanel.add(wolfStarvationSpinner);

    JLabel wolfStarvationLabel2 = new JLabel("seconds");
    wolfStarvationPanel.add(wolfStarvationLabel2);
    addToGrid(wolfStarvationPanel, 0, ++row, 2);

    // -- Sheep reproduction rate --
    // TODO: Bring back once sheep reproduction behavior is refined
    //    JPanel sheepReproductionPanel = new JPanel();
    //    JLabel sheepReproductionLabel1 = new JLabel("Sheep reproduce after");
    //    sheepReproductionPanel.add(sheepReproductionLabel1);
    //
    //    JSpinner sheepReproductionSpinner =
    //        makeIntegerSpinner(
    //            GameStates.TRADITIONAL_REPRODUCTION_SECONDS,
    //            GameStates.TRADITIONAL_REPRODUCTION_SECONDS_OPTIONS,
    //            2);
    //    sheepReproductionPanel.add(sheepReproductionSpinner);
    //
    //    JLabel sheepReproductionLabel2 = new JLabel("seconds");
    //    sheepReproductionPanel.add(sheepReproductionLabel2);
    //    addToGrid(sheepReproductionPanel, 0, ++row, 2);

    // -- Wolf reproduction --
    JPanel wolfReproductionPanel = new JPanel();
    JLabel wolfReproductionLabel1 = new JLabel("Wolves must kill");
    wolfReproductionPanel.add(wolfReproductionLabel1);

    JSpinner wolfReproductionSpinner =
        makeIntegerSpinner(
            GameStates.KILLER_N_KILLS_TO_REPRODUCE,
            GameStates.KILLER_N_KILLS_TO_REPRODUCE_OPTIONS,
            2);
    wolfReproductionPanel.add(wolfReproductionSpinner);

    JLabel wolfReproductionLabel2 = new JLabel("sheep to reproduce");
    wolfReproductionPanel.add(wolfReproductionLabel2);
    addToGrid(wolfReproductionPanel, 0, ++row, 2);

    // -- Bug radius --
    JPanel bugRadiusPanel = new JPanel();
    JLabel bugRadiusLabel = new JLabel("Size:");
    bugRadiusPanel.add(bugRadiusLabel);

    JSpinner bugRadiusSpinner =
        makeIntegerSpinner(GameStates.BUG_RADIUS, GameStates.BUG_RADIUS_OPTIONS, 2);
    bugRadiusPanel.add(bugRadiusSpinner);
    addToGrid(bugRadiusPanel, 0, ++row, 1);

    // -- Reset Button
    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            System.out.println("resetting");
            GameStates.scheduleReset();
          }
        });
    GridBagConstraints resetConstraints = new GridBagConstraints();
    resetConstraints.gridx = 1;
    resetConstraints.gridy = row;
    resetConstraints.gridwidth = 1;
    resetConstraints.anchor = GridBagConstraints.EAST;
    this.add(resetButton, resetConstraints);
  }

  private JSpinner makeIntegerSpinner(
      OptionWithSchedulableChange<Integer> integerOption, List<Integer> options, int nColumns) {
    JSpinner intSpinner = ComponentTiedToOption.spinnerTiedToIntegers(integerOption, options);
    JFormattedTextField intField = ((JSpinner.DefaultEditor) intSpinner.getEditor()).getTextField();
    intField.setColumns(nColumns);
    intField.setHorizontalAlignment(JTextField.RIGHT);
    intField.setEditable(false);
    return intSpinner;
  }

  private void addToGrid(JComponent component, int gridx, int gridy, int gridwidth) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = gridx;
    constraints.gridy = gridy;
    constraints.gridwidth = gridwidth;
    constraints.anchor = GridBagConstraints.WEST;
    this.add(component, constraints);
  }
}
