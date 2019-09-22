package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JPanel;

import bugs.Bug;
import bugs.BugType;
import utils.Sizes;
import utils.Vector2d;

public class MainDrawPanel extends JPanel
{
    private static final long serialVersionUID = 0;
    private List<Bug> currBugList_;
    private List<Bug> nextBugList_;
    private long currMillis_;
    private double rollingFPS_ = 60.0;

    private static final NumberFormat FORMATTER = new DecimalFormat("#0.0");

    public MainDrawPanel()
    {
        currMillis_ = System.currentTimeMillis();
    }

    public void repaint(List<Bug> bugList)
    {
        nextBugList_ = bugList;
        super.repaint();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        long millis = System.currentTimeMillis();
        long elapsed = millis - currMillis_;
        currMillis_ = millis;

        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Dimension currSize = getSize();
        int border = Sizes.get().getBorder();
        Vector2d boardSize = Sizes.get().getBoardSize(); // TODO: Change board size on resize
        currSize.setSize(currSize.getWidth() - (border * 2), currSize.getHeight() - (border * 2));
        double xScale = ((double) currSize.getWidth()) / boardSize.getX();
        double yScale = ((double) currSize.getHeight()) / boardSize.getY();
        double scale = xScale < yScale ? xScale : yScale;

        graphics.setColor(Color.GRAY);
        graphics.fillRect(0, 0, getSize().width, getSize().height);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(border, border, (int) (boardSize.getX() * scale), (int) (boardSize.getY() * scale));

        currBugList_ = nextBugList_;
        if (currBugList_ == null)
        {
            return;
        }

        for (Bug bug : currBugList_)
        {
            if (bug.isReproducer())
            {
                Color ringColor = bug == currBugList_.get(0) ? Color.WHITE : Color.RED;
                graphics.setColor(ringColor);
                drawBug(graphics, bug, scale, Bug.BUG_RADIUS + 3.0);
                graphics.setColor(Color.BLACK);
                drawBug(graphics, bug, scale, Bug.BUG_RADIUS + 2.0);
            }

            graphics.setColor(bug.getColor());
            drawBug(graphics, bug, scale, Bug.BUG_RADIUS);

            if (bug.getBugType() == BugType.KILLER)
            {
                graphics.setColor(Color.BLACK);
                drawBug(graphics, bug, scale, Bug.BUG_RADIUS - 2.0);
            }
        }

        graphics.setColor(Color.WHITE);
        double fps = 1000.0 / ((double) Math.max(1, elapsed));
        rollingFPS_ = (0.999 * rollingFPS_) + 0.001 * (fps);
        graphics.drawString("FPS: " + FORMATTER.format(rollingFPS_), 5 + border, 15 + border);
        graphics.drawString("Bugs: " + currBugList_.size(), 70 + border, 15 + border);
    }

    private static void drawBug(Graphics2D graphics, Bug bug, double scale, double size)
    {
        int border = Sizes.get().getBorder();
        graphics.fillOval((int) ((bug.getPosition().getX() - size) * scale) + border, (int) ((bug.getPosition().getY() - size) * scale) + border, (int) (size * 2.0 * scale),
                (int) (size * 2.0 * scale));
    }
}
