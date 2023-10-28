package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import bugs.Bug;
import bugs.BugController.TickCompletedMessage;
import bugs.BugType;
import bugs.GameStates;
import utils.Pair;
import utils.Sizes;
import utils.Vector2d;

public class MainDrawPanel extends JPanel {
  private static final long serialVersionUID = 0;
  private TickCompletedMessage nextMessage_;
  private long currMillis_;
  private double rollingFPS_ = 60.0;
  // TODO: Allow picking of which bug's net is shown using the mouse
  //  private Point mousePosition_ = null;

  private static final NumberFormat FORMATTER = new DecimalFormat("#0.0");

  public MainDrawPanel() {
    currMillis_ = System.currentTimeMillis();
    //    MouseHandler mouseHandler = new MouseHandler();
    //    addMouseMotionListener(mouseHandler);
  }

  //  private class MouseHandler implements MouseListener, MouseMotionListener {
  //    // -- MouseListener --
  //    @Override
  //    public void mouseClicked(MouseEvent e) {}
  //
  //    @Override
  //    public void mousePressed(MouseEvent e) {}
  //
  //    @Override
  //    public void mouseReleased(MouseEvent e) {}
  //
  //    @Override
  //    public void mouseEntered(MouseEvent e) {}
  //
  //    @Override
  //    public void mouseExited(MouseEvent e) {
  //      mousePosition_ = null;
  //    }
  //
  //    // -- MouseMotionListener --
  //
  //    @Override
  //    public void mouseDragged(MouseEvent e) {}
  //
  //    @Override
  //    public void mouseMoved(MouseEvent e) {
  //      mousePosition_ = e.getPoint();
  //    }
  //  }

  public void repaint(TickCompletedMessage message) {
    // In case a repaint is currently happening, do not overwrite the state it is currently using
    nextMessage_ = message;
    super.repaint();
  }

  @Override
  public void paintComponent(Graphics g) {
    TickCompletedMessage currMessage = nextMessage_;
    
    long millis = System.currentTimeMillis();
    long elapsed = millis - currMillis_;
    currMillis_ = millis;

    Graphics2D graphics = (Graphics2D) g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Dimension currSize = getSize();
    int border = Sizes.getBorder();
    Vector2d boardSize = Sizes.getBoardSize(); // TODO: Change board size on resize
    currSize.setSize(currSize.getWidth() - (border * 2), currSize.getHeight() - (border * 2));
    double xScale = (currSize.getWidth()) / boardSize.getX();
    double yScale = (currSize.getHeight()) / boardSize.getY();
    double scale = xScale < yScale ? xScale : yScale;

    graphics.setColor(Color.GRAY);
    graphics.fillRect(0, 0, getSize().width, getSize().height);
    graphics.setColor(Color.BLACK);
    graphics.fillRect(
        border, border, (int) (boardSize.getX() * scale), (int) (boardSize.getY() * scale));

    int traditionalCount = 0;
    int killerCount = 0;
    if (currMessage != null) {
      drawBugs(graphics, currMessage.bugList, scale);
      drawMatings(graphics, currMessage.matings, scale);

      if (currMessage.bugList != null) {
        for (Bug bug : currMessage.bugList) {
          if (bug.getBugType() == BugType.TRADITIONAL) {
            traditionalCount += 1;
          }
          if (bug.getBugType() == BugType.KILLER) {
            killerCount += 1;
          }
        }
      }
    }

    graphics.setColor(Color.WHITE);
    double fps = 1000.0 / (Math.max(1, elapsed));
    rollingFPS_ = (0.999 * rollingFPS_) + 0.001 * (fps);
    graphics.drawString("FPS: " + FORMATTER.format(rollingFPS_), 5 + border, 15 + border);
    graphics.drawString("Sheep: " + traditionalCount, 70 + border, 15 + border);
    graphics.drawString("Wolves: " + killerCount, 150 + border, 15 + border);
  }
  
  private static void drawBugs(Graphics2D graphics, List<Bug> bugList, double scale) {
    if (bugList == null) {
      return;
    }
    
    Map<BugType, Double> maxReproductionScorePerBugType = new HashMap<>();
    for (Bug bug : bugList) {
      maxReproductionScorePerBugType.compute(
          bug.getBugType(),
          (k, v) ->
              v == null ? bug.getReproductionScore() : Math.max(v, bug.getReproductionScore()));
    }

    for (Bug bug : bugList) {
      // Draw a ring around reproducers
      double reproductionScore = bug.getReproductionScore();
      if (reproductionScore > 0.) {
        float ringPercent =
            (float) (reproductionScore / maxReproductionScorePerBugType.get(bug.getBugType()));
        // 0% -> RED (1, 0, 0), 50% -> YELLOW (1, 1, 0), 100% GREEN (0, 1, 0)
        // red is 1 from 0% -> 50%, then 1 -> 0 from 50% -> 100%
        float ringRed = Math.min(1.f, 2.f * (1.f - ringPercent));
        // green is 0 -> 1 from 0% -> 50%, then 1 from 50% -> 100%
        float ringGreen = Math.min(1.f, 2.f * ringPercent);
        graphics.setColor(new Color(ringRed, ringGreen, 0.f));

        drawBug(graphics, bug, scale, GameStates.getBugRadius() + 3.0);
        graphics.setColor(Color.BLACK);
        drawBug(graphics, bug, scale, GameStates.getBugRadius() + 2.0);
      }

      graphics.setColor(bug.getColor());
      drawBug(graphics, bug, scale, GameStates.getBugRadius());

      if (bug.getBugType() == BugType.KILLER) {
        graphics.setColor(Color.BLACK);
        drawBug(graphics, bug, scale, GameStates.getBugRadius() - 2.0);
      }
    }
  }

  private static void drawBug(Graphics2D graphics, Bug bug, double scale, double size) {
    int border = Sizes.getBorder();
    graphics.fillOval(
        (int) ((bug.getPosition().getX() - size) * scale) + border,
        (int) ((bug.getPosition().getY() - size) * scale) + border,
        (int) (size * 2.0 * scale),
        (int) (size * 2.0 * scale));
  }

  private static void drawMatings(Graphics2D graphics, List<Pair<Bug, Bug>> matings, double scale) {
    if (matings == null || !DisplayOptions.shouldShowMatings()) {
      return;
    }

    graphics.setColor(Color.YELLOW);
    for (Pair<Bug, Bug> mating : matings) {
      graphics.drawLine(
          (int) (mating.first.getPosition().getX() * scale),
          (int) (mating.first.getPosition().getY() * scale),
          (int) (mating.second.getPosition().getX() * scale),
          (int) (mating.second.getPosition().getY() * scale));
    }
  }
}
