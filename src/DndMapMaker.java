package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DndMapMaker extends Application {
    private MapCanvas mapCanvas;

    @Override
    public void start(Stage stage) {
        mapCanvas = new MapCanvas();

        ToolBar toolbar = new ToolBar();

        Button wallButton = new Button("Wall");
        wallButton.setOnAction(e -> mapCanvas.setMode(MapCanvas.Mode.WALL));
        toolbar.getItems().add(wallButton);

        Button eraserButton = new Button("Eraser");
        eraserButton.setOnAction(e -> mapCanvas.setMode(MapCanvas.Mode.ERASER));
        toolbar.getItems().add(eraserButton);

        Button tokenButton = new Button("Token");
        tokenButton.setOnAction(e -> mapCanvas.setMode(MapCanvas.Mode.TOKEN));
        toolbar.getItems().add(tokenButton);

        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> mapCanvas.setMode(MapCanvas.Mode.SELECT));
        toolbar.getItems().add(selectButton);

        Button editTokenButton = new Button("Edit Token");
        editTokenButton.setOnAction(e -> {
            mapCanvas.setMode(MapCanvas.Mode.SELECT);
        });
        toolbar.getItems().add(editTokenButton);

        ComboBox<Token.Type> tokenTypeCombo = new ComboBox<>();
        tokenTypeCombo.getItems().addAll(Token.Type.values());
        tokenTypeCombo.setValue(Token.Type.PLAYER);
        tokenTypeCombo.setOnAction(e -> {
            Token.Type selected = tokenTypeCombo.getValue();
            if (selected != null) {
                mapCanvas.setTokenType(selected);
            }
        });
        toolbar.getItems().add(tokenTypeCombo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getItems().add(spacer);

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> mapCanvas.undo());
        toolbar.getItems().add(undoButton);
        
        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> mapCanvas.redo());
        toolbar.getItems().add(redoButton);
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveMap(stage));
        toolbar.getItems().add(saveButton);

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> loadMap(stage));
        toolbar.getItems().add(loadButton);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(mapCanvas);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("D&D Map Maker");
        stage.setScene(scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            mapCanvas.resizeCanvas(scene.getWidth(), scene.getHeight() - toolbar.getHeight());
        });
        
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            mapCanvas.resizeCanvas(scene.getWidth(), scene.getHeight() - toolbar.getHeight());
        });
        
        stage.show();
    }
    
    private void saveMap(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Map");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                MapIO.saveMap(mapCanvas.getWalls(), mapCanvas.getTokens(), file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void loadMap(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Map");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                MapIO.MapData mapData = MapIO.loadMap(file);
                if (mapData.walls != null) {
                    mapCanvas.setWalls(mapData.walls);
                }
                if (mapData.tokens != null) {
                    mapCanvas.setTokens(mapData.tokens);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 