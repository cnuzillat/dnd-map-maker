package src;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapGrid extends Canvas {
    private final int tileSize = 32;
    private Map<Point, Tile> tiles = new HashMap<>();

    private final List<Token> tokens = new ArrayList<>();

    private int selectionStartX, selectionStartY;

    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private Token draggingToken = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public MapGrid() {
        widthProperty().addListener((obs, oldVal, newVal) -> draw());
        heightProperty().addListener((obs, oldVal, newVal) -> draw());

        setWidth(800);
        setHeight(600);

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
            double adjustedX = (e.getX() - offsetX) / zoom;
            double adjustedY = (e.getY() - offsetY) / zoom;

            int gridX = (int)(adjustedX / tileSize);
            int gridY = (int)(adjustedY / tileSize);

            for (Token token : tokens) {
                double tx = token.getX();
                double ty = token.getY();
                double tw = token.getWidth() * tileSize;
                double th = token.getHeight() * tileSize;

                if (adjustedX >= tx && adjustedX <= tx + tw &&
                        adjustedY >= ty && adjustedY <= ty + th) {
                    draggingToken = token;
                    dragOffsetX = adjustedX - tx;
                    dragOffsetY = adjustedY - ty;
                    break;
                }
            }

            if (e.isPrimaryButtonDown()) {
                for (Token token : tokens) {
                    if (token.getX() == gridX && token.getY() == gridY) {
                        draggingToken = token;
                        dragOffsetX = adjustedX - gridX * tileSize;
                        dragOffsetY = adjustedY - gridY * tileSize;
                        break;
                    }
                }
            }
            else if (e.isSecondaryButtonDown()) {
                Token toRemove = null;
                for (Token token : tokens) {
                    double tx = token.getX();
                    double ty = token.getY();
                    int tw = token.getWidth();
                    int th = token.getHeight();

                    if (gridX >= tx && gridX < tx + tw &&
                            gridY >= ty && gridY < ty + th) {
                        toRemove = token;
                        break;
                    }
                }

                if (toRemove != null) {
                    tokens.remove(toRemove);
                    draw();
                }
            }
        });

        setOnMouseDragged(e -> {
            if (draggingToken != null) {
                double adjustedX = (e.getX() - offsetX) / zoom;
                double adjustedY = (e.getY() - offsetY) / zoom;

                double newX = adjustedX - dragOffsetX;
                double newY = adjustedY - dragOffsetY;

                draggingToken.setPosition(newX, newY);
                draw();
            }
        });

        setOnMouseReleased(e -> {
            draggingToken = null;
        });

    }

    public void setTile(int x, int y, TileType type) {
        tiles.put(new Point(x, y), new Tile(type));
        draw();
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

        int tilesWide = (int)(getWidth() / (zoom * tileSize)) + 2;
        int tilesHigh = (int)(getHeight() / (zoom * tileSize)) + 2;
        int startX = (int)(-offsetX / (zoom * tileSize)) - 1;
        int startY = (int)(-offsetY / (zoom * tileSize)) - 1;

        for (int y = startY; y < startY + tilesHigh; y++) {
            for (int x = startX; x < startX + tilesWide; x++) {
                Point p = new Point(x, y);
                Tile tile = tiles.getOrDefault(p, new Tile(TileType.FLOOR));
                Color color = switch (tile.getType()) {
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
            double drawX = token.getX();
            double drawY = token.getY();

            double drawWidth = token.getWidth() * tileSize;
            double drawHeight = token.getHeight() * tileSize;

            Image img = token.getImage();
            if (img != null) {
                gc.drawImage(img, drawX, drawY, drawWidth, drawHeight);
            }
            else {
                gc.setFill(Color.RED);
                gc.fillOval(drawX + 4, drawY + 4, drawWidth - 8, drawWidth - 8);
                gc.setFill(Color.WHITE);
                gc.fillText(token.getType().substring(0, 1), drawX + drawWidth / 2 - 4, drawY + drawWidth / 2 + 6);
            }
        }

        gc.restore();
    }

    public void setTiles(Tile[][] newTiles) {
        tiles.clear();

        for (int y = 0; y < newTiles.length; y++) {
            for (int x = 0; x < newTiles[0].length; x++) {
                tiles.put(new Point(x, y), newTiles[y][x]);
            }
        }
        draw();
    }

    public Map<Point, Tile> getTiles() {
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

    public Tile[][] toArray() {
        if (tiles.isEmpty()) return new Tile[0][0];

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point p : tiles.keySet()) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        Tile[][] array = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point p = new Point(minX + x, minY + y);
                array[y][x] = tiles.getOrDefault(p, new Tile(TileType.FLOOR));
            }
        }

        return array;
    }

    public void resizeCanvas(double width, double height) {
        setWidth(width);
        setHeight(height);
        draw();
    }

    public void setSelectionStart(double x, double y) {
        double adjustedX = (x - offsetX) / zoom;
        double adjustedY = (y - offsetY) / zoom;
        selectionStartX = (int)(adjustedX / tileSize);
        selectionStartY = (int)(adjustedY / tileSize);
    }

    public void setSelectionEndAndFill(double x, double y, TileType type) {
        double adjustedX = (x - offsetX) / zoom;
        double adjustedY = (y - offsetY) / zoom;
        int endX = (int)(adjustedX / tileSize);
        int endY = (int)(adjustedY / tileSize);

        int startX = Math.min(selectionStartX, endX);
        int endXFinal = Math.max(selectionStartX, endX);
        int startY = Math.min(selectionStartY, endY);
        int endYFinal = Math.max(selectionStartY, endY);

        for (int yGrid = startY; yGrid <= endYFinal; yGrid++) {
            for (int xGrid = startX; xGrid <= endXFinal; xGrid++) {
                setTile(xGrid, yGrid, type);
            }
        }
    }
}
