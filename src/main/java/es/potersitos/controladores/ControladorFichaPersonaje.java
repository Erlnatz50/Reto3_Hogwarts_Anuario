package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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

    // [NUEVO CAMPO] Almacena el identificador único
    private String personajeSlug;

    private ResourceBundle resources;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    /**
     * [NUEVO MÉTODO] Setter para recibir y almacenar el SLUG.
     * Es llamado desde ControladorVisualizarPersonajes.
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
    }


    public void setData(String nombre, String casa, String imagePath) {
        labelNombre.setText(nombre);
        labelCasa.setText(casa);
        // Placeholder for image loading logic
        try {
            // In a real app, load from resource or URL
            // imagePersonaje.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));

            // ===================================================================
            // [CORRECCIÓN CLAVE]: Asegurar que el ResourceBundle esté cargado
            // ===================================================================
            if (resources == null) {
                // Carga de respaldo si el bundle no fue inyectado
                resources = ResourceBundle.getBundle("es.potersitos.mensaje");
            }
            loader.setResources(resources);

            // Cargar el FXML
            javafx.scene.Parent root = loader.load();

            // [NUEVO] Obtener el controlador de la ventana de datos
            ControladorDatos controladorDatos = loader.getController();

            // [CRÍTICO] Pasar el SLUG almacenado a la ventana de destino
            if (this.personajeSlug != null) {
                controladorDatos.setPersonajeSlug(this.personajeSlug);
            }
            // ===================================================================

            // Configurar la Scene ANTES de añadirla al Stage
            javafx.scene.Scene scene = new javafx.scene.Scene(root);

            // Establecer el fondo de la Scene a transparente
            scene.setFill(Color.TRANSPARENT);

            // Cargar CSS
            var archivoCSS = getClass().getResource("/es/potersitos/css/estiloDatos.css");
            if (archivoCSS != null) {
                scene.getStylesheets().add(archivoCSS.toExternalForm());
            }

            // Configurar el Stage
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Datos del Personaje");
            stage.setScene(scene);

            // Eliminar los bordes nativos del sistema
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}