package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ControladorFichaPersonaje {

    @FXML
    private VBox cardBox;

    @FXML
    private ImageView imagePersonaje;

    @FXML
    private Label labelNombre;

    @FXML
    private Label labelCasa;

    private boolean isSelected = false;

    public void setData(String nombre, String casa, String imagePath) {
        labelNombre.setText(nombre);
        labelCasa.setText(casa);
        // Placeholder for image loading logic
        try {
            // In a real app, load from resource or URL
            // imagePersonaje.setImage(new
            // Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        isSelected = !isSelected;
        updateSelectionVisuals();
    }

    private void updateSelectionVisuals() {
        if (isSelected) {
            cardBox.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-background-color: #e0e0e0;");
        } else {
            cardBox.setStyle("-fx-border-color: transparent; -fx-border-width: 0; -fx-background-color: white;");
        }
    }
}
