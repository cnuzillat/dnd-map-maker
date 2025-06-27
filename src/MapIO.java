package src;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class MapIO {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void saveMap(List<Wall> walls, List<Token> tokens, File file) throws IOException {
        MapData mapData = new MapData();
        mapData.walls = walls;
        mapData.tokens = tokens;
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(mapData, writer);
        }
    }
    
    public static MapData loadMap(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, MapData.class);
        }
    }

    public static void saveMap(boolean[][] walls, List<Token> tokens, File file) throws IOException {
        List<Wall> wallObjects = new ArrayList<>();
        for (int y = 0; y < walls.length; y++) {
            for (int x = 0; x < walls[0].length; x++) {
                if (walls[y][x]) {
                    wallObjects.add(new Wall(x, y, 1, 1));
                }
            }
        }
        saveMap(wallObjects, tokens, file);
    }
    
    public static boolean[][] loadWallsOnly(File file) throws IOException {
        MapData mapData = loadMap(file);
        if (mapData.walls != null) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            
            for (Wall wall : mapData.walls) {
                minX = Math.min(minX, wall.getLeft());
                minY = Math.min(minY, wall.getTop());
                maxX = Math.max(maxX, wall.getRight());
                maxY = Math.max(maxY, wall.getBottom());
            }
            
            int width = (int) Math.ceil(maxX - minX);
            int height = (int) Math.ceil(maxY - minY);
            
            boolean[][] array = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double gridX = minX + x;
                    double gridY = minY + y;
                    
                    for (Wall wall : mapData.walls) {
                        if (wall.contains(gridX, gridY)) {
                            array[y][x] = true;
                            break;
                        }
                    }
                }
            }
            return array;
        }
        return new boolean[0][0];
    }
    
    public static class MapData {
        public List<Wall> walls;
        public List<Token> tokens;
    }
} 