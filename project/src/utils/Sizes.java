package utils;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class Sizes
{
    public static Sizes SINGLETON_;

    public static void initialize()
    {
        SINGLETON_ = new Sizes();
    }

    private static final int BORDER = 5;
    private static final int NET_PANEL_WIDTH = 400;
    private static final int BIAS_PANEL_HEIGHT = 30;
    private static final int ADJUSTMENTS_PANEL_HEIGHT = 200;

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

    public static int getTotalWidth()
    {
        return SINGLETON_.totalWidth_;
    }

    public static int getTotalHeight()
    {
        return SINGLETON_.totalHeight_;
    }

    public static Dimension getTotal()
    {
        return new Dimension(getTotalWidth(), getTotalHeight());
    }

    public static int getBorder()
    {
        return BORDER;
    }

    public static int getBiasPanelHeight()
    {
        return BIAS_PANEL_HEIGHT;
    }

    public static int getAdjustmentPanelHeight()
    {
        return ADJUSTMENTS_PANEL_HEIGHT;
    }

    public static int getNetDrawPanelHeight()
    {
        return getTotalHeight() - getBiasPanelHeight() - getAdjustmentPanelHeight();
    }

    public static int getBoardWidthWithBorder()
    {
        return getTotalWidth() - NET_PANEL_WIDTH;
    }

    public static int getBoardWidth()
    {
        return getBoardWidthWithBorder() - (BORDER * 2);
    }

    public static int getBoardHeightWithBorder()
    {
        return getTotalHeight();
    }

    public static int getBoardHeight()
    {
        return getBoardHeightWithBorder() - (BORDER * 2);
    }

    public static Vector2d getBoardSize()
    {
        return new Vector2d(getBoardWidth(), getBoardHeight());
    }

    public static int getNetPanelWidth()
    {
        return NET_PANEL_WIDTH - (BORDER * 2);
    }
}
