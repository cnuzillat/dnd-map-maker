package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MapEditor extends Application {
    private TileType selectedTile = TileType.FLOOR;

    @Override
    public void start(Stage stage) {
        MapGrid mapGrid = new MapGrid();

        ToolBar toolbar = new ToolBar();
        for (TileType type : TileType.values()) {
            Button button = new Button(type.name());
            button.setOnAction(e -> selectedTile = type);
            toolbar.getItems().add(button);
        }

        mapGrid.setOnMouseClicked(e -> {
            int x = (int)(e.getX() / mapGrid.getTileSize());
            int y = (int)(e.getY() / mapGrid.getTileSize());
            mapGrid.setTile(x, y, selectedTile);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getItems().add(spacer);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Map");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    MapIO.saveMap(mapGrid.getTiles(), file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Map");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    Tile[][] loaded = MapIO.loadMap(file);
                    mapGrid.setTiles(loaded);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        toolbar.getItems().addAll(saveButton, loadButton);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(mapGrid);

        stage.setTitle("D&D Map Maker");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
