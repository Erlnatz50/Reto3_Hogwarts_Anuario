package es.potersitos.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// SI NO TIENES JASPER REPORTS, BORRA O COMENTA ESTOS IMPORTS
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
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

    /** * Bundle del sistema de internacionalización.
     * LA ETIQUETA @FXML ES NECESARIA PARA RECIBIR EL IDIOMA DEL CONTROLADOR ANTERIOR.
     */
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
     */
    @FXML
    public void initialize() {
        // Si resources es null aquí, es que no se puso el @FXML o hubo un error en el loader.
        // Intentamos cargar uno por defecto para que no crashee.
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
        cargarDatosPrueba();
    }

    /**
     * Configura los textos y tooltips de los botones usando el ResourceBundle.
     */
    private void configurarTextosBotones() {
        if (resources == null) return;

        try {
            // Textos de botones (traducidos)
            if(actualizarButton != null) {
                actualizarButton.setText(getStringSafe("actualizar.button"));
                actualizarButton.setTooltip(new Tooltip(getStringSafe("actualizar.tooltip")));
            }
            if(exportarButton != null) {
                exportarButton.setText(getStringSafe("exportar.button"));
                exportarButton.setTooltip(new Tooltip(getStringSafe("exportar.tooltip")));
            }
            if(eliminarButton != null) {
                eliminarButton.setText(getStringSafe("eliminar.button"));
                eliminarButton.setTooltip(new Tooltip(getStringSafe("eliminar.tooltip")));
            }
        } catch (Exception e) {
            logger.warn("Error configurando botones: {}", e.getMessage());
        }
    }

    /**
     * Asigna el identificador único (slug) del personaje actual.
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
    }

    /**
     * Simula la carga de datos de un personaje de prueba.
     * Usa 'establecerTexto' para traducir las etiquetas ("Nombre:", "Izena:", etc.)
     */
    public void cargarDatosPrueba() {
        try {
            String rutaImagen = "/es/potersitos/img/persona_predeterminado.png";
            InputStream imgStream = getClass().getResourceAsStream(rutaImagen);
            if (imgStream != null) {
                imageView.setImage(new Image(imgStream));
            }
        } catch (Exception e) {
            logger.error("Error cargando imagen", e);
        }

        // Aquí usamos las claves para que se traduzca el prefijo (ej. "Nombre: " vs "Izena: ")
        establecerTexto(nombreLabel, "nombre.label", "Harry James Potter");
        personajeSlug = "harry-potter";
        establecerTexto(aliasLabel, "alias.label", "El Niño que Vivió");
        establecerTexto(animagusLabel, "animagus.label", "No");
        establecerTexto(bloodStatusLabel, "bloodStatus.label", "Mestizo");
        establecerTexto(boggartLabel, "boggart.label", "Dementor");
        establecerTexto(nacidoLabel, "nacido.label", "31 de Julio de 1980");
        establecerTexto(fallecidoLabel, "fallecido.label", "N/A");
        establecerTexto(colorOjosLabel, "colorOjos.label", "Verde");
        establecerTexto(familiaresLabel, "familiares.label", "Ginny Weasley (Esposa)");
        establecerTexto(generoLabel, "genero.label", "Masculino");
        establecerTexto(colorPeloLabel, "colorPelo.label", "Negro");
        establecerTexto(alturaLabel, "altura.label", "1.75m");
        establecerTexto(casaLabel, "casa.label", "Gryffindor");
        establecerTexto(imagenLabel, "imagen.label", "N/A");
        establecerTexto(trabajosLabel, "trabajos.label", "Jefe de Aurores");
        establecerTexto(estadoCivilLabel, "estadoCivil.label", "Casado");
        establecerTexto(nacionalidadLabel, "nacionalidad.label", "Británica");
        establecerTexto(patronusLabel, "patronus.label", "Ciervo");
        establecerTexto(romancesLabel, "romances.label", "Ginny Weasley");
        establecerTexto(colorPielLabel, "colorPiel.label", "Clara");
        establecerTexto(especieLabel, "especie.label", "Humano");
        establecerTexto(titulosLabel, "titulos.label", "Maestro de la Muerte");
        establecerTexto(varitasLabel, "varitas.label", "Acebo y pluma de fénix");
        establecerTexto(pesoLabel, "peso.label", "70kg");
    }

    /**
     * Método auxiliar para establecer texto en Labels de forma segura y traducida.
     * Formato: "Traducción: Valor" (Ej: "Izena: Harry Potter")
     */
    private void establecerTexto(Label label, String key, String valor) {
        if (label != null) {
            label.setText(getStringSafe(key) + ": " + valor);
        }
    }

    /**
     * Helper para obtener strings del resource bundle evitando excepciones.
     */
    private String getStringSafe(String key) {
        if (resources == null) return key;
        try {
            return resources.getString(key);
        } catch (Exception e) {
            return key; // Devuelve la clave si falla la traducción
        }
    }

    /**
     * Cierra la ventana actual de la aplicación.
     */
    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Maneja la acción del botón de actualización.
     */
    @FXML
    public void handleActualizar() {
        mandarAlertas(Alert.AlertType.INFORMATION, getStringSafe("actualizar.button"), "", "Datos actualizados (Simulación).");
    }

    /**
     * Maneja la acción del botón de exportación.
     */
    @FXML
    public void handleExportar() {
        logger.info("Botón 'Exportar' presionado");

        // --- INICIO BLOQUE JASPER ---
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", "No se encuentra el archivo .jrxml");
                return;
            }

            // JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            // Map<String, Object> parameters = new HashMap<>();
            // parameters.put("Casa", obtenerValor(casaLabel));

            // InputStream imagenStream = getClass().getResourceAsStream("/es/potersitos/img/foto.png");
            // if(imagenStream != null) {
            //      parameters.put("Imagen", imagenStream);
            // }

            // JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
            // JasperViewer.viewReport(jasperPrint, false);

            mandarAlertas(Alert.AlertType.INFORMATION, "Exportar", "", "Funcionalidad JasperReports comentada para evitar errores de compilación.");

        } catch (NoClassDefFoundError e) {
            mandarAlertas(Alert.AlertType.ERROR, "Error Librería", "", "Falta la librería JasperReports.");
        } catch (Exception e) {
            logger.error("Error Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", e.getMessage());
        }
        // --- FIN BLOQUE JASPER ---
    }

    /**
     * Maneja la acción del botón de eliminación de un personaje.
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
            // AQUÍ LLAMARÍAS A TU CLASE GESTORA DE CSV:
            // boolean exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);

            boolean exito = true; // Simulación de éxito

            if (exito) {
                mandarAlertas(Alert.AlertType.INFORMATION, getStringSafe("exito"), "", "Personaje eliminado.");
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", "Error al eliminar.");
            }
        }
    }

    /**
     * Muestra una alerta JavaFX.
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
     */
    private String obtenerValor(Label label) {
        if (label == null) return "";
        String text = label.getText();
        if (text == null) return "";
        int separatorIndex = text.indexOf(": ");
        return (separatorIndex != -1) ? text.substring(separatorIndex + 2).trim() : text.trim();
    }
}