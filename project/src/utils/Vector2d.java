package utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Vector2d
{
    private double x_;
    private double y_;

    public Vector2d(double x, double y)
    {
        x_ = x;
        y_ = y;
    }

    public Vector2d(Vector2d other)
    {
        x_ = other.x_;
        y_ = other.y_;
    }

    public double getX()
    {
        return x_;
    }

    public double getY()
    {
        return y_;
    }

    public void setX(double x)
    {
        x_ = x;
    }

    public void setY(double y)
    {
        y_ = y;
    }

    // does not affect either vector
    public Vector2d add(Vector2d other)
    {
        return new Vector2d(x_ + other.x_, y_ + other.y_);
    }

    // does not affect either vector
    public Vector2d subtract(Vector2d other)
    {
        return new Vector2d(x_ - other.getX(), y_ - other.getY());
    }

    // does not affect the vector
    public Vector2d scale(double scalar)
    {
        return new Vector2d(scalar * x_, scalar * y_);
    }

    // does not affect the vector
    public Vector2d normalize()
    {
        return this.scale(1 / this.norm());
    }

    public double normSquared()
    {
        return Math.pow(x_, 2) + Math.pow(y_, 2);
    }

    public double norm()
    {
        return Math.sqrt(normSquared());
    }

    private static final NumberFormat FORMATTER = new DecimalFormat("#0.000");

    @Override
    public String toString()
    {
        return "(" + FORMATTER.format(x_) + ", " + FORMATTER.format(y_) + ")";
    }
}
