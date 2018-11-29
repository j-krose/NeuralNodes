package utils;

import java.util.LinkedList;

public class KDTree2d<T>
{
    private class TreeNode
    {
        private final int depth_;
        private final Vector2d location_;
        private final T data_;
        private TreeNode lesser_;
        private TreeNode greater_;

        public TreeNode(int depth, Vector2d location, T data)
        {
            depth_ = depth;
            location_ = new Vector2d(location);
            data_ = data;
        }

        public int getDepth()
        {
            return depth_;
        }

        public Vector2d getLocation()
        {
            return new Vector2d(location_);
        }

        public T getData()
        {
            return data_;
        }

        public void setLesser(TreeNode lesser)
        {
            lesser_ = lesser;
        }

        public void setGreater(TreeNode greater)
        {
            greater_ = greater;
        }

        public TreeNode getLesser()
        {
            return lesser_;
        }

        public TreeNode getGreater()
        {
            return greater_;
        }
    }

    private TreeNode root_;

    public KDTree2d()
    {
        // root is null
    }

    public void clear()
    {
        root_ = null;
    }

    public void addLocation(Vector2d location, T data)
    {
        if (root_ == null)
        {
            root_ = new TreeNode(0, location, data);
            return;
        }

        int currDepth = 0;
        TreeNode currNode = root_;
        while (true)
        {
            double newLocationComparator;
            double currNodeLocationComparator;
            if (currDepth % 2 == 0) // X
            {
                newLocationComparator = location.getX();
                currNodeLocationComparator = currNode.getLocation().getX();
            }
            else // Y
            {
                newLocationComparator = location.getY();
                currNodeLocationComparator = currNode.getLocation().getY();
            }

            boolean lesser = newLocationComparator < currNodeLocationComparator;
            TreeNode nextNode = lesser ? currNode.getLesser() : currNode.getGreater();
            if (nextNode != null)
            {
                currNode = nextNode;
                currDepth++;
                continue;
            }
            else
            {
                TreeNode newNode = new TreeNode(currDepth + 1, location, data);
                if (lesser)
                {
                    currNode.setLesser(newNode);
                    return;
                }
                else
                {
                    currNode.setGreater(newNode);
                    return;
                }
            }
        }
    }

    public Vector2d findNearestLocation(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, false, root_, 100000000.0).getLocation();
    }

    public Vector2d findNearestLocationExcludingSame(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, true, root_, 100000000.0).getLocation();
    }

    public T findNearest(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, false, root_, 100000000.0).getData();
    }

    public T findNearestExcludingSame(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, true, root_, 100000000.0).getData();
    }

    private TreeNode findNearestHelper(Vector2d location, boolean excludingSame, TreeNode currNode, double currClosestSquared)
    {
        assert currNode != null : "Null node passed to recursive function";
        
        TreeNode closestNode = null;
        double squareDistance = location.subtract(currNode.location_).normSquared();
        if (squareDistance < currClosestSquared && (!excludingSame || squareDistance > 0.00000001))
        {
            closestNode = currNode;
            currClosestSquared = squareDistance;
        }
        
        double border = ((currNode.getDepth() % 2) == 0) ? currNode.getLocation().getX() : currNode.getLocation().getY();
        double locationDimension = ((currNode.getDepth() % 2) == 0) ? location.getX() : location.getY();
        double squareDistanceFromBorder = Math.pow(locationDimension - border, 2.0);
        
        if (locationDimension < border || squareDistanceFromBorder < currClosestSquared)
        {
            if (currNode.getLesser() != null)
            {
                TreeNode lesserNode = findNearestHelper(location, excludingSame, currNode.getLesser(), currClosestSquared);
                if (lesserNode != null)
                {
                    closestNode = lesserNode;
                    // TODO: would be more efficient if this was passed back up instead of
                    // recalculated
                    currClosestSquared = location.subtract(lesserNode.getLocation()).normSquared();
                }
            }
        }
        
        if (locationDimension > border || squareDistanceFromBorder < currClosestSquared)
        {
            if (currNode.getGreater() != null)
            {
                TreeNode greaterNode = findNearestHelper(location, excludingSame, currNode.getGreater(), currClosestSquared);
                if (greaterNode != null)
                {
                    closestNode = greaterNode;
                    // No need to update currClosestSquared since we don't use it after this.
                }
            }
        }

        return closestNode;
    }

    @Override
    public String toString()
    {
        if (root_ == null)
        {
            return "Empty Tree";
        }

        String s = "";
        LinkedList<TreeNode> currLayer = new LinkedList<>();
        LinkedList<TreeNode> nextLayer = new LinkedList<>();
        nextLayer.add(root_);
        while (!nextLayer.isEmpty())
        {
            currLayer = new LinkedList<>(nextLayer);
            nextLayer.clear();
            while (!currLayer.isEmpty())
            {
                TreeNode node = currLayer.pop();
                s += (node.getLocation() + " ");
                if (node.getLesser() != null)
                {
                    nextLayer.add(node.getLesser());
                }
                if (node.getGreater() != null)
                {
                    nextLayer.add(node.getGreater());
                }
            }
            s += "\n";
        }

        return s;
    }
}
