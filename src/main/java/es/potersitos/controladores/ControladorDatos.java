package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import javafx.embed.swing.SwingFXUtils;

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

    /** Vista de imagen del personaje */
    @FXML
    private ImageView imageView;

    /** Botones principales de acción */
    @FXML
    private Button actualizarButton, exportarButton, eliminarButton;

    /** Grid donde se muestran los datos del personaje */
    @FXML
    private GridPane datosGrid;

    /** Ruta local donde se buscan imágenes de personajes */
    private static final String RUTA_LOCAL_IMAGENES = System.getProperty("user.home") + File.separator + "Reto3_Hogwarts_Anuario" + File.separator + "imagenes" + File.separator;

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
        logger.info("ControladorDatos inicializado. Idioma: {}", resources != null ? resources.getLocale() : "Desconocido");
    }

    /**
     * Configura los textos y tooltips de los botones usando el ResourceBundle.
     *
     * @author Marco
     */
    private void configurarTextosBotones() {
        if (resources == null) return;

        configurarBoton(actualizarButton, "actualizar.button", "actualizar.tooltip");
        configurarBoton(exportarButton, "exportar.button", "exportar.tooltip");
        configurarBoton(eliminarButton, "eliminar.button", "eliminar.tooltip");
    }

    /**
     * Configura texto y tooltip de un botón específico.
     *
     * @param boton botón a configurar
     * @param claveTexto clave del texto del botón
     * @param claveTooltip clave del tooltip del botón
     * @author Erlantz
     */
    private void configurarBoton(Button boton, String claveTexto, String claveTooltip) {
        if (boton != null) {
            boton.setText(getStringSafe(claveTexto));
            boton.setTooltip(new Tooltip(getStringSafe(claveTooltip)));
        }
    }

    /**
     * Asigna el slug del personaje y carga sus datos.
     *
     * @param slug identificador único del personaje
     * @author Erlantz
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
     * @author Telmo
     */
    private void rellenarInterfaz(Map<String, String> p) {
        personajeActual = p;
        String slug = p.getOrDefault("slug", "").trim();
        String imageName = p.getOrDefault("image", "").trim();

        File imagen = obtenerImagenLocal(imageName, slug);

        if (imagen != null) {
            Image img = null;
            if (imagen.getName().toLowerCase().endsWith(".webp")) {
                try {
                    BufferedImage bi = ImageIO.read(imagen);
                    if (bi != null) {
                        img = SwingFXUtils.toFXImage(bi, null);
                    }
                } catch (Exception e) {
                    logger.warn("Error cargando WebP con ImageIO: {}", imagen.getName());
                }
            }

            if (img == null) {
                img = new Image(imagen.toURI().toString());
            }

            final Image finalImg = img;
            if (finalImg.getProgress() >= 1.0 && !finalImg.isError()) {
                imageView.setImage(finalImg);
                imageView.setVisible(true);
                logger.info("Imagen cargada localmente: {}", imagen.getName());
            } else {
                finalImg.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0 && !finalImg.isError()) {
                        imageView.setImage(finalImg);
                        imageView.setVisible(true);
                        logger.info("Imagen renderizada: {}", imagen.getName());
                    }
                });
                imageView.setImage(finalImg);
            }
        } else {
            imageView.setImage(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png"))));
            logger.warn("Imagen no encontrada para slug {}", slug);
        }

        datosGrid.getChildren().clear();
        int filaActual = 0;
        String[][] campos = {
                {"nombre.label", "name"},
                {"alias.label", "alias_names"}, {"animagus.label", "animagus"},
                {"bloodStatus.label", "blood_status"}, {"boggart.label", "boggart"},
                {"nacido.label", "born"}, {"fallecido.label", "died"},
                {"colorOjos.label", "eye_color"}, {"familiares.label", "family_members"},
                {"genero.label", "gender"}, {"colorPelo.label", "hair_color"},
                {"altura.label", "height"}, {"casa.label", "house"},
                {"imagen.label", "image"}, {"trabajos.label", "jobs"},
                {"estadoCivil.label", "marital_status"}, {"nacionalidad.label", "nationality"},
                {"patronus.label", "patronus"}, {"romances.label", "romances"},
                {"colorPiel.label", "skin_color"}, {"especie.label", "species"},
                {"titulos.label", "titles"}, {"varitas.label", "wands"}, {"peso.label", "weight"}
        };

        for (String[] campo : campos) {
            String valor = p.getOrDefault(campo[1], "");
            if (esListaJSON(campo[1])) {
                valor = formatearListaJSON(valor);
            }
            if (esValorValido(valor)) {
                Label tituloLabel = new Label(getStringSafe(campo[0]));
                tituloLabel.getStyleClass().add("label-titulo");

                Label datoLabel = new Label(valor);
                datoLabel.getStyleClass().add("label-dato");
                datoLabel.setWrapText(false);
                datoLabel.setMinWidth(0);
                datoLabel.setTooltip(new Tooltip(valor));
                GridPane.setHgrow(datoLabel, Priority.ALWAYS);
                datoLabel.setMaxWidth(Double.MAX_VALUE);

                datosGrid.add(tituloLabel, 0, filaActual);
                datosGrid.add(datoLabel, 1, filaActual);
                filaActual++;
            }
        }

        personajeSlug = p.get("slug");
    }

    /**
     * Verifica si un campo contiene una lista JSON que necesita formateo.
     *
     * @param clave nombre del campo a verificar
     * @return {@code true} si es una lista JSON, {@code false} en caso contrario
     * @author Erlantz
     */
    private boolean esListaJSON(String clave) {
        return Arrays.asList("alias_names", "family_members", "jobs", "romances", "titles").contains(clave);
    }

    /**
     * Verifica si un valor es válido para mostrar en la interfaz.
     *
     * @param valor valor a validar
     * @return {@code true} si debe mostrarse, {@code false} si debe omitirse
     * @author Erlantz
     */
    private boolean esValorValido(String valor) {
        return valor != null && !valor.isBlank() &&
                !valor.equalsIgnoreCase("unknown") && !valor.equalsIgnoreCase("[]");
    }

    /**
     * Obtiene un texto del ResourceBundle sin lanzar excepciones.
     *
     * @param key clave del texto a recuperar
     * @return texto traducido si existe, o la propia clave si no se encuentra
     * @author Marco
     */
    private String getStringSafe(String key) {
        if (resources == null) return key;
        try {
            return resources.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Formatea una lista JSON eliminando corchetes y comillas.
     *
     * @param jsonLista string JSON con la lista a formatear
     * @return lista formateada para mostrar o cadena vacía si es nula/vacía
     * @author Erlantz
     */
    private String formatearListaJSON(String jsonLista) {
        if (jsonLista == null || jsonLista.isEmpty()) return "";
        return jsonLista.replaceAll("[\\[\\]\"]", "").trim();
    }

    /**
     * Obtiene un File de imagen local probando múltiples candidatos.
     *
     * @param imageName nombre original de la imagen desde CSV
     * @param slug slug del personaje para generar nombres alternativos
     * @return {@link File} válido si existe y es legible, {@code null} si no se encuentra
     * @author Erlantz
     */
    private File obtenerImagenLocal(String imageName, String slug) {
        String[] candidatos = { imageName, slug + ".png", slug + ".jpg", slug + ".jpeg", slug + ".webp" };
        for (String nombre : candidatos) {
            if (nombre == null || nombre.isBlank()) continue;
            File f = Paths.get(RUTA_LOCAL_IMAGENES, nombre).toFile();
            if (f.exists()) {
                try {
                    BufferedImage test = ImageIO.read(f);
                    if (test != null) return f;
                } catch (Exception e) {
                    logger.warn("Imagen corrupta o ilegible: {}", f.getName());
                }
            }
        }
        return null;
    }

    /**
     * Cierra la ventana actual de la aplicación.
     *
     * @param event evento de acción del botón
     * @author Erlantz
     */
    @FXML
    private void cerrarVentana(ActionEvent event) {
        ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
    }

    /**
     * Abre la ventana de edición del personaje actual.
     *
     * @author Telmo
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
                mostrarAlerta(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("no.datos.editar"));
                return;
            }

            ResourceBundle bundle = resources != null ? resources
                    : ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml"), bundle);
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

            String slugGuardado = controller.getSlugActualizado();
            if (slugGuardado != null) {
                personajeSlug = slugGuardado;
            }
            cargarDatosPersonaje(personajeSlug);

        } catch (Exception e) {
            logger.error("Error al abrir ventana de edición", e);
            mostrarAlerta(Alert.AlertType.ERROR, getStringSafe("error"),
                    getStringSafe("fallo.abrir.editor"), e.getMessage());
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
                mostrarAlerta(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("no.encuentra.jrxml"));
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            Map<String, Object> parameters = new HashMap<>();

            String[][] paramsReporte = {
                    {"Nombre", "name"}, {"Alias", "alias_names"}, {"Casa", "house"},
                    {"Genero", "gender"}, {"Especie", "species"}, {"Ojos", "eye_color"},
                    {"Pelo", "hair_color"}, {"Piel", "skin_color"}, {"Patronus", "patronus"}
            };

            for (String[] param : paramsReporte) {
                String valor = "alias_names".equals(param[1]) ?
                        formatearListaJSON(personajeActual.getOrDefault(param[1], "")) :
                        personajeActual.getOrDefault(param[1], "");
                parameters.put(param[0], valor);
            }
            parameters.put("Imagen", obtenerStreamImagen(personajeActual));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
            JasperViewer.viewReport(jasperPrint, false);
            logger.info("Reporte PDF generado exitosamente");

        } catch (Exception e) {
            logger.error("Error al exportar el jasper", e);
            mostrarAlerta(Alert.AlertType.ERROR, getStringSafe("error"), "", e.getMessage());
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
        configurarIconoAlerta(confirmAlert);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);
                if (exito) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, getStringSafe("exito"), "",
                            getStringSafe("eliminar.exito"));
                    ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, getStringSafe("error"), "",
                            getStringSafe("eliminar.error"));
                }
            } catch (Exception e) {
                logger.error("Error real al eliminar el personaje.", e);
                mostrarAlerta(Alert.AlertType.ERROR, getStringSafe("error"), "", "Error inesperado");
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
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensajeTitulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(mensajeTitulo);
        alerta.setContentText(mensaje);
        configurarIconoAlerta(alerta);
        alerta.showAndWait();
    }

    /**
     * Configura el icono de la aplicación en una alerta.
     *
     * @param alerta alerta a la que se le configurará el icono
     * @author Erlantz
     */
    private void configurarIconoAlerta(Alert alerta) {
        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));
    }

    /**
     * Obtiene un {@link InputStream} de la imagen del personaje probando varias fuentes.
     * Prioriza archivos locales, luego imagen por defecto.
     *
     * @param p Mapa con los datos del personaje.
     * @return {@link InputStream} de la imagen.
     * @author Erlantz
     */
    private InputStream obtenerStreamImagen(Map<String, String> p) {
        if (p == null) {
            return getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
        }

        String slug = p.getOrDefault("slug", "").trim();
        String imageName = p.getOrDefault("image", "").trim();
        File img = obtenerImagenLocal(imageName, slug);

        try {
            if (img != null) {
                return new FileInputStream(img);
            }
        } catch (Exception e) {
            logger.warn("Error abriendo imagen para Jasper", e);
        }

        return getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
    }
}