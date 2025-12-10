package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
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
 * Gestiona la visualización de la tarjeta, i18n, carga de imágenes y navegación a detalles.
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

    /** Recurso de internacionalización. */
    @FXML
    private ResourceBundle resources;

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorFichaPersonaje.class);

    /** Identificador único del personaje (slug) */
    private String personajeSlug;

    /** Indica si el modo de selección está activo */
    private boolean isSelectionMode = false;

    /**
     * Inicializa el controlador.
     * Configura los tooltips traducidos.
     *
     * @author Erlantz
     */
    @FXML
    public void initialize() {
        if (this.resources == null) {
            try {
                this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("Error cargando bundle por defecto", e);
            }
        }

        if (this.resources != null) {
            try {
                Tooltip tooltipCard = new Tooltip(resources.getString("ficha.click.tooltip"));
                Tooltip.install(cardBox, tooltipCard);

                if (checkBoxSeleccionar != null) {
                    checkBoxSeleccionar.setTooltip(new Tooltip(resources.getString("ficha.seleccion.tooltip")));
                }
            } catch (Exception e) {
                logger.warn("Faltan claves de Tooltip en el ResourceBundle. Ignorando tooltips.", e);
            }
        }
    }

    /**
     * Define el identificador único (slug) del personaje mostrado.
     *
     * @param slug Identificador textual único del personaje.
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
        logger.debug("Slug asignado al personaje: {}", slug);
    }

    /**
     * Asigna la información principal del personaje a la tarjeta, incluyendo la carga de la imagen.
     *
     * @param nombre Nombre completo del personaje
     * @param casa Casa a la que pertenece
     * @param imagePath Ruta de la imagen (URL o ruta de archivo)
     */
    public void setData(String nombre, String casa, String imagePath) {
        labelNombre.setText(nombre);
        labelCasa.setText(casa);
        logger.info("Datos cargados en la ficha: {} ({})", nombre, casa);

        try {
            if (imagePath != null && !imagePath.isEmpty()) {

                String rutaPrevia = imagePath;
                final String rutaFinal;

                if (!rutaPrevia.toLowerCase().startsWith("http") && !rutaPrevia.toLowerCase().startsWith("file:")) {
                    rutaFinal = "file:/" + rutaPrevia.replace("\\", "/");
                } else {
                    rutaFinal = rutaPrevia;
                }

                Image imagen = new Image(rutaFinal, true);

                imagen.errorProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        logger.warn("Fallo al cargar la imagen de la ruta: {}. Verifique que el archivo exista.", rutaFinal);
                    }
                });

                imagePersonaje.setImage(imagen);

            } else {
                logger.warn("Ruta de imagen vacía para el personaje: {}", nombre);
            }
        } catch (Exception e) {
            logger.error("Error FATAL al intentar procesar la ruta de imagen '{}': {}", imagePath, e.getMessage(), e);
        }
    }

    /**
     * Detecta clics sobre la tarjeta.
     * Si está en modo selección, marca el checkbox.
     * Si no, abre la ventana de detalles pasando el idioma actual.
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