package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
 * Gestiona la visualización de la tarjeta y la navegación a detalles.
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

    /** * Recurso de internacionalización.
     * IMPORTANTE: La etiqueta @FXML permite que el FXMLLoader inyecte aquí
     * el idioma que viene de la ventana principal (ControladorVisualizarPersonajes).
     */
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
        // Si por alguna razón no se inyectó (ej. pruebas unitarias), cargamos el defecto
        if (this.resources == null) {
            try {
                this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("Error cargando bundle por defecto", e);
            }
        }

        // Aplicar Tooltips usando las claves del archivo de propiedades
        if (this.resources != null) {
            // Tooltip para la tarjeta completa
            try {
                Tooltip tooltipCard = new Tooltip(resources.getString("ficha.click.tooltip"));
                Tooltip.install(cardBox, tooltipCard);

                // Tooltip para el checkbox de selección
                if (checkBoxSeleccionar != null) {
                    checkBoxSeleccionar.setTooltip(new Tooltip(resources.getString("ficha.seleccion.tooltip")));
                }
            } catch (Exception e) {
                // Ignorar si falta alguna clave en el properties
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
    }

    /**
     * Asigna la información principal del personaje a la tarjeta.
     *
     * @param nombre Nombre completo del personaje
     * @param casa Casa a la que pertenece
     * @param imagePath Ruta de la imagen asociada
     */
    public void setData(String nombre, String casa, String imagePath) {
        labelNombre.setText(nombre);
        labelCasa.setText(casa);

        try {
            // TODO: Implementar carga real de imágenes
            // imagePersonaje.setImage(new Image(imagePath));
        } catch (Exception e) {
            logger.error("Error al cargar la imagen '{}': {}", imagePath, e.getMessage());
        }
    }

    /**
     * Detecta clics sobre la tarjeta.
     * Si está en modo selección, marca el checkbox.
     * Si no, abre la ventana de detalles pasando el idioma actual.
     */
    @FXML
    private void handleCardClick() {
        // 1. Lógica de Selección
        if (isSelectionMode) {
            boolean nuevoEstado = !checkBoxSeleccionar.isSelected();
            checkBoxSeleccionar.setSelected(nuevoEstado);
            return;
        }

        // 2. Lógica de Apertura de Detalles
        logger.info("Abriendo ventana de detalles para '{}'.", labelNombre.getText());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));

            // ¡IMPORTANTE!: Pasamos el 'resources' actual (que ya tiene el idioma correcto)
            // al siguiente cargador.
            if (resources != null) {
                loader.setResources(resources);
            }

            Parent root = loader.load();

            ControladorDatos controladorDatos = loader.getController();
            if (personajeSlug != null) {
                controladorDatos.setPersonajeSlug(personajeSlug);
            }

            Scene scene = new Scene(root);

            // Cargar estilos CSS
            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloDatos.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                }
            } catch (Exception e) {
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            Stage stage = new Stage();
            stage.setTitle("Datos del Personaje");
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();

        } catch (IOException e) {
            logger.error("Error al cargar la ventana de datos: {}", e.getMessage(), e);
        }
    }

    /**
     * Alterna el modo de selección de la tarjeta.
     */
    public void setSelectionMode(boolean active) {
        this.isSelectionMode = active;
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.setVisible(active);
            if (!active) {
                checkBoxSeleccionar.setSelected(false);
            }
        }
    }

    /**
     * Indica si la tarjeta está marcada en el modo de selección.
     */
    public boolean isSelected() {
        return checkBoxSeleccionar != null && checkBoxSeleccionar.isSelected();
    }

    /**
     * Devuelve el nombre del personaje mostrado en la tarjeta.
     */
    public String getNombre() {
        return labelNombre.getText();
    }
}