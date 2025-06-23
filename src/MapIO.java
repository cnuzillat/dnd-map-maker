package src;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapIO {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveMap(Tile[][] tiles, File file) throws IOException {
        TileType[][] types = new TileType[tiles.length][tiles[0].length];
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                types[y][x] = tiles[y][x].getType();
            }
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(types, writer);
        }
    }

    public static Tile[][] loadMap(File file) throws IOException {
        String json = Files.readString(file.toPath());
        TileType[][] types = gson.fromJson(json, TileType[][].class);
        Tile[][] tiles = new Tile[types.length][types[0].length];
        for (int y = 0; y < types.length; y++) {
            for (int x = 0; x < types[0].length; x++) {
                tiles[y][x] = new Tile(types[y][x]);
            }
        }
        return tiles;
    }
}
