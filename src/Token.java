package src;

import javafx.scene.image.Image;

public class Token {
    private String type;
    private int x, y;
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

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
