package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;

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
     * @param nombre        Nombre del personaje.
     * @param casa          Casa de Hogwarts a la que pertenece.
     * @param imagenArchivo Slug del personaje
     * @author Nizam
     */
    public void setData(String nombre, String casa, String imagenArchivo) {
        labelNombre.setText(formatearTexto(nombre));
        labelCasa.setText(formatearTexto(casa));

        if (casa != null) {
            String estiloBase = "-fx-font-weight: bold; -fx-text-fill: ";
            switch (casa.toLowerCase().trim()) {
                case "gryffindor" -> labelCasa.setStyle(estiloBase + "#740001;");
                case "slytherin" -> labelCasa.setStyle(estiloBase + "#1a472a;");
                case "ravenclaw" -> labelCasa.setStyle(estiloBase + "#0e1a40;");
                case "hufflepuff" -> labelCasa.setStyle(estiloBase + "#ecb939;");
                default -> labelCasa.setStyle(estiloBase + "#555555;");
            }
        }

        logger.info("=== DEBUG CARGA IMAGEN: {} ===", imagenArchivo);

        if (imagenArchivo != null && !imagenArchivo.isBlank()) {
            File archivo = Paths.get(RUTA_LOCAL_IMAGENES, imagenArchivo).toFile();
            if (cargarArchivoImagen(archivo, imagenArchivo)) return;
        }

        if (personajeSlug != null && !personajeSlug.isBlank()) {
            String[] extensiones = { ".jpg", ".png", ".jpeg", ".webp", ".JPG", ".PNG", ".JPEG", ".WEBP" };
            for (String ext : extensiones) {
                File archivo = Paths.get(RUTA_LOCAL_IMAGENES, personajeSlug + ext).toFile();
                if (cargarArchivoImagen(archivo, personajeSlug + ext)) return;
            }
        }

        try (InputStream stream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png")) {
            if (stream != null) {
                imagePersonaje.setImage(new Image(stream));
            }
        } catch (Exception e) {
            logger.warn("No se ha podido cargar la imagen por defecto", e);
        }

    }

    /**
     * Intenta cargar un archivo de imagen local con soporte WebP.
     *
     * @param archivo archivo de imagen a cargar
     * @param nombreDebug nombre para logging/debugging
     * @return {@code true} si se cargó exitosamente, {@code false} si falló
     * @author Telmo
     */
    private boolean cargarArchivoImagen(File archivo, String nombreDebug) {
        logger.info("Probando: {} ({} bytes)", archivo.getAbsolutePath(), archivo.length());

        if (!archivo.exists()) return false;

        try {
            Image imagen = null;
            if (archivo.getName().toLowerCase().endsWith(".webp")) {
                try {
                    BufferedImage bi = ImageIO.read(archivo);
                    if (bi != null) {
                        imagen = SwingFXUtils.toFXImage(bi, null);
                    }
                } catch (Exception e) {
                    logger.warn("Fallo carga WebP con ImageIO: {}", e.getMessage());
                }
            }

            if (imagen == null) {
                imagen = new Image(archivo.toURI().toString());
            }

            imagePersonaje.setImage(imagen);
            logger.info("IMAGEN ASIGNADA (o intentando cargar): {}", nombreDebug);
            return true;

        } catch (Exception e) {
            logger.error("Error cargando {}: {}", nombreDebug, e.getMessage());
            return false;
        }
    }

    /**
     * Formatea el texto recibido, capitalizando la primera letra de cada palabra.
     *
     * @param texto Texto original.
     * @return Texto formateado en estilo “Título”.
     * @author Erlantz
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
     * Maneja el clic sobre la tarjeta del personaje.
     * Si modo selección activo → toggle checkbox, sino → abrir detalles.
     *
     * @author Erlantz
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
            scene.setFill(Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setOnHidden(e -> {
                if (onRefresh != null) onRefresh.run();
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
        if (!active) checkBoxSeleccionar.setSelected(false);
    }

    /**
     * Establece una acción (callback) a ejecutar cuando cambia la selección del
     * checkbox.
     *
     * @param listener Acción a ejecutar al cambiar el estado del checkbox.
     * @author Telmo
     */
    public void setOnSelectionChanged(Runnable listener) {
        onSelectionChanged = listener;
        checkBoxSeleccionar.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (onSelectionChanged != null) onSelectionChanged.run();
        });
    }

    /**
     * Comprueba si el personaje está seleccionado.
     *
     * @return {@code true} si el checkbox está marcado.
     * @author Erlantz
     */
    public boolean isSelected() {
        return checkBoxSeleccionar.isSelected();
    }

    /**
     * Devuelve el identificador único asociado al personaje.
     *
     * @return Slug del personaje.
     * @author Erlantz
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
     * Establece un listener que se ejecutará al cerrar la ventana de detalles para refrescar la vista principal.
     *
     * @param recargarListaCompleta acción de refresco
     * @author Erlantz
     */
    public void setOnRefreshListener(Runnable recargarListaCompleta) {
        onRefresh = recargarListaCompleta;
    }
}