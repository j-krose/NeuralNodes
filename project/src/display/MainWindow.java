package display;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import bugs.Bug;
import bugs.BugController;
import utils.Vector2d;

public class MainWindow
{
    private JFrame mainFrame_;
    private MainDrawPanel mainDrawPanel_;
    private NetDrawPanel netDrawPanel_;

    public static final int BORDER = 5;
    private static final int NET_PANEL_WIDTH = 400;

    public MainWindow(BugController bugController)
    {
        javax.swing.SwingUtilities.invokeLater(() -> initialize(bugController));
    }

    public void initialize(BugController bugController)
    {
        mainFrame_ = new JFrame("Neurotic Nodes");
        mainFrame_.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame_.setLayout(new GridBagLayout());
        Vector2d boardSize = BugController.BOARD_SIZE;

        mainDrawPanel_ = new MainDrawPanel(boardSize);
        GridBagConstraints mainDrawPanelConstraints = new GridBagConstraints();
        mainDrawPanelConstraints.gridx = 0;
        mainDrawPanelConstraints.gridy = 0;
        mainDrawPanelConstraints.fill = GridBagConstraints.BOTH;
        mainDrawPanelConstraints.weightx = 1.0;
        mainDrawPanelConstraints.weighty = 1.0;
        int mainWidth = (int) boardSize.getX() + (BORDER * 2);
        int height = (int) boardSize.getY() + (BORDER * 2);
        mainDrawPanel_.setPreferredSize(new Dimension(mainWidth, height));
        mainFrame_.getContentPane().add(mainDrawPanel_, mainDrawPanelConstraints);

        netDrawPanel_ = new NetDrawPanel();
        GridBagConstraints netDrawPanelContraints = new GridBagConstraints();
        netDrawPanelContraints.gridx = 1;
        netDrawPanelContraints.gridy = 0;
        netDrawPanelContraints.fill = GridBagConstraints.BOTH;
        netDrawPanelContraints.weightx = 1.0;
        netDrawPanelContraints.weighty = 1.0;
        netDrawPanel_.setPreferredSize(new Dimension(NET_PANEL_WIDTH, height));
        mainFrame_.getContentPane().add(netDrawPanel_, netDrawPanelContraints);
        mainFrame_.getContentPane().setPreferredSize(new Dimension(mainWidth + NET_PANEL_WIDTH, height));
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
