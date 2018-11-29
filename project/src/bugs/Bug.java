package bugs;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import utils.KDTree2d;
import utils.Vector2d;

public abstract class Bug
{
    public static final ExecutorService SHARED_BUG_TICK_EXECUTOR = Executors.newWorkStealingPool();

    public static final double BUG_RADIUS = 4.0;
    public static final double BUG_RADIUS_SQUARED = Math.pow(BUG_RADIUS, 2.0);
    private static final double EYE_CONE_ANGLE = 180.0 * (Math.PI / 180.0); // Find a good minimum for this
    private static final double TIME_DIVISOR = 30.0;

    private Vector2d position_;
    private final Color color_;
    private boolean isReproducer_ = false;
    private final Genome genome_;
    private final NeuralNet net_;
    private boolean isAlive_ = true;

    public Bug(Vector2d position)
    {
        position_ = position;
        fixPosition();
        color_ = new Color(BugController.SHARED_RANDOM.nextFloat(), BugController.SHARED_RANDOM.nextFloat(), BugController.SHARED_RANDOM.nextFloat());
        genome_ = new Genome();
        net_ = new NeuralNet(genome_);
    }

    public Bug(Bug other)
    {
        position_ = new Vector2d(other.position_);
        color_ = new Color(other.color_.getRGB());
        isReproducer_ = other.isReproducer_;
        genome_ = other.genome_;
        net_ = new NeuralNet(other.net_);
        isAlive_ = other.isAlive_;
    }

    public Bug(Bug bug1, Bug bug2, Vector2d position)
    {
        position_ = position;

        double choice = BugController.SHARED_RANDOM.nextDouble();
        if (choice <= 0.95)
        {
            Color mixed = new Color((bug1.getColor().getRed() + bug2.getColor().getRed()) / 2, (bug1.getColor().getGreen() + bug2.getColor().getGreen()) / 2,
                    (bug1.getColor().getBlue() + bug2.getColor().getBlue()) / 2);
            // A little bit of variation
            color_ = new Color(Math.min(255, Math.max(0, mixed.getRed() + (BugController.SHARED_RANDOM.nextInt(11) - 5))),
                    Math.min(255, Math.max(0, mixed.getGreen() + (BugController.SHARED_RANDOM.nextInt(11) - 5))),
                    Math.min(255, Math.max(0, mixed.getBlue() + (BugController.SHARED_RANDOM.nextInt(11) - 5))));
        }
        else
        {
            color_ = new Color(BugController.SHARED_RANDOM.nextFloat(), BugController.SHARED_RANDOM.nextFloat(), BugController.SHARED_RANDOM.nextFloat());
        }

        genome_ = new Genome(bug1.genome_, bug2.genome_);
        net_ = new NeuralNet(genome_);
    }

    public abstract BugType getBugType();

    private void fixPosition()
    {
        position_.setX(Math.max(BUG_RADIUS, Math.min(BugController.BOARD_SIZE.getX() - BUG_RADIUS, position_.getX())));
        position_.setY(Math.max(BUG_RADIUS, Math.min(BugController.BOARD_SIZE.getY() - BUG_RADIUS, position_.getY())));
    }

    public Vector2d getPosition()
    {
        return position_;
    }

    public Color getColor()
    {
        return color_;
    }

    public void setIsReproducer(boolean isReproducer)
    {
        isReproducer_ = isReproducer;
    }

    public boolean isReproducer()
    {
        return isReproducer_;
    }

    public boolean isAlive()
    {
        return isAlive_;
    }

    public NeuralNet getNeuralNet()
    {
        return new NeuralNet(net_);
    }

    private static double sigmoid(boolean forwards, double tightness, double xCenter, double x)
    {
        double sig = 1.0 / (1.0 + Math.exp(-1.0 * tightness * (x - xCenter)));
        if (!forwards)
        {
            sig = 1 - sig;
        }
        return sig;
    }

    public Future<?> tickBug(KDTree2d<Bug> bugTree, long millisElapsed)
    {
        return SHARED_BUG_TICK_EXECUTOR.submit(() ->
        {            
            onTickStart();

            // Bugs in trees are copies. Ok to look into them while multithreading
            Bug otherBug = bugTree.findNearestExcludingSame(position_);
            Vector2d otherBugPos = otherBug.getPosition();
            
            // must be calculated before any movement
            boolean touchingOtherBug = position_.subtract(otherBug.getPosition()).normSquared() < (BUG_RADIUS_SQUARED * 4.0);

            // First four inputs are closeness to walls
            double leftWall = sigmoid(false, 0.1, 25, position_.getX());
            double rightWall = sigmoid(true, 0.1, BugController.BOARD_SIZE.getX() - 25, position_.getX());
            double bottomWall = sigmoid(false, 0.1, 25, position_.getY());
            double topWall = sigmoid(true, 0.1, BugController.BOARD_SIZE.getY() - 25, position_.getY());

            // Next four inputs are four "eyes" with cones pointing right, up, left, and
            // down. The value input to the neurons corresponding to these cones is the
            // product of a sigmoid based on distance, and a falloff function based on angle
            // away from the center of the cone
            Vector2d toOtherBug = otherBugPos.subtract(position_);
            double distance = toOtherBug.norm();
            double distanceSigmoid = sigmoid(false, 0.1, 25, distance);
            Vector2d dirToOtherBug = toOtherBug.normalize();
            double angle = Math.atan2(dirToOtherBug.getY(), dirToOtherBug.getX());
            if (angle < 0)
            {
                angle += (2 * Math.PI);
            }

            double rightEye = Math.max(0, 1.0 - (Math.abs(angle - 0.0) / (EYE_CONE_ANGLE / 2.0)));
            rightEye += Math.max(0, 1.0 - (Math.abs(angle - (2.0 * Math.PI)) / (EYE_CONE_ANGLE / 2.0)));
            double topEye = Math.max(0, 1.0 - (Math.abs(angle - (Math.PI / 2.0)) / (EYE_CONE_ANGLE / 2.0)));
            double leftEye = Math.max(0, 1.0 - (Math.abs(angle - Math.PI) / (EYE_CONE_ANGLE / 2.0)));
            double bottomEye = Math.max(0, 1.0 - (Math.abs(angle - (3.0 * Math.PI / 2.0)) / (EYE_CONE_ANGLE / 2.0)));

            rightEye *= distanceSigmoid;
            topEye *= distanceSigmoid;
            leftEye *= distanceSigmoid;
            bottomEye *= distanceSigmoid;

            // Next two correspond to same and other nodes. One spikes if the closest node
            // is of the same type, and the other spikes is the approacher is different
            double same = getBugType() == otherBug.getBugType() ? distanceSigmoid : 0.0;
            double different = getBugType() != otherBug.getBugType() ? distanceSigmoid : 0.0;

            net_.setLayerValues(0, new double[] { rightWall, topWall, leftWall, bottomWall, rightEye, topEye, leftEye, bottomEye, same, different });
            net_.solveNet();

            double[] movement = net_.getResultLayer();
            double outAngle = movement[0] * 2.0 * Math.PI;
            Vector2d movementVec = new Vector2d(Math.cos(outAngle), Math.sin(outAngle));
            movementVec = movementVec.scale(movement[1] * 5.0);

            double timeScale = ((double) millisElapsed) / TIME_DIVISOR;
            position_ = position_.add(movementVec.scale(timeScale));
            
            // Death conditions
            boolean outOfBoundsX = position_.getX() < BUG_RADIUS || position_.getX() > (BugController.BOARD_SIZE.getX() - BUG_RADIUS);
            boolean outOfBoundsY = position_.getY() < BUG_RADIUS || position_.getY() > (BugController.BOARD_SIZE.getY() - BUG_RADIUS);
            if (outOfBoundsX || outOfBoundsY)
            {
                isAlive_ = false;
            }
            else if (killedByBug(touchingOtherBug, otherBug))
            {
                isAlive_ = false;
            }
            else if (killedBySpeed(movement[1]))
            {
                isAlive_ = false;
            }
            else
            {
                // should be unnecessary since this is now a death condition
                fixPosition();
            }
        });
    }

    // Subclasses can override this if they need to do something at the beginning of
    // the tick
    protected void onTickStart()
    {

    }

    // Subclasses can override this if they might die by movement speed
    protected boolean killedBySpeed(double rawSpeed)
    {
        return false;
    }

    // Subclasses can override this if they might die by neighbor intersections
    protected boolean killedByBug(boolean touchingClosestBug, Bug closestBug)
    {
        return false;
    }
}
