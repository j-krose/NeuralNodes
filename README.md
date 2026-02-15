## Wolf in Sheep's Clothing

<video src="https://github.com/user-attachments/assets/c53aeaab-87bb-49b0-9fd1-c8457ef118af" controls="controls" style="max-width: 100%;">
</video>

A program designed to showcase the Neural Nodes framework.  Each node has its own fully connected forward-propagating nonlinear [nueral net](https://en.wikipedia.org/wiki/Artificial_neural_network).  When a node is "born", the parameters of its neural net are set permanently.  The input layer of the neural net allows the node to see the direction and distance to the nearest node, the identity of the nearest node (sheep or wolf), and the distance to each of the four walls.  The input layer of then feeds through a hidden layer in which each neuron uses a [sigmoid](https://en.wikipedia.org/wiki/Sigmoid_function) as an activation function, which then feeds to they output layer. The output of the neural net defines a direction that the node will move, and a velocity to move at.

A [genetic algorithm](https://en.wikipedia.org/wiki/Genetic_algorithm) then sits on top of this structure of individual nodes.  If a node satisfies some condition (different types of nodes have different reproduction conditions), it may have the opportunity to pass on its genome, which defines the weights and biases of the neural net and the number of neurons in the hidden layer.  Node reproduction always involves the mixing of two different node geonomes, as well as a degree of random mutation, to ensure that the system is always evolving and trying new solutions.  At the beginning of the simulation, a certain number of nodes are creating using completely random genomes.  Over time, the populations in question evolve "smarter" neural nets that allow them to better solve whatever problem is framed by their reproduction critera.  In essence, the nodes with the best neual nets pass on altered versions those neural nets to future generations.  The goal is provide minimal intervention into the system and let the genetic algorithm optimize the wights and biases of the neural nets of the populations of nodes.

#### Sheep and Wolves

Solid nodes are "sheep" and nodes represented as a colored ring are "wolves".

Sheep die if they touch a wall or touch any other node.  There is a setting that allows the user to choose whether sheep must continue to move to survive, which makes the simulation a bit more lively and can be compared to the needs of a real population to move around to graze.  Every tick, one new sheep is born by mixing the genome of two of the five sheep that have been around the longest.  Since the sheep that live the longest are the ones that are allowed to reproduce, the genetic algorithm tends to favor sheep that know how to avoid other nodes, and avoid walls.

Wolves die if they touch a wall, or touch another wolf.  If a wolf touches a sheep, it kills that sheep.  Wolves muse eat to reproduce.  Once a wolf reaches a certian threshold of "kills", it will reproduce with the wolf that has the greatest total number of kills.  The kill count of the wolf is then reset, so that it can reproduce again when it reaches the threshold again.  Since the wolves that kill the most are the ones that are allowed to reproduce, the genetic algorithm tends to favor wolves that can distinguish sheep from other wolves, and chase after those sheep quickly enough to touch them.

##### Observations

* Although there is a slight genetic advantage for wolves to live for a long time (because the wolf with the most kills gets to reproduce in every reproduction), it seems that this is vastly overshadowed by building neural nets that are good at finding and killing sheep.  It seems that wolves have a hard time learning to avoid walls, because as long as they reach the threshold of reproduction at least once they can pass on their genome.  This means that a wolf that is very good at making a large number of kills quickly can influence the wolf population without needing to actually be good at surviving for a long time.  This is in stark contrast to sheep, which only reproduce if they stick around to be the oldest sheep in the bunch.  The popluation of sheep quickly learns how to avoid walls, becuase the sheep that do not know how to avoid walls die away almost immediately.
* In contrast to the last observation, wolves do seem to learn to avoid each other fairly quickly.  Since there are many wolves in the playing field at any given time, a wolf who cannot avoid other wolves often cannot reach the kill threshold before dying.

### Installation

Download the runnable jar directly by clicking [here](https://github.com/j-krose/NeuralNodes/raw/main/WolfInSheepsClothing.jar), or by navigating to [this page](https://github.com/j-krose/NeuralNodes/blob/main/WolfInSheepsClothing.jar) and clicking the download button.

Once downloaded, double-click on the jar file to run it. Requires minimum of Java 9, which can be installed from [Oracle](https://www.oracle.com/java/technologies/downloads/).

If launching the jar from the file system does not work, the program can also be launched from the command line with `java -jar WolfInSheepsClothing.jar`. You can use `java -version` to check that you have Java 9 or later installed.

## Contributing to this repository

The project uses [google-java-format](https://github.com/google/google-java-format) for opinionated formatting. Follow
the instructions in the README to add the plugin to eclipse.

Turn on save actions (Preferences > Java > Editor > Save Actions) to format edited lines, and organize inputs.

You can also use eclipse-java-google-style.xml as the style in eclipse (Preferences > Java > Code Style > Formatter), so
that the default behaviors of eclipse to not clash with the styling it applies on-save.

Enjoy!
