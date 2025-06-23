package src;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class MapGrid extends Canvas {
    private final int rows = 20;
    private final int cols = 20;
    private final int tileSize = 32;
    private final Tile[][] tiles = new Tile[rows][cols];

    private final List<Token> tokens = new ArrayList<>();

    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private double mouseDragStartX = 0;
    private double mouseDragStartY = 0;
    private double offsetStartX = 0;
    private double offsetStartY = 0;

    public MapGrid() {
        setWidth(cols * tileSize);
        setHeight(rows * tileSize);

        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                tiles[y][x] = new Tile(TileType.FLOOR);

        draw();

        setupInteraction();
    }

    private void setupInteraction() {
        setOnScroll(e -> {
            double zoomFactor = 1.1;
            if (e.getDeltaY() < 0) zoomFactor = 1 / zoomFactor;

            double mouseX = e.getX();
            double mouseY = e.getY();

            offsetX = (offsetX - mouseX) * zoomFactor + mouseX;
            offsetY = (offsetY - mouseY) * zoomFactor + mouseY;

            zoom *= zoomFactor;

            draw();
        });

        setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
                mouseDragStartX = e.getX();
                mouseDragStartY = e.getY();
                offsetStartX = offsetX;
                offsetStartY = offsetY;
            }
        });

        setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
                offsetX = offsetStartX + (e.getX() - mouseDragStartX);
                offsetY = offsetStartY + (e.getY() - mouseDragStartY);
                draw();
            }
        });
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            tiles[y][x].setType(type);
            draw();
        }
    }

    public int getTileSize() {
        return tileSize;
    }

    public void addToken(Token token) {
        tokens.add(token);
        draw();
    }

    public void removeToken(Token token) {
        tokens.remove(token);
        draw();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                TileType type = tiles[y][x].getType();
                Color color = switch (type) {
                    case GRASS -> Color.LIGHTGREEN;
                    case WALL -> Color.GRAY;
                    case WATER -> Color.LIGHTBLUE;
                    case FLOOR -> Color.BEIGE;
                };
                gc.setFill(color);
                gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                gc.setStroke(Color.BLACK);
                gc.strokeRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        for (Token token : tokens) {
            double drawX = token.getX() * tileSize;
            double drawY = token.getY() * tileSize;

            Image img = token.getImage();
            if (img != null) {
                gc.drawImage(img, drawX, drawY, tileSize, tileSize);
            }
            else {
                gc.setFill(Color.RED);
                gc.fillOval(drawX + 4, drawY + 4, tileSize - 8, tileSize - 8);
                gc.setFill(Color.WHITE);
                gc.fillText(token.getType().substring(0, 1), drawX + tileSize / 2 - 4, drawY + tileSize / 2 + 6);
            }
        }

        gc.restore();
    }

    public void setTiles(Tile[][] newTiles) {
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                tiles[y][x] = newTiles[y][x];
            }
        }
        draw();
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getZoom() {
        return zoom;
    }
}
