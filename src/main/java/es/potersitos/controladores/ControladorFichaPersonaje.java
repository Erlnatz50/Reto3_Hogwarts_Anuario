package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.io.IOException;

public class ControladorFichaPersonaje {

    private static final Logger logger = LoggerFactory.getLogger(ControladorFichaPersonaje.class);

    @FXML
    private VBox cardBox;

    @FXML
    private ImageView imagePersonaje;

    @FXML
    private Label labelNombre;

    @FXML
    private Label labelCasa;

    private ResourceBundle resources;
    private String personajeSlug; // <--- CAMPO AÑADIDO: Para almacenar el slug

    // Método que el controlador principal (ControladorVisualizarPersonajes) llama
    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    // Método que el controlador principal llama para pasar el SLUG
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
        logger.debug("Ficha cargada con slug: " + slug);
    }

    // Método setData para configurar los Labels y la ruta de la imagen
    public void setData(String nombre, String casa, String imagePath) {
        labelNombre.setText(nombre);
        labelCasa.setText(casa);

        // Lógica de carga de imagen
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // imagePersonaje.setImage(new Image(imagePath, true));
            } catch (Exception e) {
                logger.error("Error al cargar imagen para la ficha: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        logger.info("Ficha de personaje (Slug: {}) clickeada. Abriendo detalles.", personajeSlug);
        try {
            // Se asume que ventanaDatos.fxml existe y usa ControladorDatos.java
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));

            // Pasamos el ResourceBundle a la ventana de detalles
            if (resources != null) {
                loader.setResources(resources);
            }
            javafx.scene.Parent root = loader.load();

            // Si necesitaras el slug en ControladorDatos, lo pasas aquí:
            // ControladorDatos datosController = loader.getController();
            // datosController.cargarDatosPersonaje(personajeSlug);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            var archivoCSS = getClass().getResource("/es/potersitos/css/estiloDatos.css");
            if (archivoCSS != null) {
                root.getStylesheets().add(archivoCSS.toExternalForm());
            }
            stage.setTitle(labelNombre.getText()); // Título dinámico
            stage.setScene(new javafx.scene.Scene(root));
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();
        } catch (IOException e) {
            logger.error("Error al cargar la ventana de detalles (ventanaDatos.fxml)", e);
        }
    }
}