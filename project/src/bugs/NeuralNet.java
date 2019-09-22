package bugs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NeuralNet
{
    public static final ExecutorService SHARED_NODE_SOLVE_EXECUTOR = Executors.newWorkStealingPool();

    // -1 represents undefined
    public static final int NODES_IN_LAYER[] = { 10, -1, 2 };
    public static final int HIDDEN_LAYER_SIZE_LOWER_BOUND = 1;
    public static final int HIDDEN_LAYER_SIZE_UPPER_BOUND = 10;

    private final Genome genome_;
//    private final int numHiddenLayerNodes_;
    private final int nodesInLayer_[];
    private double nodeValues_[][];

    // Neural Net with a random genome
    public NeuralNet(Genome genome)
    {
        genome_ = genome;
        nodesInLayer_ = genome_.getNodesInLayer();
        clearNodeValues();
    }

    public NeuralNet(NeuralNet other)
    {
        genome_ = new Genome(other.genome_);
        nodesInLayer_ = genome_.getNodesInLayer();
        nodeValues_ = new double[getNumLayers()][];
        for (int i = 0; i < getNumLayers(); i++)
        {
            nodeValues_[i] = other.nodeValues_[i].clone();
        }
    }

    private int getNumLayers()
    {
        return nodesInLayer_.length;
    }

    private void clearNodeValues()
    {
        nodeValues_ = new double[getNumLayers()][];
        for (int i = 0; i < getNumLayers(); i++)
        {
            nodeValues_[i] = new double[nodesInLayer_[i]];
            for (int j = 0; j < nodesInLayer_[i]; j++)
            {
                nodeValues_[i][j] = 0.0;
            }
        }
    }

    public void setLayerValues(int layer, double[] values)
    {
        assert layer >= 0 && layer < getNumLayers() : "Incorect layer";
        assert nodesInLayer_[layer] == values.length : "Incorrect number of values for layer";
        nodeValues_[layer] = values.clone();
    }

    private static double ReLU(double value)
    {
        return Math.max(0, value);
    }

    private class SolveNode implements Runnable
    {
        private int layer_;
        private int node_;

        public SolveNode(int layer, int node)
        {
            assert layer > 0 && layer < getNumLayers() : "Incorect layer";
            layer_ = layer;
            node_ = node;
        }

        public void run()
        {
            double value = 0.0;
            for (int i = 0; i < nodesInLayer_[layer_ - 1]; i++)
            {
                value += (nodeValues_[layer_ - 1][i] * genome_.getWeightAt(layer_ - 1, i, node_));
            }
            value += genome_.getBiasAt(layer_, node_);
            if (layer_ != (getNumLayers() - 1))
            {
                nodeValues_[layer_][node_] = ReLU(value);
            }
            else
            {
                nodeValues_[layer_][node_] = value;
            }
        }
    }

    // should only be called once the input layer is set
    public void solveNet()
    {
//        List<Future<?>> layerFutures = new LinkedList<>();
//        for (int i = 1; i < getNumLayers(); i++)
//        {
//            for (int j = 0; j < nodeValues_[i].length; j++)
//            {
//                layerFutures.add(SHARED_NODE_SOLVE_EXECUTOR.submit(new SolveNode(i, j)));
//            }
//            for (Future<?> f : layerFutures)
//            {
//                // Wait for the layer to complete before moving on to the next layer
//                try
//                {
//                    f.get();
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//            layerFutures.clear();
//        }
        for (int i = 1; i < getNumLayers(); i++)
        {
            for (int j = 0; j < nodeValues_[i].length; j++)
            {
                new SolveNode(i, j).run();
            }
        }
    }

    public void printNet()
    {
        for (int i = 0; i < getNumLayers(); i++)
        {
            for (int j = 0; j < nodesInLayer_[i]; j++)
            {
                System.out.print(nodeValues_[i][j] + " ");
            }
            System.out.println();
        }
    }

    public double[] getResultLayer()
    {
        return nodeValues_[getNumLayers() - 1];
    }

    public int[] getNodesInLayer()
    {
        return nodesInLayer_.clone();
    }

    public double[][] getNodeValues()
    {
        double nodeValueCopy[][] = new double[getNumLayers()][];
        for (int i = 0; i < getNumLayers(); i++)
        {
            nodeValueCopy[i] = nodeValues_[i].clone();
        }
        return nodeValueCopy;
    }

    public double getWeightAt(int startingLayer, int startingNode, int endingNode)
    {
        return genome_.getWeightAt(startingLayer, startingNode, endingNode);
    }

    public double getBiasAt(int layer, int node)
    {
        return genome_.getBiasAt(layer, node);
    }
}
