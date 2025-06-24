package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.control.ToggleButton;

import java.io.File;
import java.io.IOException;

public class MapEditor extends Application {
    private TileType selectedTile = TileType.FLOOR;
    private boolean tokenMode = false;

    @Override
    public void start(Stage stage) {
        MapGrid mapGrid = new MapGrid();
        updateMouseHandlers(mapGrid, stage);

        ToolBar toolbar = new ToolBar();
        for (TileType type : TileType.values()) {
            Button button = new Button(type.name());
            button.setOnAction(e -> selectedTile = type);
            toolbar.getItems().add(button);
        }

        Region spacerOne = new Region();
        HBox.setHgrow(spacerOne, Priority.ALWAYS);

        toolbar.getItems().add(spacerOne);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Map");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files",
                    "*.json"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    Tile[][] tileArray = mapGrid.toArray();
                    MapIO.saveMap(tileArray, file);
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

        ToggleButton tokenToggle = new ToggleButton("Token Mode");
        tokenToggle.setOnAction(e -> {
            tokenMode = tokenToggle.isSelected();
            updateMouseHandlers(mapGrid, stage);
        });
        toolbar.getItems().add(tokenToggle);

        Region spacerTwo = new Region();
        HBox.setHgrow(spacerTwo, Priority.ALWAYS);

        toolbar.getItems().add(spacerTwo);

        toolbar.getItems().addAll(saveButton, loadButton);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(mapGrid);

        stage.setTitle("D&D Map Maker");
        Scene scene = new Scene(root);
        stage.setScene(scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            mapGrid.resizeCanvas(scene.getWidth(), scene.getHeight() - toolbar.getHeight());
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            mapGrid.resizeCanvas(scene.getWidth(), scene.getHeight() - toolbar.getHeight());
        });

        stage.show();
    }

    private void updateMouseHandlers(MapGrid mapGrid, Stage stage) {
        if (tokenMode) {
            mapGrid.setOnMouseClicked(e -> {
                double adjustedX = (e.getX() - mapGrid.getOffsetX()) / mapGrid.getZoom();
                double adjustedY = (e.getY() - mapGrid.getOffsetY()) / mapGrid.getZoom();
                int x = (int) (adjustedX / mapGrid.getTileSize());
                int y = (int) (adjustedY / mapGrid.getTileSize());
                TextInputDialog nameDialog = new TextInputDialog("TokenName");
                nameDialog.setTitle("Token Name");
                nameDialog.setHeaderText("Enter a name for your token:");
                nameDialog.setContentText("Name:");

                var result = nameDialog.showAndWait();
                if (result.isPresent() && !result.get().isBlank()) {
                    String tokenName = result.get();

                    TextInputDialog widthDialog = new TextInputDialog("1");
                    widthDialog.setTitle("Token Width");
                    widthDialog.setHeaderText("Enter token width (in tiles):");
                    widthDialog.setContentText("Width:");

                    var widthResult = widthDialog.showAndWait();
                    int width = 1;
                    if (widthResult.isPresent()) {
                        try {
                            width = Integer.parseInt(widthResult.get());
                            if (width < 1) width = 1;
                        } catch (NumberFormatException e1) {
                            width = 1;
                        }
                    }

                    TextInputDialog heightDialog = new TextInputDialog("1");
                    heightDialog.setTitle("Token Height");
                    heightDialog.setHeaderText("Enter token height (in tiles):");
                    heightDialog.setContentText("Height:");

                    var heightResult = heightDialog.showAndWait();
                    int height = 1;
                    if (heightResult.isPresent()) {
                        try {
                            height = Integer.parseInt(heightResult.get());
                            if (height < 1) height = 1;
                        } catch (NumberFormatException e2) {
                            height = 1;
                        }
                    }

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Select Token Image");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
                    );

                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null && file.exists()) {
                        Image tokenImage = new Image(file.toURI().toString());

                        Token token = new Token(tokenName, x * mapGrid.getTileSize(), y * mapGrid.getTileSize(),
                                tokenImage);
                        token.setWidth(width);
                        token.setHeight(height);
                        mapGrid.addToken(token);
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Image Error");
                        alert.setHeaderText("No image selected");
                        alert.setContentText("Token image not selected, token not placed.");
                        alert.showAndWait();
                    }
                }
            });

            mapGrid.setOnMousePressed(null);
            mapGrid.setOnMouseDragged(null);
            mapGrid.setOnMouseReleased(null);

        }
        else {
            mapGrid.setOnMouseClicked(null);

            mapGrid.setOnMouseDragged(e -> {
                if (e.isPrimaryButtonDown()) {
                    double adjustedX = (e.getX() - mapGrid.getOffsetX()) / mapGrid.getZoom();
                    double adjustedY = (e.getY() - mapGrid.getOffsetY()) / mapGrid.getZoom();

                    int x = (int)(adjustedX / mapGrid.getTileSize());
                    int y = (int)(adjustedY / mapGrid.getTileSize());

                    mapGrid.setTile(x, y, selectedTile);
                }
            });
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
