package src;

import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import java.awt.Point;
import java.io.File;

public class Token {
    public enum Type {
        PLAYER("Player", Color.BLUE),
        ENEMY("Enemy", Color.RED),
        NPC("NPC", Color.GREEN),
        PROP("Prop", Color.BROWN),
        DOOR("Door", Color.ORANGE),
        TRAP("Trap", Color.PURPLE);
        
        private final String displayName;
        private final Color color;
        
        Type(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
    }
    
    private Point position;
    private double fractionalX;
    private double fractionalY;
    private Type type;
    private String name;
    private int size;
    private String imagePath;
    private Image customImage;
    
    public Token(Point position, Type type, String name, int size) {
        this.position = position;
        this.fractionalX = 0.0;
        this.fractionalY = 0.0;
        this.type = type;
        this.name = name;
        this.size = size;
        this.imagePath = null;
        this.customImage = null;
    }
    
    public Token(Point position, Type type, String name, int size, String imagePath) {
        this.position = position;
        this.fractionalX = 0.0;
        this.fractionalY = 0.0;
        this.type = type;
        this.name = name;
        this.size = size;
        this.imagePath = imagePath;
        loadImage();
    }
    
    private void loadImage() {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File file = new File(imagePath);
                if (file.exists()) {
                    customImage = new Image(file.toURI().toString());
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + imagePath);
                e.printStackTrace();
            }
        }
    }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }
    
    public double getFractionalX() { return fractionalX; }
    public void setFractionalX(double fractionalX) { this.fractionalX = fractionalX; }
    
    public double getFractionalY() { return fractionalY; }
    public void setFractionalY(double fractionalY) { this.fractionalY = fractionalY; }

    public double getExactX() { return position.x + fractionalX; }
    public double getExactY() { return position.y + fractionalY; }

    public void setExactPosition(double x, double y) {
        this.position = new Point((int)x, (int)y);
        this.fractionalX = x - (int)x;
        this.fractionalY = y - (int)y;
    }
    
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { 
        this.imagePath = imagePath;
        loadImage();
    }
    
    public Image getCustomImage() { return customImage; }
    public boolean hasCustomImage() { return customImage != null; }
    
    @Override
    public String toString() {
        return name + " (" + type.getDisplayName() + ")";
    }
} 