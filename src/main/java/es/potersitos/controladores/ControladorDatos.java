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

    /** Imagen del personaje */
    @FXML
    private ImageView imageView;

    /** Etiquetas FXML que muestran los datos del personaje */
    @FXML
    private Label nombreLabel, aliasLabel, animagusLabel, bloodStatusLabel, boggartLabel, nacidoLabel,
            fallecidoLabel, colorOjosLabel, familiaresLabel, generoLabel, colorPeloLabel, alturaLabel,
            casaLabel, imagenLabel, trabajosLabel, estadoCivilLabel, nacionalidadLabel, patronusLabel,
            romancesLabel, colorPielLabel, especieLabel, titulosLabel, varitasLabel, pesoLabel;

    /** Botones principales de acción */
    @FXML
    private Button actualizarButton, exportarButton, eliminarButton;

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
        String nombre = p.getOrDefault("name", "");
        String imagePathCSV = p.getOrDefault("image", "");

        boolean imagenCargada = false;

        String nombreFormateado = formatearTexto(nombre);
        if (!nombreFormateado.isEmpty()) {
            imagenCargada = intentarCargarVariasExtensiones(nombreFormateado) || intentarCargarVariasExtensiones(nombre.toLowerCase().replaceAll("\\s+", "-"));
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

        if (!imagenCargada) {
            cargarImagenPorDefecto();
        }

        establecerTexto(nombreLabel, "nombre.label",nombre);
        establecerTexto(aliasLabel, "alias.label", p.getOrDefault("alias_names", "N/A"));
        establecerTexto(animagusLabel, "animagus.label", p.getOrDefault("animagus", "N/A"));
        establecerTexto(bloodStatusLabel, "bloodStatus.label", p.getOrDefault("blood_status", "N/A"));
        establecerTexto(boggartLabel, "boggart.label", p.getOrDefault("boggart", "N/A"));
        establecerTexto(nacidoLabel, "nacido.label", p.getOrDefault("born", "N/A"));
        establecerTexto(fallecidoLabel, "fallecido.label", p.getOrDefault("died", "N/A"));
        establecerTexto(colorOjosLabel, "colorOjos.label", p.getOrDefault("eye_color", "N/A"));
        establecerTexto(familiaresLabel, "familiares.label", p.getOrDefault("family_members", "N/A"));
        establecerTexto(generoLabel, "genero.label", p.getOrDefault("gender", "N/A"));
        establecerTexto(colorPeloLabel, "colorPelo.label", p.getOrDefault("hair_color", "N/A"));
        establecerTexto(alturaLabel, "altura.label", p.getOrDefault("height", "N/A"));
        establecerTexto(casaLabel, "casa.label", p.getOrDefault("house", "N/A"));
        establecerTexto(imagenLabel, "imagen.label", imagePathCSV.isEmpty() ? "N/A" : imagePathCSV);
        establecerTexto(trabajosLabel, "trabajos.label", p.getOrDefault("jobs", "N/A"));
        establecerTexto(estadoCivilLabel, "estadoCivil.label", p.getOrDefault("marital_status", "N/A"));
        establecerTexto(nacionalidadLabel, "nacionalidad.label", p.getOrDefault("nationality", "N/A"));
        establecerTexto(patronusLabel, "patronus.label", p.getOrDefault("patronus", "N/A"));
        establecerTexto(romancesLabel, "romances.label", p.getOrDefault("romances", "N/A"));
        establecerTexto(colorPielLabel, "colorPiel.label", p.getOrDefault("skin_color", "N/A"));
        establecerTexto(especieLabel, "especie.label", p.getOrDefault("species", "N/A"));
        establecerTexto(titulosLabel, "titulos.label", p.getOrDefault("titles", "N/A"));
        establecerTexto(varitasLabel, "varitas.label", p.getOrDefault("wands", "N/A"));
        establecerTexto(pesoLabel, "peso.label", p.getOrDefault("weight", "N/A"));

        personajeSlug = p.get("slug");
    }

    /**
     * Intenta cargar una imagen probando varias extensiones comunes.
     *
     * @param base nombre base del archivo sin extensión
     * @return {@code true} si la imagen se ha cargado correctamente, {@code false} en caso contrario
     * @author Nizam
     */
    private boolean intentarCargarVariasExtensiones(String base) {
        return cargarImagenLocal(base + ".png") || cargarImagenLocal(base + ".jpg") || cargarImagenLocal(base + ".jpeg");
    }

    /**
     * Carga una imagen local forzando su lectura desde disco.
     *
     * @param nombreArchivo nombre del archivo de imagen (incluida la extensión)
     * @return {@code true} si la imagen existe y se ha cargado correctamente, {@code false} si no existe o ocurre un error
     * @author Nizam
     */
    private boolean cargarImagenLocal(String nombreArchivo) {
        File archivo = new File(RUTA_LOCAL_IMAGENES + nombreArchivo);
        if (!archivo.exists()) return false;

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
        if (texto == null || texto.isBlank()) return "";
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
        if (nombre.contains("/")) nombre = nombre.substring(nombre.lastIndexOf("/") + 1);
        if (nombre.contains("?")) nombre = nombre.substring(0, nombre.indexOf("?"));
        if (nombre.contains(".")) nombre = nombre.substring(0, nombre.lastIndexOf("."));
        return nombre.replace("%20", " ");
    }

    /**
     * Establece texto traducido en una etiqueta.
     *
     * @param label etiqueta JavaFX a modificar
     * @param key clave del {@link ResourceBundle} para el texto base
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
        if (resources == null) return key;
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
        if (personajeSlug == null || personajeSlug.isBlank()) return;

        try {
            Optional<Map<String, String>> personaje = PersonajeCSVManager.leerTodosLosPersonajes().stream()
                    .filter(p -> personajeSlug.equalsIgnoreCase(p.getOrDefault("slug", "")))
                    .findFirst();

            if (personaje.isEmpty()) {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("no.datos.editar"));
                return;
            }

            ResourceBundle bundle = resources != null ? resources : ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
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
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.showAndWait();

            cargarDatosPersonaje(personajeSlug);

        } catch (Exception e) {
            logger.error("Error al abrir ventana de edición", e);
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), getStringSafe("fallo.abrir.editor"), e.getMessage());
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

            parameters.put("Nombre", obtenerValor(nombreLabel));
            parameters.put("Alias", obtenerValor(aliasLabel));
            parameters.put("Casa", obtenerValor(casaLabel));
            parameters.put("Genero", obtenerValor(generoLabel));
            parameters.put("Especie", obtenerValor(especieLabel));
            parameters.put("Ojos", obtenerValor(colorOjosLabel));
            parameters.put("Pelo", obtenerValor(colorPeloLabel));
            parameters.put("Piel", obtenerValor(colorPielLabel));
            parameters.put("Patronus", obtenerValor(patronusLabel));

            if (imageView.getImage() != null) {
                parameters.put("Imagen", imageView.getImage());
            } else {
                InputStream imagenStream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
                if (imagenStream != null) {
                    parameters.put("Imagen", imagenStream);
                }
            }

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
        if (personajeSlug == null || personajeSlug.isBlank()) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(getStringSafe("eliminar.confirm.titulo"));
        confirmAlert.setHeaderText(getStringSafe("eliminar.confirm.header"));
        confirmAlert.setContentText(getStringSafe("eliminar.confirm.contenido"));

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
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
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
        alerta.showAndWait();
    }

    /**
     * Extrae el valor real de un {@link Label} con formato {@code "Clave: Valor"}.
     *
     * @param label label etiqueta de la cual se extraerá el valor
     * @return valor textual sin la clave ni el separador, o cadena vacía si el label es nulo
     * @author Telmo
     */
    private String obtenerValor(Label label) {
        if (label == null)
            return "";
        String text = label.getText();
        if (text == null)
            return "";
        int separatorIndex = text.indexOf(": ");
        return (separatorIndex != -1) ? text.substring(separatorIndex + 2).trim() : text.trim();
    }
}