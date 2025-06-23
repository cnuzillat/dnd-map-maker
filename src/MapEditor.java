package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
