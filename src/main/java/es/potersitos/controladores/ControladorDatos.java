package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

/**
 * Controlador para la ventana de datos de personajes.
 * Gestiona la interacción con los elementos del FXML y soporta multiidioma.
 *
 * @author Marco
 * @version 1.0
 */
public class ControladorDatos {

    /**
     * Logger para esta clase
     */
    private static final Logger logger = LoggerFactory.getLogger(ControladorDatos.class);

    /**
     * Bundle del sistema de internacionalización.
     */
    @FXML
    private ResourceBundle resources;

    /**
     * SLUG del personaje actual (necesario para la eliminación)
     */
    private String personajeSlug;

    /**
     * Datos del personaje actual
     */
    private Map<String, String> personajeActual;

    @FXML
    private ImageView imageView;

    /**
     * Botones principales de acción
     */
    @FXML
    private Button actualizarButton, exportarButton, eliminarButton, closeButton;

    @FXML
    private GridPane datosGrid;

    /**
     * Fila actual en la que se añadirán los datos al grid dynamicamente.
     */
    private int filaActual = 0;

    /**
     * Ruta local donde se buscan imágenes de personajes
     */
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
            Label errorLabel = new Label(getStringSafe("error.nodatos"));
            datosGrid.getChildren().clear();
            datosGrid.getChildren().add(errorLabel);
        }
    }

    /**
     * Rellena las etiquetas FXML con los valores del mapa del personaje.
     *
     * @param p mapa con los datos del personaje
     * @author Nizam
     */
    private void rellenarInterfaz(Map<String, String> p) {
        personajeActual = p;
        String nombre = p.getOrDefault("name", "");
        String slug = p.getOrDefault("slug", "").trim();
        String imageName = p.getOrDefault("image", "").trim();

        // 1. Intentar cargar desde URL
        boolean imagenCargada = false;
        if (imageName.startsWith("http")) {
            try {
                imageView.setImage(new Image(imageName, true));
                imagenCargada = true;
                logger.info("Imagen cargada desde URL: {}", imageName);
            } catch (Exception e) {
                logger.warn("Error cargando imagen desde URL: {}", imageName);
            }
        }

        // 2. Intentar como ruta absoluta
        if (!imagenCargada && !imageName.isEmpty()) {
            File imgFileAbs = new File(imageName);
            if (imgFileAbs.isAbsolute() && imgFileAbs.exists()) {
                try {
                    imageView.setImage(new Image(imgFileAbs.toURI().toString()));
                    imagenCargada = true;
                    logger.info("Imagen cargada desde ruta absoluta: {}", imageName);
                } catch (Exception e) {
                    logger.warn("Error cargando imagen desde ruta absoluta: {}", imageName);
                }
            }
        }

        // 3. Intentar en carpeta local (campo image o slug)
        if (!imagenCargada) {
            String[] candidatos = { imageName, slug + ".jpg", slug + ".png", slug + ".jpeg", slug + ".JPG",
                    slug + ".PNG", slug + ".JPEG" };
            for (String cand : candidatos) {
                if (cand == null || cand.isBlank())
                    continue;
                File imgFile = Paths.get(RUTA_LOCAL_IMAGENES, cand).toFile();
                if (imgFile.exists()) {
                    try {
                        imageView.setImage(new Image(imgFile.toURI().toString()));
                        imagenCargada = true;
                        logger.info("Imagen cargada localmente: {}", cand);
                        break;
                    } catch (Exception e) {
                        logger.warn("Error cargando imagen local: {}", cand);
                    }
                }
            }
        }

        // 4. Imagen por defecto
        if (!imagenCargada) {
            cargarImagenPorDefecto();
        }

        // Limpiar contenedor dinámico
        datosGrid.getChildren().clear();
        filaActual = 0;

        // Rellenar campos solo si tienen valor
        actualizarCampo("nombre.label", nombre);
        actualizarCampo("alias.label", formatearListaJSON(p.get("alias_names")));
        actualizarCampo("animagus.label", p.get("animagus"));
        actualizarCampo("bloodStatus.label", p.get("blood_status"));
        actualizarCampo("boggart.label", p.get("boggart"));
        actualizarCampo("nacido.label", p.get("born"));
        actualizarCampo("fallecido.label", p.get("died"));
        actualizarCampo("colorOjos.label", p.get("eye_color"));
        actualizarCampo("familiares.label", formatearListaJSON(p.get("family_members")));
        actualizarCampo("genero.label", p.get("gender"));
        actualizarCampo("colorPelo.label", p.get("hair_color"));
        actualizarCampo("altura.label", p.get("height"));
        actualizarCampo("casa.label", p.get("house"));
        actualizarCampo("imagen.label", imageName);
        actualizarCampo("trabajos.label", formatearListaJSON(p.get("jobs")));
        actualizarCampo("estadoCivil.label", p.get("marital_status"));
        actualizarCampo("nacionalidad.label", p.get("nationality"));
        actualizarCampo("patronus.label", p.get("patronus"));
        actualizarCampo("romances.label", formatearListaJSON(p.get("romances")));
        actualizarCampo("colorPiel.label", p.get("skin_color"));
        actualizarCampo("especie.label", p.get("species"));
        actualizarCampo("titulos.label", formatearListaJSON(p.get("titles")));
        actualizarCampo("varitas.label", p.get("wands"));
        actualizarCampo("peso.label", p.get("weight"));

        personajeSlug = p.get("slug");
    }

    /**
     * Añade una fila de información al VBox si el valor es válido.
     */
    private void actualizarCampo(String keyClave, String valor) {
        if (valor == null || valor.isBlank() || valor.equalsIgnoreCase("unknown") || valor.equalsIgnoreCase("[]")) {
            return;
        }

        Label tituloLabel = new Label(getStringSafe(keyClave));
        tituloLabel.getStyleClass().add("label-titulo");

        Label datoLabel = new Label(valor);
        datoLabel.getStyleClass().add("label-dato");

        // Truncamiento y Tooltip para textos largos
        datoLabel.setWrapText(false);
        datoLabel.setMinWidth(0);
        datoLabel.setTooltip(new Tooltip(valor));
        GridPane.setHgrow(datoLabel, Priority.ALWAYS);
        datoLabel.setMaxWidth(Double.MAX_VALUE);

        // Añadir al grid en las columnas 0 (título) y 1 (dato)
        datosGrid.add(tituloLabel, 0, filaActual);
        datosGrid.add(datoLabel, 1, filaActual);

        filaActual++;
    }

    /**
     * Carga la imagen por defecto del proyecto.
     *
     * @author Nizam
     */
    private void cargarImagenPorDefecto() {
        imageView.setImage(new Image(getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png")));
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
        if (personajeSlug == null || personajeSlug.isBlank()) {
            logger.warn("handleActualizar llamado sin personajeSlug válido");
            return;
        }

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
                logger.warn("Error al aplicar CSS", e);
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.showAndWait();

            // Refrescar con el slug (por si cambió durante la edición)
            String slugGuardado = controller.getSlugActualizado();
            if (slugGuardado != null) {
                personajeSlug = slugGuardado;
            }
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
            parameters.put("Alias", formatearListaJSON(personajeActual.getOrDefault("alias_names", "")));
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
        if (personajeSlug == null || personajeSlug.isBlank()) {
            logger.warn("handleEliminar llamado sin personajeSlug válido");
            return;
        }

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
        if (p == null) {
            return getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
        }

        String slug = p.getOrDefault("slug", "").trim();
        String imageName = p.getOrDefault("image", "").trim();

        try {
            // 1. Intentar como URL
            if (imageName.startsWith("http")) {
                try {
                    return new java.net.URL(imageName).openStream();
                } catch (Exception e) {
                    logger.warn("No se pudo abrir stream desde URL: {}", imageName);
                }
            }

            // 2. Intentar como ruta absoluta
            if (!imageName.isEmpty()) {
                File fileAbs = new File(imageName);
                if (fileAbs.isAbsolute() && fileAbs.exists()) {
                    return new FileInputStream(fileAbs);
                }
            }

            // 3. Intentar en carpeta local (campo image o slug)
            String[] candidatos = { imageName, slug + ".jpg", slug + ".png", slug + ".jpeg", slug + ".JPG",
                    slug + ".PNG", slug + ".JPEG" };
            for (String cand : candidatos) {
                if (cand == null || cand.isBlank())
                    continue;
                File imgFile = Paths.get(RUTA_LOCAL_IMAGENES, cand).toFile();
                if (imgFile.exists()) {
                    return new FileInputStream(imgFile);
                }
            }

            logger.warn("No se encontró imagen para: {}", p.getOrDefault("name", "N/A"));
        } catch (Exception e) {
            logger.warn("Error obteniendo stream de imagen para Jasper", e);
        }

        return getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
    }

    private String formatearListaJSON(String jsonLista) {
        if (jsonLista == null || jsonLista.isEmpty())
            return "";
        return jsonLista.replaceAll("[\\[\\]\"]", "").trim();
    }
}