package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /** Contenedor principal del diseño de la tarjeta del personaje. */
    @FXML
    private VBox cardBox;

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

    private Runnable onRefresh;

    /** Ruta local donde se encuentran las imágenes de personajes. */
    private static final String RUTA_LOCAL_IMAGENES = "C:\\Users\\dm2\\Reto3_Hogwarts_Anuario\\imagenes\\";

    /**
     * Inicializa el controlador y asigna el {@link ResourceBundle} de idioma.
     * Se ejecuta automáticamente tras la carga del FXML.
     *
     * @author Marco
     */
    @FXML
    public void initialize() {
        if (this.resources == null) {
            try {
                this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception _) {
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
        this.personajeSlug = slug;
    }

    /**
     * Carga los datos visuales del personaje en la tarjeta: nombre, casa e imagen.
     *
     * @param nombre    Nombre del personaje.
     * @param casa      Casa de Hogwarts a la que pertenece.
     * @param imagePath Ruta o nombre del archivo de imagen.
     * @author Nizam
     */
    public void setData(String nombre, String casa, String imagePath) {
        String nombreBonito = formatearTexto(nombre);
        labelNombre.setText(nombreBonito);
        labelCasa.setText(formatearTexto(casa));
        aplicarEstiloCasa(casa);

        try {
            boolean encontrado = false;

            if (nombre != null && !nombre.isBlank()) {
                String base1 = nombre.toLowerCase().trim().replaceAll("\\s+", "-");
                encontrado = intentarCargarVariasExtensiones(base1);
            }

            if (!encontrado && !nombreBonito.isEmpty()) {
                String base2 = nombreBonito.replaceAll("\\s+", "-");
                encontrado = intentarCargarVariasExtensiones(base2) || intentarCargarVariasExtensiones(nombreBonito);
            }

            if (!encontrado && imagePath != null && !imagePath.isBlank() && !"null".equalsIgnoreCase(imagePath)) {
                String baseCSV = limpiaNombreArchivo(imagePath);
                encontrado = intentarCargarVariasExtensiones(baseCSV);
            }

            if (!encontrado) {
                cargarImagenPorDefecto();
            }

        } catch (Exception e) {
            logger.error("Error al cargar la imagen del personaje", e);
            cargarImagenPorDefecto();
        }
    }

    /**
     * Ruta o nombre del archivo de imagen.
     *
     * @param nombreBase Nombre base del archivo de imagen.
     * @return {@code true} si se encontró y cargó una imagen válida, {@code false}
     *         en caso contrario.
     * @author Nizam
     */
    private boolean intentarCargarVariasExtensiones(String nombreBase) {
        if (cargarImagenFuerzaBruta(nombreBase + ".png"))
            return true;
        if (cargarImagenFuerzaBruta(nombreBase + ".jpg"))
            return true;
        return cargarImagenFuerzaBruta(nombreBase + ".jpeg");
    }

    /**
     * Carga el archivo de imagen desde disco usando {@link FileInputStream}.
     *
     * @param nombreArchivo Nombre del archivo a buscar en la ruta local.
     * @return {@code true} si la imagen fue cargada correctamente.
     * @author Nizam
     */
    private boolean cargarImagenFuerzaBruta(String nombreArchivo) {
        File archivo = new File(RUTA_LOCAL_IMAGENES + nombreArchivo);
        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                imagePersonaje.setImage(new Image(fis));
                return true;
            } catch (IOException e) {
                logger.warn("No se ha podido cargar la imagen: {}", archivo.getName());
                return false;
            }
        }
        return false;
    }

    /**
     * Formatea el texto recibido, capitalizando la primera letra de cada palabra.
     *
     * @param texto Texto original.
     * @return Texto formateado en estilo “Título”.
     * @author Nizam
     */
    private String formatearTexto(String texto) {
        if (texto == null || texto.isEmpty())
            return "";
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder res = new StringBuilder();
        for (String p : palabras)
            if (!p.isEmpty()) {
                res.append(Character.toUpperCase(p.charAt(0)))
                        .append(p.substring(1).toLowerCase())
                        .append(" ");
            }
        return res.toString().trim();
    }

    /**
     * Limpia el nombre del archivo extraído de una URL o ruta, eliminando
     * extensiones y caracteres especiales.
     *
     * @param ruta Ruta o URL con el nombre del archivo.
     * @return Nombre simple y limpio del archivo.
     * @author Nizam
     */
    private String limpiaNombreArchivo(String ruta) {
        String nombre = ruta;
        if (nombre.contains("/"))
            nombre = nombre.substring(nombre.lastIndexOf("/") + 1);
        if (nombre.contains("?"))
            nombre = nombre.substring(0, nombre.indexOf("?"));
        if (nombre.contains("."))
            nombre = nombre.substring(0, nombre.lastIndexOf("."));
        return nombre.replace("%20", " ");
    }

    /**
     * Aplica un color distintivo a la etiqueta de casa según la casa del personaje.
     *
     * @param casa Nombre de la casa (Gryffindor, Slytherin, Ravenclaw y
     *             Hufflepuff).
     * @author Nizam
     */
    private void aplicarEstiloCasa(String casa) {
        if (casa == null)
            return;
        String estilo = "-fx-font-weight: bold; -fx-text-fill: ";
        switch (casa.toLowerCase().trim()) {
            case "gryffindor" -> labelCasa.setStyle(estilo + "#740001;");
            case "slytherin" -> labelCasa.setStyle(estilo + "#1a472a;");
            case "ravenclaw" -> labelCasa.setStyle(estilo + "#0e1a40;");
            case "hufflepuff" -> labelCasa.setStyle(estilo + "#ecb939;");
            default -> labelCasa.setStyle(estilo + "#555555;");
        }
    }

    /**
     * Carga una imagen por defecto si no se encuentra imagen específica del
     * personaje.
     *
     * @author Nizam
     */
    private void cargarImagenPorDefecto() {
        try {
            InputStream stream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
            if (stream != null)
                imagePersonaje.setImage(new Image(stream));
        } catch (Exception e) {
            logger.warn("No se ha podido cargar la imagen por defecto");
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
            if (resources != null) {
                loader.setResources(resources);
            }

            Parent root = loader.load();
            ControladorDatos cd = loader.getController();
            if (personajeSlug != null) {
                cd.setPersonajeSlug(personajeSlug);
            }

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
                if (onRefresh != null)
                    onRefresh.run();
            });
            stage.show();

        } catch (IOException e) {
            logger.error("Error al abrir la ventana de detalles", e);
        }
    }

    /**
     * Activa o desactiva el modo selección de la tarjeta.
     *
     * @param active {@code true} para mostrar el checkbox, {@code false} para
     *               ocultarlo.
     * @author Telmo
     */
    public void setSelectionMode(boolean active) {
        isSelectionMode = active;
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.setVisible(active);
            if (!active)
                checkBoxSeleccionar.setSelected(false);
        }
    }

    /**
     * Establece una acción (callback) a ejecutar cuando cambia la selección del
     * checkbox.
     *
     * @param listener Acción a ejecutar al cambiar el estado del checkbox.
     * @author Telmo
     */
    public void setOnSelectionChanged(Runnable listener) {
        this.onSelectionChanged = listener;
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.selectedProperty().addListener((o, ov, nv) -> {
                if (onSelectionChanged != null) {
                    onSelectionChanged.run();
                }
            });
        }
    }

    /**
     * Comprueba si el personaje está seleccionado.
     *
     * @return {@code true} si el checkbox está marcado.
     * @author Marco
     */
    public boolean isSelected() {
        return checkBoxSeleccionar != null && checkBoxSeleccionar.isSelected();
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
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.setSelected(value);
        }
    }

    public void setOnRefreshListener(Runnable recargarListaCompleta) {
        this.onRefresh = recargarListaCompleta;
    }
}