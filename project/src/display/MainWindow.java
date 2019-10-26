package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import bugs.Bug;
import bugs.BugController;
import utils.Sizes;

public class MainWindow
{
    private JFrame mainFrame_;
    private MainDrawPanel mainDrawPanel_;
    private NetDrawPanel netDrawPanel_;
    private JCheckBox showBiases_;

    public MainWindow(BugController bugController)
    {
        javax.swing.SwingUtilities.invokeLater(() -> initialize(bugController));
    }

    public void initialize(BugController bugController)
    {
        mainFrame_ = new JFrame("Wolf in Sheep's Clothing");
        mainFrame_.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame_.setLayout(new GridBagLayout());

        mainDrawPanel_ = new MainDrawPanel();
        GridBagConstraints mainDrawPanelConstraints = new GridBagConstraints();
        mainDrawPanelConstraints.gridx = 0;
        mainDrawPanelConstraints.gridy = 0;
        mainDrawPanelConstraints.gridheight = 3;
        mainDrawPanelConstraints.fill = GridBagConstraints.BOTH;
        mainDrawPanelConstraints.weightx = 1.0;
        mainDrawPanelConstraints.weighty = 1.0;
        mainDrawPanel_.setPreferredSize(new Dimension(Sizes.getBoardWidthWithBorder(), Sizes.getTotalHeight()));
        mainFrame_.getContentPane().add(mainDrawPanel_, mainDrawPanelConstraints);

        showBiases_ = new JCheckBox("Include biases in display");

        netDrawPanel_ = new NetDrawPanel(showBiases_);
        int netPanelWidth = Sizes.getNetPanelWidth();
        GridBagConstraints netDrawPanelContraints = new GridBagConstraints();
        netDrawPanelContraints.gridx = 1;
        netDrawPanelContraints.gridy = 0;
        netDrawPanelContraints.fill = GridBagConstraints.BOTH;
        netDrawPanelContraints.weightx = 1.0;
        netDrawPanelContraints.weighty = 1.0;
        netDrawPanel_.setPreferredSize(new Dimension(netPanelWidth, Sizes.getNetDrawPanelHeight()));
        mainFrame_.getContentPane().add(netDrawPanel_, netDrawPanelContraints);

        JPanel biasPanel = new JPanel();
        biasPanel.add(showBiases_);
        GridBagConstraints optionPanelConstraings = new GridBagConstraints();
        optionPanelConstraings.gridx = 1;
        optionPanelConstraings.gridy = 1;
        biasPanel.setPreferredSize(new Dimension(netPanelWidth, Sizes.getBiasPanelHeight()));
        biasPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        mainFrame_.getContentPane().add(biasPanel, optionPanelConstraings);

        JPanel adjustmentsPanel = new AdjustmentsPanel();
        GridBagConstraints adjustmentsPanelConstraints = new GridBagConstraints();
        adjustmentsPanelConstraints.gridx = 1;
        adjustmentsPanelConstraints.gridy = 2;
        adjustmentsPanel.setPreferredSize(new Dimension(netPanelWidth, Sizes.getAdjustmentPanelHeight()));
        mainFrame_.getContentPane().add(adjustmentsPanel, adjustmentsPanelConstraints);

        mainFrame_.getContentPane().setPreferredSize(Sizes.getTotal());
        mainFrame_.pack();
        mainFrame_.setVisible(true);

        // receive updates:
        bugController.getTickCompletedPublisher().subscribe(new Subscriber<List<Bug>>()
        {
            private Subscription subscription_;

            @Override
            public void onSubscribe(Subscription subscription)
            {
                subscription_ = subscription;
                subscription_.request(1);
            }

            @Override
            public void onNext(List<Bug> bugList)
            {
                mainDrawPanel_.repaint(bugList);
                netDrawPanel_.repaint(bugList.get(0).getNeuralNet());
                subscription_.request(1);
            }

            @Override
            public void onError(Throwable t)
            {
                System.out.println(t.getMessage());
            }

            @Override
            public void onComplete()
            {
            }
        });
    }
}
