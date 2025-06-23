package src;

import javafx.scene.image.Image;

public class Token {
    private String type;
    private int x, y;
    private int width = 1;
    private int height = 1;
    private Image image;

    public Token(String type, int x, int y, Image image) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
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

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
