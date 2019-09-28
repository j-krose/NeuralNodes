package utils;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class Sizes
{
    public static Sizes SINGLETON;

    public static void initialize()
    {
        SINGLETON = new Sizes();
    }

    public static Sizes get()
    {
        return SINGLETON;
    }

    public static final int BORDER = 5;
    private static final int NET_PANEL_WIDTH = 400;

    private final int totalWidth_;
    private final int totalHeight_;

    private Sizes()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        // Hacky, but use a bare JFrame to get insets.
        JFrame temp = new JFrame("temp");
        temp.setVisible(true);
        Insets tempInsets = temp.getInsets();
        temp.dispose();
        totalWidth_ = (int) screenSize.getWidth() - (insets.left + insets.right + tempInsets.left + tempInsets.right);
        totalHeight_ = (int) screenSize.getHeight() - (insets.top + insets.bottom + tempInsets.top + tempInsets.bottom);

    }

    public int getTotalWidth()
    {
        return totalWidth_;
    }

    public int getTotalHeight()
    {
        return totalHeight_;
    }

    public Dimension getTotal()
    {
        return new Dimension(getTotalWidth(), getTotalHeight());
    }

    public int getBorder()
    {
        return BORDER;
    }

    public int getBoardWidthWithBorder()
    {
        return totalWidth_ - NET_PANEL_WIDTH;
    }

    public int getBoardWidth()
    {
        return getBoardWidthWithBorder() - (BORDER * 2);
    }

    public int getBoardHeightWithBorder()
    {
        return totalHeight_;
    }

    public int getBoardHeight()
    {
        return getBoardHeightWithBorder() - (BORDER * 2);
    }

    public Vector2d getBoardSize()
    {
        return new Vector2d(getBoardWidth(), getBoardHeight());
    }

    public int getNetPanelWidth()
    {
        return NET_PANEL_WIDTH - (BORDER * 2);
    }
}
