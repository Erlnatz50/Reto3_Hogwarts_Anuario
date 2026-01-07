package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Controlador para la ventana de datos de personajes.
 * Gestiona la interacción con los elementos del FXML y soporta multiidioma.
 *
 * @author Marco
 * @version 1.0
 */
public class ControladorDatos {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorDatos.class);

    /** Bundle del sistema de internacionalización. */
    @FXML
    private ResourceBundle resources;

    /** SLUG del personaje actual (necesario para la eliminación) */
    private String personajeSlug;

    /** Datos del personaje actual */
    private Map<String, String> personajeActual;

    /** Imagen del personaje */
    @FXML
    private ImageView imageView;

    /** Contenedor Grid para los datos */
    @FXML
    private GridPane datosGrid;

    /** Contador de filas para el GridPane */
    private int filaActual = 0;

    /** Botones principales de acción */
    @FXML
    private Button actualizarButton, exportarButton, eliminarButton;

    /** Ruta local donde se buscan imágenes de personajes */
    private static final String RUTA_LOCAL_IMAGENES = System.getProperty("user.home") + File.separator
            + "Reto3_Hogwarts_Anuario" + File.separator + "imagenes" + File.separator;

    /**
     * Metodo de inicialización del controlador.
     * Configura los textos de la interfaz según el idioma recibido.
     *
     * @author Marco
     */
    @FXML
    public void initialize() {
        if (resources == null) {
            try {
                resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("No se pudo cargar ResourceBundle por defecto.", e);
            }
        }
        configurarTextosBotones();
        logger.info("ControladorDatos inicializado. Idioma: {}",
                resources != null ? resources.getLocale() : "Desconocido");
    }

    /**
     * Configura los textos y tooltips de los botones usando el ResourceBundle.
     *
     * @author Marco
     */
    private void configurarTextosBotones() {
        if (resources == null) {
            return;
        }

        if (actualizarButton != null) {
            actualizarButton.setText(getStringSafe("actualizar.button"));
            actualizarButton.setTooltip(new Tooltip(getStringSafe("actualizar.tooltip")));
        }

        if (exportarButton != null) {
            exportarButton.setText(getStringSafe("exportar.button"));
            exportarButton.setTooltip(new Tooltip(getStringSafe("exportar.tooltip")));
        }

        if (eliminarButton != null) {
            eliminarButton.setText(getStringSafe("eliminar.button"));
            eliminarButton.setTooltip(new Tooltip(getStringSafe("eliminar.tooltip")));
        }
    }

    /**
     * Asigna el slug del personaje y carga sus datos.
     *
     * @param slug identificador único del personaje
     * @author Marco
     */
    public void setPersonajeSlug(String slug) {
        personajeSlug = slug;
        cargarDatosPersonaje(slug);
    }

    /**
     * Carga los datos de un personaje desde el CSV a partir de su slug.
     *
     * @param slug identificador único del personaje
     * @author Nizam
     */
    private void cargarDatosPersonaje(String slug) {
        if (slug == null || slug.isEmpty()) {
            logger.error("SLUG nulo o vacío. No se pueden cargar los datos.");
            return;
        }

        List<Map<String, String>> todosPersonajes = PersonajeCSVManager.leerTodosLosPersonajes();

        Optional<Map<String, String>> personajeEncontrado = todosPersonajes.stream()
                .filter(p -> slug.equalsIgnoreCase(p.getOrDefault("slug", "")))
                .findFirst();

        if (personajeEncontrado.isPresent()) {
            rellenarInterfaz(personajeEncontrado.get());
        } else {
            logger.error("Personaje con SLUG '{}' no encontrado.", slug);
            // Mostrar mensaje de error en el grid si no hay datos
            datosGrid.getChildren().clear();
            datosGrid.add(new Label(getStringSafe("error.nodatos")), 0, 0);
        }
    }

    /**
     * Rellena las etiquetas FXML con los valores del mapa del personaje.
     * Solo se muestran los campos que tienen información.
     *
     * @param p mapa con los datos del personaje
     * @author Nizam
     */
    private void rellenarInterfaz(Map<String, String> p) {
        this.personajeActual = p;
        String nombre = p.getOrDefault("name", "");
        String imagePathCSV = p.getOrDefault("image", "");

        boolean imagenCargada = false;

        String nombreFormateado = formatearTexto(nombre);
        if (!nombreFormateado.isEmpty()) {
            imagenCargada = intentarCargarVariasExtensiones(nombreFormateado)
                    || intentarCargarVariasExtensiones(nombre.toLowerCase().replaceAll("\\s+", "-"));
        }

        if (!imagenCargada && !imagePathCSV.isBlank()) {
            String base = limpiaNombreArchivo(imagePathCSV);
            imagenCargada = intentarCargarVariasExtensiones(base);

            if (!imagenCargada && imagePathCSV.startsWith("http")) {
                try {
                    imageView.setImage(new Image(imagePathCSV, true));
                    imagenCargada = true;
                } catch (Exception ignored) {
                }
            }
        }

        if (imagenCargada) {
            // Log logic if needed
        } else {
            cargarImagenPorDefecto();
        }

        // Limpiar el grid antes de rellenar
        datosGrid.getChildren().clear();
        filaActual = 0;

        // Rellenar campos dinámicamente en el grid
        actualizarCampo("nombre.label", nombre);
        actualizarCampo("alias.label", p.get("alias_names"));
        actualizarCampo("animagus.label", p.get("animagus"));
        actualizarCampo("bloodStatus.label", p.get("blood_status"));
        actualizarCampo("boggart.label", p.get("boggart"));
        actualizarCampo("nacido.label", p.get("born"));
        actualizarCampo("fallecido.label", p.get("died"));
        actualizarCampo("colorOjos.label", p.get("eye_color"));
        actualizarCampo("familiares.label", p.get("family_members"));
        actualizarCampo("genero.label", p.get("gender"));
        actualizarCampo("colorPelo.label", p.get("hair_color"));
        actualizarCampo("altura.label", p.get("height"));
        actualizarCampo("casa.label", p.get("house"));
        actualizarCampo("imagen.label", imagePathCSV);
        actualizarCampo("trabajos.label", p.get("jobs"));
        actualizarCampo("estadoCivil.label", p.get("marital_status"));
        actualizarCampo("nacionalidad.label", p.get("nationality"));
        actualizarCampo("patronus.label", p.get("patronus"));
        actualizarCampo("romances.label", p.get("romances"));
        actualizarCampo("colorPiel.label", p.get("skin_color"));
        actualizarCampo("especie.label", p.get("species"));
        actualizarCampo("titulos.label", p.get("titles"));
        actualizarCampo("varitas.label", p.get("wands"));
        actualizarCampo("peso.label", p.get("weight"));

        personajeSlug = p.get("slug");
    }

    /**
     * Crea dinámicamente un par de etiquetas (Título y Dato) y las añade al
     * GridPane
     * si el valor es válido.
     *
     * @param keyClave clave del ResourceBundle para el título
     * @param valor    valor textual del dato
     * @author Nizam
     */
    private void actualizarCampo(String keyClave, String valor) {
        boolean tieneValor = valor != null && !valor.isBlank() && !valor.equalsIgnoreCase("unknown");

        if (tieneValor) {
            // Crear el Label para el título (el bundle ya incluye el :)
            Label tituloLabel = new Label(getStringSafe(keyClave));
            tituloLabel.getStyleClass().add("label-titulo");

            // Crear el Label para el dato
            Label datoLabel = new Label(valor);
            datoLabel.getStyleClass().add("label-dato");
            datoLabel.setWrapText(false);
            datoLabel.setMinWidth(0);
            datoLabel.setTooltip(new Tooltip(valor));

            // Asegurar que el dato crezca en el grid
            GridPane.setHgrow(datoLabel, Priority.ALWAYS);
            datoLabel.setMaxWidth(Double.MAX_VALUE);

            // Añadir al grid
            datosGrid.add(tituloLabel, 0, filaActual);
            datosGrid.add(datoLabel, 1, filaActual);

            filaActual++;
        }
    }

    /**
     * Intenta cargar una imagen probando varias extensiones comunes.
     *
     * @param base nombre base del archivo sin extensión
     * @return {@code true} si la imagen se ha cargado correctamente, {@code false}
     *         en caso contrario
     * @author Nizam
     */
    private boolean intentarCargarVariasExtensiones(String base) {
        return cargarImagenLocal(base + ".png") || cargarImagenLocal(base + ".jpg")
                || cargarImagenLocal(base + ".jpeg");
    }

    /**
     * Carga una imagen local forzando su lectura desde disco.
     *
     * @param nombreArchivo nombre del archivo de imagen (incluida la extensión)
     * @return {@code true} si la imagen existe y se ha cargado correctamente,
     *         {@code false} si no existe o ocurre un error
     * @author Nizam
     */
    private boolean cargarImagenLocal(String nombreArchivo) {
        File archivo = new File(RUTA_LOCAL_IMAGENES + nombreArchivo);
        if (!archivo.exists())
            return false;

        try (FileInputStream fis = new FileInputStream(archivo)) {
            imageView.setImage(new Image(fis));
            return true;
        } catch (Exception e) {
            logger.warn("No se ha podido cargar la imagen {}", nombreArchivo);
            return false;
        }
    }

    /**
     * Carga la imagen por defecto del proyecto.
     *
     * @author Nizam
     */
    private void cargarImagenPorDefecto() {
        try (InputStream imgStream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png")) {
            if (imgStream != null) {
                imageView.setImage(new Image(imgStream));
            }
        } catch (Exception e) {
            logger.error("No se ha podido cargar la imagen por defecto.", e);
        }
    }

    /**
     * Capitaliza palabras de un texto.
     *
     * @param texto texto original a formatear
     * @return texto con cada palabra capitalizada, o cadena vacía si es nulo
     * @author Nizam
     */
    private String formatearTexto(String texto) {
        if (texto == null || texto.isBlank())
            return "";
        StringBuilder sb = new StringBuilder();
        for (String p : texto.trim().split("\\s+")) {
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Limpia una ruta o URL dejando solo el nombre base del archivo.
     *
     * @param ruta ruta completa o URL de una imagen
     * @return nombre base del archivo sin extensión ni parámetros
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
     * Obtiene un texto del ResourceBundle sin lanzar excepciones.
     *
     * @param key clave del texto a recuperar
     * @return texto traducido si existe, o la propia clave si no se encuentra
     * @author Marco
     */
    private String getStringSafe(String key) {
        if (resources == null)
            return key;
        try {
            return resources.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Cierra la ventana actual de la aplicación.
     *
     * @author Marco
     */
    @FXML
    private void cerrarVentana(ActionEvent event) {
        ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
    }

    /**
     * Abre la ventana de edición del personaje actual.
     *
     * @author Nizam
     */
    @FXML
    public void handleActualizar() {
        if (personajeSlug == null || personajeSlug.isBlank())
            return;

        try {
            Optional<Map<String, String>> personaje = PersonajeCSVManager.leerTodosLosPersonajes().stream()
                    .filter(p -> personajeSlug.equalsIgnoreCase(p.getOrDefault("slug", "")))
                    .findFirst();

            if (personaje.isEmpty()) {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("no.datos.editar"));
                return;
            }

            ResourceBundle bundle = resources != null ? resources
                    : ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml"),
                    bundle);
            Parent root = loader.load();

            ControladorNuevoPersonaje controller = loader.getController();
            controller.setDatosPersonaje(personaje.get());

            Stage stage = new Stage();
            stage.setTitle(getStringSafe("menu.archivo.editar"));

            Scene scene = new Scene(root);

            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloNuevo.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                }
            } catch (Exception e) {
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.showAndWait();

            cargarDatosPersonaje(personajeSlug);

        } catch (Exception e) {
            logger.error("Error al abrir ventana de edición", e);
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), getStringSafe("fallo.abrir.editor"),
                    e.getMessage());
        }
    }

    /**
     * Exporta la ficha del personaje mediante JasperReports.
     *
     * @author Telmo
     */
    @FXML
    public void handleExportar() {
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("no.encuentra.jrxml"));
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            Map<String, Object> parameters = new HashMap<>();

            parameters.put("Nombre", personajeActual.getOrDefault("name", ""));
            parameters.put("Alias", personajeActual.getOrDefault("alias_names", ""));
            parameters.put("Casa", personajeActual.getOrDefault("house", ""));
            parameters.put("Genero", personajeActual.getOrDefault("gender", ""));
            parameters.put("Especie", personajeActual.getOrDefault("species", ""));
            parameters.put("Ojos", personajeActual.getOrDefault("eye_color", ""));
            parameters.put("Pelo", personajeActual.getOrDefault("hair_color", ""));
            parameters.put("Piel", personajeActual.getOrDefault("skin_color", ""));
            parameters.put("Patronus", personajeActual.getOrDefault("patronus", ""));

            parameters.put("Imagen", obtenerStreamImagen(personajeActual));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
            JasperViewer.viewReport(jasperPrint, false);

            logger.info("Reporte PDF generado exitosamente");

        } catch (Exception e) {
            logger.error("Error al exportar el jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", e.getMessage());
        }
    }

    /**
     * Elimina el personaje actual tras confirmación del usuario.
     *
     * @param event evento de acción generado por el botón de eliminación
     * @author Marco
     */
    @FXML
    public void handleEliminar(ActionEvent event) {
        if (personajeSlug == null || personajeSlug.isBlank())
            return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(getStringSafe("eliminar.confirm.titulo"));
        confirmAlert.setHeaderText(getStringSafe("eliminar.confirm.header"));
        confirmAlert.setContentText(getStringSafe("eliminar.confirm.contenido"));

        Stage stage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean exito = false;
            try {
                exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);
            } catch (Exception e) {
                logger.error("Error real al eliminar el personaje.", e);
            }

            if (exito) {
                mandarAlertas(Alert.AlertType.INFORMATION, getStringSafe("exito"), "", getStringSafe("eliminar.exito"));
                Stage mainStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                mainStage.close();
            } else {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("eliminar.error"));
            }
        }
    }

    /**
     * Muestra una alerta JavaFX con los datos proporcionados.
     *
     * @param tipo          Tipo de alerta (INFO, WARNING, ERROR...)
     * @param titulo        Título de la alerta
     * @param mensajeTitulo Encabezado del mensaje
     * @param mensaje       Contenido del mensaje
     *
     * @author Erlantz
     */
    private void mandarAlertas(Alert.AlertType tipo, String titulo, String mensajeTitulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(mensajeTitulo);
        alerta.setContentText(mensaje);

        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));

        alerta.showAndWait();
    }

    /**
     * Obtiene un {@link InputStream} de la imagen del personaje probando varias
     * fuentes.
     * Prioriza archivos locales, luego URLs y finalmente la imagen por defecto.
     *
     * @param p Mapa con los datos del personaje.
     * @return {@link InputStream} de la imagen.
     * @author Nizam
     */
    private InputStream obtenerStreamImagen(Map<String, String> p) {
        if (p == null)
            return getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");

        String nombre = p.getOrDefault("name", "");
        String imagePathCSV = p.getOrDefault("image", "");
        String nombreFormateado = formatearTexto(nombre);

        if (!nombreFormateado.isEmpty()) {
            InputStream is = obtenerStreamLocal(formatearTexto(nombreFormateado).replace(" ", "-").toLowerCase());
            if (is != null)
                return is;
        }

        String baseNombre = nombre.toLowerCase().replaceAll("\\s+", "-");
        InputStream isNombre = obtenerStreamLocal(baseNombre);
        if (isNombre != null)
            return isNombre;

        if (!imagePathCSV.isBlank()) {
            String baseCSV = limpiaNombreArchivo(imagePathCSV);
            InputStream isCSV = obtenerStreamLocal(baseCSV);
            if (isCSV != null)
                return isCSV;

            if (imagePathCSV.startsWith("http")) {
                try {
                    return new java.net.URL(imagePathCSV).openStream();
                } catch (Exception ignored) {
                }
            }
        }

        return getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
    }

    /**
     * Intenta abrir un {@link FileInputStream} para un nombre de archivo base en la
     * ruta local.
     */
    private InputStream obtenerStreamLocal(String base) {
        String[] extensions = { ".png", ".jpg", ".jpeg" };
        for (String ext : extensions) {
            File f = new File(RUTA_LOCAL_IMAGENES + base + ext);
            if (f.exists()) {
                try {
                    return new FileInputStream(f);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}