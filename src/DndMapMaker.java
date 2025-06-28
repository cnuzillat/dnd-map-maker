package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

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

        Button blackOverlayButton = new Button("Black Overlay");
        blackOverlayButton.setOnAction(e -> mapCanvas.setMode(MapCanvas.Mode.BLACK_OVERLAY));
        toolbar.getItems().add(blackOverlayButton);

        Button layerButton = new Button("Layer");
        layerButton.setOnAction(e -> mapCanvas.setMode(MapCanvas.Mode.LAYER));
        toolbar.getItems().add(layerButton);

        Button editTokenButton = new Button("Edit Token");
        editTokenButton.setOnAction(e -> {
            mapCanvas.setMode(MapCanvas.Mode.SELECT);
        });
        toolbar.getItems().add(editTokenButton);

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

        VBox layerPanel = createLayerPanel();
        System.out.println("Side panel created, width: " + layerPanel.getPrefWidth());

        ScrollPane scrollPane = new ScrollPane(layerPanel);
        scrollPane.setPrefWidth(220);
        scrollPane.setMinWidth(220);
        scrollPane.setMaxWidth(220);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #e0e0e0;");
        
        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(mapCanvas);
        root.setRight(scrollPane);
        
        System.out.println("BorderPane created with right panel");

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("D&D Map Maker");
        stage.setScene(scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double panelWidth = 220;
            mapCanvas.resizeCanvas(scene.getWidth() - panelWidth, scene.getHeight() - toolbar.getHeight());
        });
        
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double panelWidth = 220;
            mapCanvas.resizeCanvas(scene.getWidth() - panelWidth, scene.getHeight() - toolbar.getHeight());
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
                MapIO.saveMap(mapCanvas.getWalls(), mapCanvas.getTokens(), mapCanvas.getBlackOverlays(), file);
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
                if (mapData.blackOverlays != null) {
                    mapCanvas.setBlackOverlays(mapData.blackOverlays);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private VBox createLayerPanel() {
        VBox layerPanel = new VBox(10);
        layerPanel.setPrefWidth(200);
        layerPanel.setMinWidth(200);
        layerPanel.setMaxWidth(200);
        layerPanel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 10; -fx-border-color: #999999; -fx-border-width: 1;");

        Label titleLabel = new Label("Layer Management");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #333333;");
        layerPanel.getChildren().add(titleLabel);

        Label currentLayerLabel = new Label("Current Layer:");
        currentLayerLabel.setStyle("-fx-font-weight: bold;");
        layerPanel.getChildren().add(currentLayerLabel);

        ComboBox<String> currentLayerCombo = new ComboBox<>();
        currentLayerCombo.setPrefWidth(180);
        currentLayerCombo.setOnAction(e -> {
            String selected = currentLayerCombo.getValue();
            if (selected != null) {
                mapCanvas.setCurrentLayerCategory(selected);
                System.out.println("Current layer set to: " + selected);
            }
        });
        layerPanel.getChildren().add(currentLayerCombo);

        Label orderLabel = new Label("Layer Order (top to bottom):");
        orderLabel.setStyle("-fx-font-weight: bold;");
        layerPanel.getChildren().add(orderLabel);

        VBox layerList = new VBox(5);
        layerPanel.getChildren().add(layerList);

        Button addLayerButton = new Button("Add New Layer");
        addLayerButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("New Layer");
            dialog.setTitle("Add New Layer");
            dialog.setHeaderText("Enter layer name:");
            dialog.setContentText("Layer name:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String newLayerName = result.get().trim();
                mapCanvas.addLayer(newLayerName);
                updateLayerList(layerList, currentLayerCombo);
            }
        });
        layerPanel.getChildren().add(addLayerButton);

        updateLayerList(layerList, currentLayerCombo);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        layerPanel.getChildren().add(spacer);

        System.out.println("Layer panel created with " + layerPanel.getChildren().size() + " children");
        return layerPanel;
    }
    
    private void updateLayerList(VBox layerList, ComboBox<String> currentLayerCombo) {
        layerList.getChildren().clear();

        currentLayerCombo.getItems().clear();
        currentLayerCombo.getItems().addAll(mapCanvas.getLayerOrder());
        if (!currentLayerCombo.getItems().isEmpty()) {
            currentLayerCombo.setValue(currentLayerCombo.getItems().get(0));
        }

        for (String layerName : mapCanvas.getLayerOrder()) {
            HBox layerItem = new HBox(5);
            
            CheckBox layerCheckBox = new CheckBox(layerName);
            layerCheckBox.setSelected(mapCanvas.getVisibleLayers().contains(layerName));
            layerCheckBox.setOnAction(e -> {
                mapCanvas.toggleLayer(layerName);
                System.out.println("Toggled layer: " + layerName + " (selected: " + layerCheckBox.isSelected() + ")");
            });
            
            Button upButton = new Button("↑");
            upButton.setPrefWidth(30);
            upButton.setOnAction(e -> {
                mapCanvas.moveLayerUp(layerName);
                updateLayerList(layerList, currentLayerCombo);
            });
            
            Button downButton = new Button("↓");
            downButton.setPrefWidth(30);
            downButton.setOnAction(e -> {
                mapCanvas.moveLayerDown(layerName);
                updateLayerList(layerList, currentLayerCombo);
            });
            
            Button removeButton = new Button("×");
            removeButton.setPrefWidth(30);
            removeButton.setOnAction(e -> {
                mapCanvas.removeLayer(layerName);
                updateLayerList(layerList, currentLayerCombo);
            });
            
            layerItem.getChildren().addAll(layerCheckBox, upButton, downButton, removeButton);
            layerList.getChildren().add(layerItem);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 