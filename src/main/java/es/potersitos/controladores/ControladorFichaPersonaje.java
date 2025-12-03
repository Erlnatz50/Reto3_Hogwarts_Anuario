package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;

import java.io.IOException;
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

    @FXML
    private CheckBox checkBoxSeleccionar;

    private ResourceBundle resources;
    private String personajeSlug;
    private boolean isSelectionMode = false;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
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
        if (isSelectionMode) {
            checkBoxSeleccionar.setSelected(!checkBoxSeleccionar.isSelected());
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));

            if (resources == null) {
                try {
                    resources = ResourceBundle.getBundle("es.potersitos.mensaje");
                } catch (Exception e) {
                    // Ignore if bundle not found
                }
            }
            if (resources != null) {
                loader.setResources(resources);
            }

            javafx.scene.Parent root = loader.load();

            // Pass slug to ControladorDatos
            ControladorDatos controladorDatos = loader.getController();
            if (this.personajeSlug != null) {
                controladorDatos.setPersonajeSlug(this.personajeSlug);
            }

            javafx.scene.Scene scene = new javafx.scene.Scene(root);

            // Try to load CSS if it exists
            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloDatos.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("Error loading CSS: " + e.getMessage());
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Datos del Personaje");
            stage.setScene(scene);

            // Remove system borders
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSelectionMode(boolean active) {
        this.isSelectionMode = active;
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.setVisible(active);
            if (!active) {
                checkBoxSeleccionar.setSelected(false);
            }
        }
    }

    public boolean isSelected() {
        return checkBoxSeleccionar != null && checkBoxSeleccionar.isSelected();
    }

    public String getNombre() {
        return labelNombre.getText();
    }
}