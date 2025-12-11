package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

import net.sf.jasperreports.view.JasperViewer;

/**
 * Controlador para la ventana de datos de personajes.
 * Gestiona la interacción con los elementos del FXML y soporta multiidioma.
 *
 * @author Marco
 * @version 1.1
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
    private Button actualizarButton, exportarButton, eliminarButton, closeButton;

    /**
     * Metodo de inicialización del controlador.
     * Configura los textos de la interfaz según el idioma recibido.
     *
     * @author Marco
     */
    @FXML
    public void initialize() {
        if (this.resources == null) {
            try {
                this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("No se pudo cargar ResourceBundle por defecto.", e);
            }
        }

        logger.info("ControladorDatos inicializado. Idioma: {}",
                resources != null ? resources.getLocale() : "Desconocido");

        configurarTextosBotones();
    }

    /**
     * Configura los textos y tooltips de los botones usando el ResourceBundle.
     *
     * @author Marco
     */
    private void configurarTextosBotones() {
        if (resources == null)
            return;

        try {
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
        } catch (Exception e) {
            logger.warn("Error configurando botones: {}", e.getMessage());
        }
    }

    /**
     * Asigna el identificador único (slug) del personaje actual y dispara la carga
     * de datos.
     *
     * @author Marco
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
        cargarDatosPersonaje(slug);
    }

    /**
     * Carga el personaje completo usando el SLUG de la lista de datos.
     *
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
     * Rellena las etiquetas FXML con los valores del mapa del personaje y maneja la
     * traducción de etiquetas.
     *
     * @author Nizam
     */
    private void rellenarInterfaz(Map<String, String> p) {
        String imagePath = p.getOrDefault("image", "");
        if (!imagePath.isEmpty()) {
            try {
                String rutaFinal = imagePath;
                if (!rutaFinal.toLowerCase().startsWith("http") && !rutaFinal.toLowerCase().startsWith("file:")) {
                    rutaFinal = "file:/" + rutaFinal.replace("\\", "/");
                }
                Image image = new Image(rutaFinal, true);
                imageView.setImage(image);
            } catch (Exception e) {
                logger.error("Error al cargar la imagen desde la ruta: {}", imagePath, e);
            }
        } else {
            try {
                InputStream imgStream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
                if (imgStream != null) {
                    imageView.setImage(new Image(imgStream));
                }
            } catch (Exception ex) {
                logger.error("No se pudo cargar la imagen por defecto.", ex);
            }
        }

        establecerTexto(nombreLabel, "nombre.label", p.getOrDefault("name", "N/A"));
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
        establecerTexto(imagenLabel, "imagen.label", imagePath.isEmpty() ? "N/A" : imagePath);
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

        this.personajeSlug = p.get("slug");
    }

    /**
     * Metodo auxiliar para establecer texto en Labels de forma segura y traducida.
     * Formato: "Traducción: Valor" (Ej.: "Izena: Harry Potter")
     *
     * @author Marco
     */
    private void establecerTexto(Label label, String key, String valor) {
        if (label != null) {
            label.setText(getStringSafe(key) + ": " + valor);
        }
    }

    /**
     * Helper para obtener strings del resource bundle evitando excepciones.
     *
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
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Maneja la acción del botón de actualización.
     *
     * @author Nizam
     */
    @FXML
    public void handleActualizar() {
        mandarAlertas(Alert.AlertType.INFORMATION, getStringSafe("actualizar.button"), "",
                getStringSafe("actualizar.msg"));
    }

    /**
     * Maneja la acción del botón de exportación.
     *
     * @author Nizam
     */
    @FXML
    public void handleExportar() {
        logger.info("Botón 'Exportar' presionado");

        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", "No se encuentra el archivo .jrxml");
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

            // Cargar la imagen del personaje
            InputStream imagenStream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
            if (imagenStream != null) {
                parameters.put("Imagen", imagenStream);
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
            JasperViewer.viewReport(jasperPrint, false);

            logger.info("Reporte PDF generado exitosamente");

        } catch (NoClassDefFoundError e) {
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "",
                    "Falta la librería JasperReports. Comente la funcionalidad si no la usa.");
        } catch (Exception e) {
            logger.error("Error Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", e.getMessage());
        }
    }

    /**
     * Maneja la acción del botón de eliminación de un personaje.
     *
     * @author Marco
     */
    @FXML
    public void handleEliminar(ActionEvent event) {
        if (personajeSlug == null || personajeSlug.isEmpty()) {
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", "Slug vacío.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(getStringSafe("eliminar.confirm.titulo"));
        confirmAlert.setHeaderText(getStringSafe("eliminar.confirm.header"));
        confirmAlert.setContentText(getStringSafe("eliminar.confirm.contenido"));

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            boolean exito = false;
            try {
                exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);
                logger.warn("Simulando eliminación de: {}", personajeSlug);
                exito = true;
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
     * Obtiene el valor textual de una etiqueta.
     *
     * @author Marco
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