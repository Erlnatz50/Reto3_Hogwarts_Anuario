package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

public class ControladorFichaPersonaje {

    @FXML
    private VBox cardBox;

    @FXML
    private ImageView imagePersonaje;

    @FXML
    private Label labelNombre;

    @FXML
    private Label labelCasa;

    private ResourceBundle resources;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));
            if (resources != null) {
                loader.setResources(resources);
            }
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            var archivoCSS = getClass().getResource("/es/potersitos/css/estiloDatos.css");
            if (archivoCSS != null) {
                root.getStylesheets().add(archivoCSS.toExternalForm());
            }
            stage.setTitle("Datos del Personaje");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }}

