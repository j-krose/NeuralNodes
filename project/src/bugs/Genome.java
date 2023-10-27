package bugs;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Genome {
  // TODO: Move color into here
  private static final double MUTATION_CHANCE = 0.02;

  private final int nodesInLayer_[];
  // [starting layer index][starting node][ending node]
  private final double weights_[][][];
  // [layer][node]
  private final double biases_[][];

  private static int generateNumHiddenLayerNodes() {
    int diff = NeuralNet.HIDDEN_LAYER_SIZE_UPPER_BOUND - NeuralNet.HIDDEN_LAYER_SIZE_LOWER_BOUND;
    return BugController.SHARED_RANDOM.nextInt(diff + 1) + NeuralNet.HIDDEN_LAYER_SIZE_LOWER_BOUND;
  }

  private static int wiggleNumHiddenLayerNodes(int numHiddenLayerNodes) {
    double choice = BugController.SHARED_RANDOM.nextDouble();
    if (choice < 0.05) {
      numHiddenLayerNodes++;
    } else if (choice < 0.1) {
      numHiddenLayerNodes--;
    }
    return Math.max(
        NeuralNet.HIDDEN_LAYER_SIZE_LOWER_BOUND,
        Math.min(NeuralNet.HIDDEN_LAYER_SIZE_UPPER_BOUND, numHiddenLayerNodes));
  }

  private static double generateWeight() {
    return (BugController.SHARED_RANDOM.nextDouble() * 2.0) - 1.0;
  }

  private static double generateBias() {
    return generateWeight();
  }

  private static double wiggleWeight(double weight) {
    return weight + ((BugController.SHARED_RANDOM.nextDouble() * 0.1) - 0.05);
  }

  private static double wiggleBias(double bias) {
    return wiggleWeight(bias);
  }

  private static <T> T mixGenes(T one, T two, Supplier<T> supplier, UnaryOperator<T> wiggler) {
    double choice = BugController.SHARED_RANDOM.nextDouble();
    if (choice < ((1.0 - MUTATION_CHANCE) / 2.0)) {
      return wiggler.apply(one);
    } else if (choice < (1.0 - MUTATION_CHANCE)) {
      return wiggler.apply(two);
    } else {
      return supplier.get();
    }
  }

  // Construct a random genome
  public Genome() {
    int numHiddenLayerNodes = generateNumHiddenLayerNodes();
    nodesInLayer_ =
        new int[] {NeuralNet.NODES_IN_LAYER[0], numHiddenLayerNodes, NeuralNet.NODES_IN_LAYER[2]};
    weights_ = new double[nodesInLayer_.length - 1][][];
    for (int i = 0; i < nodesInLayer_.length - 1; i++) {
      weights_[i] = new double[nodesInLayer_[i]][];

      for (int j = 0; j < weights_[i].length; j++) {
        weights_[i][j] = new double[nodesInLayer_[i + 1]];
        for (int k = 0; k < nodesInLayer_[i + 1]; k++) {
          // start with [-1.0, 1.0] but don't restrict to this
          weights_[i][j][k] = generateWeight();
        }
      }
    }

    biases_ = new double[nodesInLayer_.length][];
    for (int i = 0; i < nodesInLayer_.length; i++) {
      biases_[i] = new double[nodesInLayer_[i]];
      for (int j = 0; j < nodesInLayer_[i]; j++) {
        biases_[i][j] = generateBias();
      }
    }

    checkGenome();
  }

  public Genome(Genome other) {
    nodesInLayer_ = other.nodesInLayer_.clone();
    weights_ = new double[nodesInLayer_.length - 1][][];
    for (int i = 0; i < nodesInLayer_.length - 1; i++) {
      weights_[i] = new double[nodesInLayer_[i]][];
      for (int j = 0; j < weights_[i].length; j++) {
        weights_[i][j] = other.weights_[i][j].clone();
      }
    }
    biases_ = new double[nodesInLayer_.length][];
    for (int i = 0; i < nodesInLayer_.length; i++) {
      biases_[i] = other.biases_[i].clone();
    }
    checkGenome();
  }

  public Genome(Genome genome1, Genome genome2) {
    int numHiddenLayerNodes =
        mixGenes(
            genome1.nodesInLayer_[1],
            genome2.nodesInLayer_[1],
            () -> generateNumHiddenLayerNodes(),
            (num) -> wiggleNumHiddenLayerNodes(num));
    nodesInLayer_ =
        new int[] {NeuralNet.NODES_IN_LAYER[0], numHiddenLayerNodes, NeuralNet.NODES_IN_LAYER[2]};

    // pick weights randomly from the parents, mutating occasionally
    weights_ = new double[nodesInLayer_.length - 1][][];
    for (int i = 0; i < nodesInLayer_.length - 1; i++) {
      weights_[i] = new double[nodesInLayer_[i]][];

      for (int j = 0; j < weights_[i].length; j++) {
        weights_[i][j] = new double[nodesInLayer_[i + 1]];
        for (int k = 0; k < nodesInLayer_[i + 1]; k++) {
          double weight1 =
              genome1.weights_[i][j % genome1.nodesInLayer_[i]][k % genome1.nodesInLayer_[i + 1]];
          double weight2 =
              genome2.weights_[i][j % genome2.nodesInLayer_[i]][k % genome2.nodesInLayer_[i + 1]];
          weights_[i][j][k] =
              mixGenes(weight1, weight2, () -> generateWeight(), (w) -> wiggleWeight(w));
        }
      }
    }

    biases_ = new double[nodesInLayer_.length][];
    for (int i = 0; i < nodesInLayer_.length; i++) {
      biases_[i] = new double[nodesInLayer_[i]];
      for (int j = 0; j < nodesInLayer_[i]; j++) {
        double bias1 = genome1.biases_[i][j % genome1.nodesInLayer_[i]];
        double bias2 = genome2.biases_[i][j % genome2.nodesInLayer_[i]];
        biases_[i][j] = mixGenes(bias1, bias2, () -> generateBias(), (b) -> wiggleBias(b));
      }
    }

    checkGenome();
  }

  private void checkGenome() {
    assert weights_.length == (nodesInLayer_.length - 1) : "Bad genome 1";
    for (int i = 0; i < (nodesInLayer_.length - 1); i++) {
      assert weights_[i].length == nodesInLayer_[i] : "Bad genome 2";
      for (int j = 0; j < weights_[i].length; j++) {
        assert weights_[i][j].length == nodesInLayer_[i + 1] : "Bad genome 3";
      }
    }
    assert biases_.length == (nodesInLayer_.length) : "Bad genome 1";
    for (int i = 0; i < (nodesInLayer_.length); i++) {
      assert biases_[i].length == nodesInLayer_[i] : "Bad genome 2";
    }
  }

  public double getWeightAt(int startingLayer, int startingNode, int endingNode) {
    assert (startingLayer == 0 || startingLayer == 1) : "Invalid starting layer";
    assert startingNode < nodesInLayer_[startingLayer] : "Invalid starting node";
    assert endingNode < nodesInLayer_[startingLayer + 1] : "Invalid ending node";
    return weights_[startingLayer][startingNode][endingNode];
  }

  public double getBiasAt(int layer, int node) {
    assert (layer >= 0 && layer < nodesInLayer_.length) : "Invalid starting layer";
    assert node < nodesInLayer_[layer] : "Invalid node";
    return biases_[layer][node];
  }

  public int[] getNodesInLayer() {
    return nodesInLayer_;
  }
}
