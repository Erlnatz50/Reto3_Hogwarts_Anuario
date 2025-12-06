package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador de la ficha individual de un personaje.
 *
 * @author Marco
 * @version 1.0
 */
public class ControladorFichaPersonaje {

    /** Contenedor principal de la tarjeta del personaje */
    @FXML
    private VBox cardBox;

    /** Imagen del personaje */
    @FXML
    private ImageView imagePersonaje;

    /** Etiquetas con los datos básicos del personaje */
    @FXML
    private Label labelNombre, labelCasa;

    /** Caja de selección visible solo en modo de selección múltiple */
    @FXML
    private CheckBox checkBoxSeleccionar;

    /** Recurso de internacionalización */
    private ResourceBundle resources;

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorFichaPersonaje.class);

    /** Identificador único del personaje (slug) */
    private String personajeSlug;

    /** Indica si el modo de selección está activo */
    private boolean isSelectionMode = false;

    /**
     *
     * @author Erlantz
     */
    @FXML
    public void initialize() {
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
    }

    /**
     * Define el identificador único (slug) del personaje mostrado.
     *
     * @param slug Identificador textual único del personaje.
     * @author Marco
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
        logger.debug("Slug asignado al personaje: {}", slug);
    }

    /**
     * Asigna la información principal del personaje a la tarjeta.
     *
     * @param nombre Nombre completo del personaje
     * @param casa Casa a la que pertenece
     * @param imagePath Ruta de la imagen asociada
     * @author Marco
     */
    public void setData(String nombre, String casa, String imagePath) {
        labelNombre.setText(nombre);
        labelCasa.setText(casa);
        logger.info("Datos cargados en la ficha: {} ({})", nombre, casa);

        try {
            // Ejemplo para cuando se integre el sistema real de imágenes:
            // imagePersonaje.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            logger.error("Error al cargar la imagen '{}': {}", imagePath, e.getMessage());
        }
    }

    /**
     * Detecta clics sobre la tarjeta.
     *
     * @author Marco
     */
    @FXML
    private void handleCardClick() {
        if (isSelectionMode) {
            boolean nuevoEstado = !checkBoxSeleccionar.isSelected();
            checkBoxSeleccionar.setSelected(nuevoEstado);
            logger.debug("Personaje '{}' {} seleccionado.", labelNombre.getText(), nuevoEstado ? "" : "no");
            return;
        }

        logger.info("Abriendo ventana de detalles para '{}'.", labelNombre.getText());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));

            if (resources == null) {
                try {
                    resources = ResourceBundle.getBundle("es.potersitos.mensaje");
                    logger.debug("Bundle de recursos cargado por defecto.");
                } catch (Exception e) {
                    logger.warn("No se pudo cargar el bundle de mensajes predeterminado: {}", e.getMessage());
                }
            }

            if (resources != null) {
                loader.setResources(resources);
            }

            Parent root = loader.load();

            ControladorDatos controladorDatos = loader.getController();
            if (personajeSlug != null) {
                controladorDatos.setPersonajeSlug(personajeSlug);
                logger.debug("Slug '{}' pasado al ControladorDatos.", personajeSlug);
            }

            Scene scene = new Scene(root);

            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloDatos.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                    logger.debug("Hoja de estilo CSS aplicada correctamente.");
                }
            } catch (Exception e) {
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            Stage stage = new Stage();
            stage.setTitle("Datos del Personaje");
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();

            logger.info("Ventana de datos abierta correctamente para '{}'.", labelNombre.getText());

        } catch (IOException e) {
            logger.error("Error al cargar la ventana de datos del personaje '{}': {}", labelNombre.getText(), e.getMessage(), e);
        }
    }

    /**
     * Alterna el modo de selección de la tarjeta.
     *
     * @param active {@code true} para activar modo selección, {@code false} para desactivarlo.
     * @author Marco
     */
    public void setSelectionMode(boolean active) {
        this.isSelectionMode = active;
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.setVisible(active);
            if (!active) {
                checkBoxSeleccionar.setSelected(false);
            }
        }
        logger.debug("Modo selección {} para '{}'.", active ? "activado" : "desactivado", labelNombre.getText());
    }

    /**
     * Indica si la tarjeta está marcada en el modo de selección.
     *
     * @return {@code true} si la tarjeta está seleccionada, {@code false} si no lo está.
     * @author Marco
     */
    public boolean isSelected() {
        boolean seleccionado = checkBoxSeleccionar != null && checkBoxSeleccionar.isSelected();
        logger.trace("Consulta de selección en '{}': {}", labelNombre.getText(), seleccionado);
        return seleccionado;
    }

    /**
     * Devuelve el nombre del personaje mostrado en la tarjeta.
     *
     * @return Nombre asignado al personaje.
     * @author Marco
     */
    public String getNombre() {
        return labelNombre.getText();
    }
}