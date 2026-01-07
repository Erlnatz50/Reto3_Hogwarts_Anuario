package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador asociado a la vista de una ficha de personaje individual.
 * Gestiona los datos visuales y el comportamiento interactivo de cada tarjeta
 * (nombre, casa, imagen y selección). También permite abrir una ventana con
 * más detalles del personaje.
 *
 * @author Marco
 * @version 1.0
 */
public class ControladorFichaPersonaje {

    /** Imagen del personaje mostrada en la tarjeta. */
    @FXML
    private ImageView imagePersonaje;

    /** Etiquetas que muestran el nombre y la casa del personaje. */
    @FXML
    private Label labelNombre, labelCasa;

    /** Checkbox usado para activar o desactivar la selección del personaje. */
    @FXML
    private CheckBox checkBoxSeleccionar;

    /** Recurso de internacionalización (traducciones de la interfaz). */
    @FXML
    private ResourceBundle resources;

    /** Logger para registrar información y errores en tiempo de ejecución. */
    private static final Logger logger = LoggerFactory.getLogger(ControladorFichaPersonaje.class);

    /** Identificador único del personaje (slug). */
    private String personajeSlug;

    /** Indica si la tarjeta se encuentra en modo selección. */
    private boolean isSelectionMode = false;

    /** Acción a ejecutar cuando cambia el estado del checkbox. */
    private Runnable onSelectionChanged;

    /** Acción a ejecutar al cerrar la ventana de detalles para refrescar datos. */
    private Runnable onRefresh;

    /** Ruta local donde se buscan imágenes de personajes */
    private static final String RUTA_LOCAL_IMAGENES = System.getProperty("user.home") + File.separator + "Reto3_Hogwarts_Anuario" + File.separator + "imagenes" + File.separator;

    /**
     * Inicializa el controlador y asigna el {@link ResourceBundle} de idioma.
     * Se ejecuta automáticamente tras la carga del FXML.
     *
     * @author Marco
     */
    @FXML
    public void initialize() {
        if (resources == null) {
            try {
                resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.warn("No se ha podido cargar el ResourceBundle por defecto", e);
            }
        }
    }

    /**
     * Asigna el slug asociado al personaje.
     *
     * @param slug Identificador único del personaje.
     * @author Erlantz
     */
    public void setPersonajeSlug(String slug) {
        personajeSlug = slug;
    }

    /**
     * Carga los datos visuales del personaje en la tarjeta: nombre, casa e imagen.
     *
     * @param nombre    Nombre del personaje.
     * @param casa      Casa de Hogwarts a la que pertenece.
     * @param imagenArchivo Slug del personaje
     * @author Nizam
     */
    public void setData(String nombre, String casa, String imagenArchivo) {
        labelNombre.setText(formatearTexto(nombre));
        labelCasa.setText(formatearTexto(casa));
        aplicarEstiloCasa(casa);

        cargarImagen(imagenArchivo);
    }

    private void cargarImagen(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            cargarImagenPorDefecto();
            return;
        }

        File archivo = Paths.get(RUTA_LOCAL_IMAGENES, nombreArchivo).toFile();
        logger.info("Cargando imagen: {}", archivo.getAbsolutePath());

        if (archivo.exists()) {
            imagePersonaje.setImage(new Image(archivo.toURI().toString(), true));
        } else {
            logger.warn("Imagen no encontrada: {}", archivo.getAbsolutePath());
            cargarImagenPorDefecto();
        }
    }

    /**
     * Carga una imagen por defecto si no se encuentra imagen específica del
     * personaje.
     *
     * @author Nizam
     */
    private void cargarImagenPorDefecto() {
        try (InputStream stream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png")) {
            if (stream != null) {
                imagePersonaje.setImage(new Image(stream));
            }
        } catch (Exception e) {
            logger.warn("No se ha podido cargar la imagen por defecto", e);
        }
    }

    /**
     * Formatea el texto recibido, capitalizando la primera letra de cada palabra.
     *
     * @param texto Texto original.
     * @return Texto formateado en estilo “Título”.
     * @author Nizam
     */
    private String formatearTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            sb.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Aplica un color distintivo a la etiqueta de casa según la casa del personaje.
     *
     * @param casa Nombre de la casa (Gryffindor, Slytherin, Ravenclaw y Hufflepuff).
     * @author Nizam
     */
    private void aplicarEstiloCasa(String casa) {
        if (casa == null) return;
        String estiloBase = "-fx-font-weight: bold; -fx-text-fill: ";
        switch (casa.toLowerCase().trim()) {
            case "gryffindor" -> labelCasa.setStyle(estiloBase + "#740001;");
            case "slytherin" -> labelCasa.setStyle(estiloBase + "#1a472a;");
            case "ravenclaw" -> labelCasa.setStyle(estiloBase + "#0e1a40;");
            case "hufflepuff" -> labelCasa.setStyle(estiloBase + "#ecb939;");
            default -> labelCasa.setStyle(estiloBase + "#555555;");
        }
    }

    /**
     * Maneja el clic sobre la tarjeta del personaje.
     * Si el modo selección está activo, activa el checkbox,
     * de lo contrario, abre la vista de detalles del personaje.
     *
     * @author Marco
     */
    @FXML
    private void handleCardClick() {
        if (isSelectionMode) {
            checkBoxSeleccionar.setSelected(!checkBoxSeleccionar.isSelected());
        } else {
            abrirDetalles();
        }
    }

    /**
     * Abre una nueva ventana con los detalles completos del personaje seleccionado.
     *
     * @author Marco
     */
    private void abrirDetalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));
            loader.setResources(resources);

            Parent root = loader.load();
            ControladorDatos cd = loader.getController();
            cd.setPersonajeSlug(personajeSlug);

            Scene scene = new Scene(root);
            var css = getClass().getResource("/es/potersitos/css/estiloDatos.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setOnHidden(e -> {
                if (onRefresh != null) {
                    onRefresh.run();
                }
            });
            stage.show();

        } catch (IOException e) {
            logger.error("Error al abrir la ventana de detalles", e);
        }
    }

    /**
     * Activa o desactiva el modo selección de la tarjeta.
     *
     * @param active {@code true} para mostrar el checkbox, {@code false} para ocultarlo.
     * @author Telmo
     */
    public void setSelectionMode(boolean active) {
        isSelectionMode = active;
        checkBoxSeleccionar.setVisible(active);
        if (!active) {
            checkBoxSeleccionar.setSelected(false);
        }
    }

    /**
     * Establece una acción (callback) a ejecutar cuando cambia la selección del checkbox.
     *
     * @param listener Acción a ejecutar al cambiar el estado del checkbox.
     * @author Telmo
     */
    public void setOnSelectionChanged(Runnable listener) {
        onSelectionChanged = listener;
        checkBoxSeleccionar.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (onSelectionChanged != null) {
                onSelectionChanged.run();
            }
        });
    }

    /**
     * Comprueba si el personaje está seleccionado.
     *
     * @return {@code true} si el checkbox está marcado.
     * @author Marco
     */
    public boolean isSelected() {
        return checkBoxSeleccionar.isSelected();
    }

    /**
     * Devuelve el identificador único asociado al personaje.
     *
     * @return Slug del personaje.
     * @author Marco
     */
    public String getPersonajeSlug() {
        return personajeSlug;
    }

    /**
     * Marca o desmarca manualmente el checkbox de selección.
     *
     * @param value {@code true} para marcar, {@code false} para desmarcar.
     * @author Erlantz
     */
    public void setSelected(boolean value) {
        checkBoxSeleccionar.setSelected(value);
    }

    /**
     * Establece un listener que se ejecutará al cerrar la ventana de detalles
     * para refrescar la vista principal.
     *
     * @param recargarListaCompleta acción de refresco
     * @author Telmo
     */
    public void setOnRefreshListener(Runnable recargarListaCompleta) {
        onRefresh = recargarListaCompleta;
    }
}