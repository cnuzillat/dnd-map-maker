package src;

import javafx.scene.image.Image;

public class Token {
    private String type;
    private double x, y;
    private int width = 1;
    private int height = 1;
    private Image image;

    public Token(String type, double x, double y, Image image) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Image getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
