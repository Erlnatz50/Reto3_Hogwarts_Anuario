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
import java.nio.file.Paths;
import java.util.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    /**
     * Imagen del personaje
     */
    @FXML
    private ImageView imageView;

    /**
     * Etiquetas FXML que muestran los titulos de los datos del personaje
     */
    @FXML
    private Label nombreLabel, aliasLabel, animagusLabel, bloodStatusLabel, boggartLabel, nacidoLabel,
            fallecidoLabel, colorOjosLabel, familiaresLabel, generoLabel, colorPeloLabel, alturaLabel,
            casaLabel, imagenLabel, trabajosLabel, estadoCivilLabel, nacionalidadLabel, patronusLabel,
            romancesLabel, colorPielLabel, especieLabel, titulosLabel, varitasLabel, pesoLabel;

    /**
     * Etiquetas FXML que muestran los datos del personaje
     */
    @FXML
    private Label nombreDatoLabel, aliasDatoLabel, animagusDatoLabel, bloodStatusDatoLabel, boggartDatoLabel,
            nacidoDatoLabel, fallecidoDatoLabel, colorOjosDatoLabel, familiaresDatoLabel, generoDatoLabel,
            colorPeloDatoLabel, alturaDatoLabel, casaDatoLabel, imagenDatoLabel, trabajosDatoLabel,
            estadoCivilDatoLabel, nacionalidadDatoLabel, patronusDatoLabel, romancesDatoLabel,
            colorPielDatoLabel, especieDatoLabel, titulosDatoLabel, varitasDatoLabel, pesoDatoLabel;

    /**
     * Botones principales de acción
     */
    @FXML
    private Button actualizarButton, exportarButton, eliminarButton;

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
            establecerTexto(nombreLabel, "nombre.label", getStringSafe("error.nodatos"));
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
        String imageName = p.getOrDefault("image", "").trim();

        if (!imageName.isEmpty()) {
            File imgFile = Paths.get(RUTA_LOCAL_IMAGENES, imageName).toFile();
            if (imgFile.exists()) {
                try (FileInputStream fis = new FileInputStream(imgFile)) {
                    imageView.setImage(new Image(fis));
                } catch (Exception e) {
                    cargarImagenPorDefecto();
                }
            } else {
                cargarImagenPorDefecto();
            }
        } else {
            cargarImagenPorDefecto();
        }

        nombreDatoLabel.setText(nombre);
        aliasDatoLabel.setText(formatearListaJSON(p.get("alias_names")));
        animagusDatoLabel.setText(p.get("animagus"));
        bloodStatusDatoLabel.setText(p.get("blood_status"));
        boggartDatoLabel.setText(p.get("boggart"));
        nacidoDatoLabel.setText(p.get("born"));
        fallecidoDatoLabel.setText(p.get("died"));
        colorOjosDatoLabel.setText(p.get("eye_color"));
        familiaresDatoLabel.setText(formatearListaJSON(p.get("family_members")));
        generoDatoLabel.setText(p.get("gender"));
        colorPeloDatoLabel.setText(p.get("hair_color"));
        alturaDatoLabel.setText(p.get("height"));
        casaDatoLabel.setText(p.get("house"));
        imagenDatoLabel.setText(imageName);
        trabajosDatoLabel.setText(formatearListaJSON(p.get("jobs")));
        estadoCivilDatoLabel.setText(p.get("marital_status"));
        nacionalidadDatoLabel.setText(p.get("nationality"));
        patronusDatoLabel.setText(p.get("patronus"));
        romancesDatoLabel.setText(formatearListaJSON(p.get("romances")));
        colorPielDatoLabel.setText(p.get("skin_color"));
        especieDatoLabel.setText(p.get("species"));
        titulosDatoLabel.setText(formatearListaJSON(p.get("titles")));
        varitasDatoLabel.setText(p.get("wands"));
        pesoDatoLabel.setText(p.get("weight"));

        personajeSlug = p.get("slug");
    }

    /**
     * Carga una imagen local forzando su lectura desde disco.
     *
     * @param nombreArchivo nombre del archivo de imagen (incluida la extensión)
     * @return {@code true} si la imagen existe y se ha cargado correctamente,
     * {@code false} si no existe o ocurre un error
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
            logger.warn("No se ha podido cargar la imagen {}", nombreArchivo, e);
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
     * Establece texto traducido en una etiqueta.
     *
     * @param label etiqueta JavaFX a modificar
     * @param key   clave del {@link ResourceBundle} para el texto base
     * @param valor valor a mostrar junto a la clave traducida
     * @author Marco
     */
    private void establecerTexto(Label label, String key, String valor) {
        if (label != null) {
            label.setText(getStringSafe(key) + ": " + valor);
        }
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

            parameters.put("Nombre", obtenerValor(nombreDatoLabel));
            parameters.put("Alias", obtenerValor(aliasDatoLabel));
            parameters.put("Casa", obtenerValor(casaDatoLabel));
            parameters.put("Genero", obtenerValor(generoDatoLabel));
            parameters.put("Especie", obtenerValor(especieDatoLabel));
            parameters.put("Ojos", obtenerValor(colorOjosDatoLabel));
            parameters.put("Pelo", obtenerValor(colorPeloDatoLabel));
            parameters.put("Piel", obtenerValor(colorPielDatoLabel));
            parameters.put("Patronus", obtenerValor(patronusDatoLabel));

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
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png")))
        );

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
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png")))
        );

        alerta.showAndWait();
    }

    /**
     * Extrae el valor real de un {@link Label} con formato {@code "Clave: Valor"}.
     *
     * @param datoLabel label etiqueta de la cual se extraerá el valor
     * @return valor textual sin la clave ni el separador, o cadena vacía si el
     * label es nulo
     * @author Telmo
     */
    private String obtenerValor(Label datoLabel) {
        if (datoLabel == null)
            return "";
        String text = datoLabel.getText();
        return text == null ? "" : text.trim();
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
        try {
            if (p == null) {
                return imagenPorDefecto();
            }

            String imageName = p.getOrDefault("image", "").trim();
            if (!imageName.isEmpty()) {
                File imgFile = Paths.get(RUTA_LOCAL_IMAGENES, imageName).toFile();
                if (imgFile.exists()) {
                    return new FileInputStream(imgFile);
                }
            }
        } catch (Exception e) {
            logger.warn("Error cargando imagen para Jasper", e);
        }

        return imagenPorDefecto();
    }

    private InputStream imagenPorDefecto() {
        return getClass().getResourceAsStream(
                "/es/potersitos/img/persona_predeterminado.png"
        );
    }

    private String formatearListaJSON(String jsonLista) {
        if (jsonLista == null || jsonLista.isEmpty()) return "";
        return jsonLista.replaceAll("[\\[\\]\"]", "").trim();
    }
}