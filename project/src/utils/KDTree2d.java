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

        public LocationAndData toLocationAndData()
        {
            return new LocationAndData(location_, data_);
        }

        public int getDepth()
        {
            return depth_;
        }

        public Vector2d getLocation()
        {
            return new Vector2d(location_);
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

    public class LocationAndData
    {
        public Vector2d location_;
        public T data_;

        public LocationAndData(Vector2d location, T data)
        {
            location_ = new Vector2d(location);
            data_ = data;
        }

        public Vector2d getLocation()
        {
            return new Vector2d(location_);
        }

        public T getData()
        {
            return data_;
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
        TreeNodeAndDistanceSquared tn = findNearestHelper(location, false, root_, 100000000.0);
        return tn.treeNode_.getLocation();
    }

    public Vector2d findNearestLocationExcludingSame(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, true, root_, 100000000.0).treeNode_.getLocation();
    }

    public LocationAndData findNearest(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, false, root_, 100000000.0).treeNode_.toLocationAndData();
    }

    public LocationAndData findNearestExcludingSame(Vector2d location)
    {
        if (root_ == null)
        {
            return null;
        }
        return findNearestHelper(location, true, root_, 100000000.0).treeNode_.toLocationAndData();
    }

    private class TreeNodeAndDistanceSquared
    {
        public TreeNode treeNode_;
        public double closestSquared_;

        public TreeNodeAndDistanceSquared(TreeNode treeNode, double closestSquared)
        {
            treeNode_ = treeNode;
            closestSquared_ = closestSquared;
        }

    }

    private TreeNodeAndDistanceSquared findNearestHelper(Vector2d location, boolean excludingSame, TreeNode currNode, double currClosestSquared)
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
                TreeNodeAndDistanceSquared lesserNode = findNearestHelper(location, excludingSame, currNode.getLesser(), currClosestSquared);
                if (lesserNode != null)
                {
                    closestNode = lesserNode.treeNode_;
                    currClosestSquared = lesserNode.closestSquared_;
                }
            }
        }

        if (locationDimension > border || squareDistanceFromBorder < currClosestSquared)
        {
            if (currNode.getGreater() != null)
            {
                TreeNodeAndDistanceSquared greaterNode = findNearestHelper(location, excludingSame, currNode.getGreater(), currClosestSquared);
                if (greaterNode != null)
                {
                    closestNode = greaterNode.treeNode_;
                    currClosestSquared = greaterNode.closestSquared_;
                }
            }
        }

        return closestNode == null ? null : new TreeNodeAndDistanceSquared(closestNode, currClosestSquared);
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
