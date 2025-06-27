package src;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;

public class TokenEditor {
    private Token token;
    private boolean confirmed = false;

    private TextField nameField;
    private ComboBox<Token.Type> typeCombo;
    private Spinner<Integer> sizeSpinner;
    private ImageView imagePreview;
    private Label imagePathLabel;
    private String selectedImagePath;
    
    public TokenEditor(Token token) {
        this.token = token;
    }
    
    public boolean showAndWait() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Token");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label nameLabel = new Label("Name:");
        nameField = new TextField(token.getName());
        nameField.setPrefWidth(200);

        Label typeLabel = new Label("Type:");
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Token.Type.values());
        typeCombo.setValue(token.getType());

        Label sizeLabel = new Label("Size (grid squares):");
        sizeSpinner = new Spinner<>(1, 10, token.getSize());
        sizeSpinner.setEditable(true);

        Label imageLabel = new Label("Custom Image:");
        HBox imageBox = new HBox(10);
        
        Button selectImageButton = new Button("Select Image");
        selectImageButton.setOnAction(e -> selectImage(dialog));
        
        Button clearImageButton = new Button("Clear Image");
        clearImageButton.setOnAction(e -> clearImage());
        
        imageBox.getChildren().addAll(selectImageButton, clearImageButton);

        imagePreview = new ImageView();
        imagePreview.setFitWidth(100);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);
        
        imagePathLabel = new Label("No image selected");
        imagePathLabel.setWrapText(true);

        if (token.hasCustomImage()) {
            imagePreview.setImage(token.getCustomImage());
            imagePathLabel.setText(token.getImagePath());
            selectedImagePath = token.getImagePath();
        }

        HBox buttonBox = new HBox(10);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        
        okButton.setOnAction(e -> {
            saveToken();
            confirmed = true;
            dialog.close();
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(okButton, cancelButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(typeLabel, 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(sizeLabel, 0, 2);
        grid.add(sizeSpinner, 1, 2);
        grid.add(imageLabel, 0, 3);
        grid.add(imageBox, 1, 3);
        grid.add(imagePreview, 0, 4);
        grid.add(imagePathLabel, 1, 4);
        
        root.getChildren().addAll(grid, buttonBox);
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
        
        return confirmed;
    }
    
    private void selectImage(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Token Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            imagePathLabel.setText(selectedImagePath);
            
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load image");
                alert.setContentText("The selected file could not be loaded as an image.");
                alert.showAndWait();
            }
        }
    }
    
    private void clearImage() {
        selectedImagePath = null;
        imagePreview.setImage(null);
        imagePathLabel.setText("No image selected");
    }
    
    private void saveToken() {
        token.setName(nameField.getText());
        token.setType(typeCombo.getValue());
        token.setSize(sizeSpinner.getValue());
        token.setImagePath(selectedImagePath);
    }
} 