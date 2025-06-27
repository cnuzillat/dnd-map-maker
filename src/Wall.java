package src;

import java.awt.Point;

public class Wall {
    private double x, y;
    private double width, height;
    
    public Wall(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public double getLeft() { return x; }
    public double getRight() { return x + width; }
    public double getTop() { return y; }
    public double getBottom() { return y + height; }
} 