package src;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapGrid extends Canvas {
    private final int rows = 20;
    private final int cols = 20;
    private final int tileSize = 32;
    private final Tile[][] tiles = new Tile[rows][cols];

    public MapGrid() {
        setWidth(cols * tileSize);
        setHeight(rows * tileSize);

        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                tiles[y][x] = new Tile(TileType.FLOOR);

        draw();
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

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
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
    }
}
