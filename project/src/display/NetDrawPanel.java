package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JPanel;
import bugs.NeuralNet;
import utils.Vector2d;

public class NetDrawPanel extends JPanel {
  private static final long serialVersionUID = 1;
  private static final int NODE_SPACING = 10;
  private static final int NODE_BORDER = 4;
  private static final double EXTRA_LINE_SPACE = 2.0;

  private static final NumberFormat FORMATTER = new DecimalFormat("#0.0");

  private NeuralNet currSelectedBugNet_;
  private NeuralNet nextSelectedBugNet_;

  public void repaint(NeuralNet selectedBugNet) {
    nextSelectedBugNet_ = selectedBugNet;
    super.repaint();
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D graphics = (Graphics2D) g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, getWidth(), getHeight());

    currSelectedBugNet_ = nextSelectedBugNet_;

    if (currSelectedBugNet_ == null) {
      return;
    }

    Dimension currSize = getSize();
    int nodeDiameter = (currSize.width / NeuralNet.HIDDEN_LAYER_SIZE_UPPER_BOUND) - NODE_SPACING;
    int innerNodeDiameter = nodeDiameter - (NODE_BORDER * 2);

    int nodesInLayer[] = currSelectedBugNet_.getNodesInLayer();
    int layers = nodesInLayer.length;
    double[][] nodeValues = currSelectedBugNet_.getNodeValues();
    Vector2d[][] nodeCenters = new Vector2d[layers][];
    for (int layer = 0; layer < layers; layer++) {
      int nodesInThisLayer = nodesInLayer[layer];
      nodeCenters[layer] = new Vector2d[nodesInThisLayer];
      for (int node = 0; node < nodesInThisLayer; node++) {
        double nodeValue = nodeValues[layer][node];

        int nodeCenterX = ((2 * node) + 1) * currSize.width / (2 * nodesInThisLayer);
        int nodeCenterY = ((2 * layer) + 1) * currSize.height / (2 * layers);
        nodeCenters[layer][node] = new Vector2d(nodeCenterX, nodeCenterY);

        graphics.setColor(nodeValueToColor(nodeValue));
        graphics.fillOval(
            nodeCenterX - (nodeDiameter / 2),
            nodeCenterY - (nodeDiameter / 2),
            nodeDiameter,
            nodeDiameter);

        graphics.setColor(Color.BLACK);
        graphics.fillOval(
            nodeCenterX - (innerNodeDiameter / 2),
            nodeCenterY - (innerNodeDiameter / 2),
            innerNodeDiameter,
            innerNodeDiameter);

        graphics.setColor(Color.WHITE);
        // TODO: position and format the text better
        if (layer == (layers - 1) && node == 0) {
          double angle = nodeValue * 2.0 * Math.PI;
          graphics.drawLine(
              nodeCenterX,
              nodeCenterY,
              nodeCenterX + (int) ((innerNodeDiameter / 2.0) * Math.cos(angle)),
              nodeCenterY + (int) ((innerNodeDiameter / 2.0) * Math.sin(angle)));
        } else {
          int x = nodeCenterX - (nodeDiameter / 4);
          int y = nodeCenterY + (nodeDiameter / 7);
          graphics.drawString(FORMATTER.format(Math.abs(nodeValue)), x, y);
        }
      }
    }

    for (int layer = 0; layer < layers - 1; layer++) {
      for (int startNode = 0; startNode < nodesInLayer[layer]; startNode++) {
        for (int endNode = 0; endNode < nodesInLayer[layer + 1]; endNode++) {
          double edgeValue =
              (nodeValues[layer][startNode]
                  * currSelectedBugNet_.getWeightAt(layer, startNode, endNode));
          if (DisplayOptions.shouldShowBiases()) {
            edgeValue += currSelectedBugNet_.getBiasAt(layer + 1, endNode);
            edgeValue /= 2.5;
          }
          if (Math.abs(edgeValue) > 0.01) {
            graphics.setColor(weightValueToColor(edgeValue));
            Vector2d startNodeCenter = nodeCenters[layer][startNode];
            Vector2d endNodeCenter = nodeCenters[layer + 1][endNode];
            Vector2d direction = endNodeCenter.subtract(startNodeCenter).normalize();
            Vector2d offset = direction.scale((nodeDiameter / 2) + EXTRA_LINE_SPACE);
            Vector2d startPoint = startNodeCenter.add(offset);
            Vector2d endPoint = endNodeCenter.subtract(offset);
            graphics.drawLine(
                (int) startPoint.getX(),
                (int) startPoint.getY(),
                (int) endPoint.getX(),
                (int) endPoint.getY());
          }
          //                    graphics.drawLine((int) startNodeCenter.getX(), (int)
          // startNodeCenter.getY(), (int) endNodeCenter.getX(), (int) endNodeCenter.getY());
        }
      }
    }
  }

  // Empty value is white
  private static Color nodeValueToColor(double nodeValue) {
    if (nodeValue > 0) {
      return new Color(
          Math.max(0.f, Math.min(1.f, 1.f - (float) nodeValue)),
          1.f,
          Math.max(0.f, Math.min(1.f, 1.f - (float) nodeValue)));
    } else {
      return new Color(
          1.f,
          Math.max(0.f, Math.min(1.f, 1.f + (float) nodeValue)),
          Math.max(0.f, Math.min(1.f, 1.f + (float) nodeValue)));
    }
  }

  // Empty value is black
  private static Color weightValueToColor(double nodeValue) {
    if (nodeValue > 0) {
      return new Color(0.f, Math.max(0.f, Math.min(1.f, (float) nodeValue)), 0.f);
    } else {
      return new Color(Math.max(0.f, Math.min(1.f, (float) -nodeValue)), 0.f, 0.f);
    }
  }
}
