package display;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

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

    public static final int BIAS_PANEL_HEIGHT = 30;

    public void initialize(BugController bugController)
    {
        mainFrame_ = new JFrame("Neurotic Nodes");
        mainFrame_.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame_.setLayout(new GridBagLayout());

        mainDrawPanel_ = new MainDrawPanel();
        GridBagConstraints mainDrawPanelConstraints = new GridBagConstraints();
        mainDrawPanelConstraints.gridx = 0;
        mainDrawPanelConstraints.gridy = 0;
        mainDrawPanelConstraints.gridheight = 2;
        mainDrawPanelConstraints.fill = GridBagConstraints.BOTH;
        mainDrawPanelConstraints.weightx = 1.0;
        mainDrawPanelConstraints.weighty = 1.0;
        int mainWidth = Sizes.get().getBoardWidthWithBorder();
        int height = Sizes.get().getTotalHeight();
        mainDrawPanel_.setPreferredSize(new Dimension(mainWidth, height));
        mainFrame_.getContentPane().add(mainDrawPanel_, mainDrawPanelConstraints);

        showBiases_ = new JCheckBox("Include biases");

        netDrawPanel_ = new NetDrawPanel(showBiases_);
        int netPanelWidth = Sizes.get().getNetPanelWidth();
        GridBagConstraints netDrawPanelContraints = new GridBagConstraints();
        netDrawPanelContraints.gridx = 1;
        netDrawPanelContraints.gridy = 0;
        netDrawPanelContraints.fill = GridBagConstraints.BOTH;
        netDrawPanelContraints.weightx = 1.0;
        netDrawPanelContraints.weighty = 1.0;
        netDrawPanel_.setPreferredSize(new Dimension(netPanelWidth, height - BIAS_PANEL_HEIGHT));
        mainFrame_.getContentPane().add(netDrawPanel_, netDrawPanelContraints);

        JPanel optionPanel = new JPanel();
        optionPanel.add(showBiases_);
        GridBagConstraints optionPanelConstraings = new GridBagConstraints();
        optionPanelConstraings.gridx = 1;
        optionPanelConstraings.gridy = 1;
        optionPanel.setPreferredSize(new Dimension(netPanelWidth, BIAS_PANEL_HEIGHT));
        mainFrame_.getContentPane().add(optionPanel, optionPanelConstraings);

        mainFrame_.getContentPane().setPreferredSize(Sizes.get().getTotal());
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
