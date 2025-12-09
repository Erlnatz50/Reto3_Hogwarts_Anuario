package es.potersitos.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import java.io.InputStream;

import es.potersitos.util.PersonajeCSVManager;

/**
 * Controlador para la ventana de datos de personajes.
 * Gestiona la interacción con los elementos del FXML.
 *
 * @author Marco
 * @version 1.0
 */
public class ControladorDatos {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorDatos.class);

    /** Bundle del sistema de internacionalización */
    private ResourceBundle resources;

    /** SLUG del personaje actual (necesario para la eliminación) */
    private String personajeSlug;

    /** Imagen del personaje */
    @FXML
    private ImageView imageView;

    /** Contenedor principal de etiquetas de datos */
    @FXML
    private VBox datosVBox;

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
     *
     * @author Marco
     */
    @FXML
    public void initialize() {
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
        logger.info("ControladorDatos inicializado correctamente.");
        cargarDatosPrueba();
    }

    /**
     * Asigna el identificador único (slug) del personaje actual.
     *
     * @param slug Identificador del personaje.
     * @author Marco
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
    }

    /**
     * Simula la carga de datos de un personaje de prueba en la interfaz.
     *
     * @author Marco
     */
    public void cargarDatosPrueba() {
        logger.debug("Cargando datos de prueba...");
        try {
            String rutaImagen = "/es/potersitos/img/persona_predeterminado.png";
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(rutaImagen)));
            imageView.setImage(image);
            logger.info("Imagen cargada con éxito desde: {}", rutaImagen);
        } catch (Exception e) {
            logger.error("Error al cargar la imagen. Verifica la ruta y el nombre del archivo: /es/potersitos/img/foto.png", e);
        }

        nombreLabel.setText(resources.getString("nombre.label") + ": Harry James Potter");
        personajeSlug = "harry-potter";
        aliasLabel.setText(resources.getString("alias.label") + ": El Niño que Vivió");
        animagusLabel.setText(resources.getString("animagus.label") + ": No");
        bloodStatusLabel.setText(resources.getString("bloodStatus.label") + ": Mestizo");
        boggartLabel.setText(resources.getString("boggart.label") + ": Dementor");
        nacidoLabel.setText(resources.getString("nacido.label") + ": 31 de Julio de 1980");
        fallecidoLabel.setText(resources.getString("fallecido.label") + ": N/A");
        colorOjosLabel.setText(resources.getString("colorOjos.label") + ": Verde");
        familiaresLabel.setText(resources.getString("familiares.label") + ": Ginny Weasley (Esposa)");
        generoLabel.setText(resources.getString("genero.label") + ": Masculino");
        colorPeloLabel.setText(resources.getString("colorPelo.label") + ": Negro");
        alturaLabel.setText(resources.getString("altura.label") + ": 1.75m");
        casaLabel.setText(resources.getString("casa.label") + ": Gryffindor");
        imagenLabel.setText(resources.getString("imagen.label") + ": (Ruta a la imagen de Harry)");
        trabajosLabel.setText(resources.getString("trabajos.label") + ": Jefe de Aurores");
        estadoCivilLabel.setText(resources.getString("estadoCivil.label") + ": Casado");
        nacionalidadLabel.setText(resources.getString("nacionalidad.label") + ": Británica");
        patronusLabel.setText(resources.getString("patronus.label") + ": Ciervo");
        romancesLabel.setText(resources.getString("romances.label") + ": Ginny Weasley, Cho Chang");
        colorPielLabel.setText(resources.getString("colorPiel.label") + ": Clara");
        especieLabel.setText(resources.getString("especie.label") + ": Humano");
        titulosLabel.setText(resources.getString("titulos.label") + ": Maestro de la Muerte");
        varitasLabel.setText(resources.getString("varitas.label") + ": Acebo y pluma de fénix");
        pesoLabel.setText(resources.getString("peso.label") + ": Aproximado");
    }

    /**
     * Cierra la ventana actual de la aplicación.
     *
     * @param event Evento de acción generado por el botón de cierre.
     * @author Marco
     */
    @FXML
    private void cerrarVentana(ActionEvent event) {
        logger.info("Cerrando ventana mediante botón.");
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Maneja la acción del botón de actualización.
     *
     * @author Marco
     */
    @FXML
    public void handleActualizar() {
        logger.info("Botón 'Actualizar' presionado");
        String mensaje = (resources != null && resources.containsKey("actualizar.msg"))
                ? resources.getString("actualizar.msg")
                : "Funcionalidad de actualizar ejecutada.";

        mandarAlertas(Alert.AlertType.INFORMATION, "Actualizar", "", mensaje);
    }

    /**
     * Maneja la acción del botón de exportación de datos del personaje.
     *
     * @author Marco
     */
    @FXML
    public void handleExportar() {
        logger.info("Botón 'Exportar' presionado");
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, "Error", "", "No se encuentra el archivo del reporte.");
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Casa", obtenerValor(casaLabel));
            parameters.put("Genero", obtenerValor(generoLabel));
            parameters.put("Especie", obtenerValor(especieLabel));
            parameters.put("Ojos", obtenerValor(colorOjosLabel));
            parameters.put("Pelo", obtenerValor(colorPeloLabel));
            parameters.put("Piel", obtenerValor(colorPielLabel));
            parameters.put("Patronus", obtenerValor(patronusLabel));

            InputStream imagenStream = getClass().getResourceAsStream("/es/potersitos/img/foto.png");
            parameters.put("Imagen", imagenStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));

            JasperViewer.viewReport(jasperPrint, false);

        } catch (JRException e) {
            logger.error("Error al exportar el reporte Jasper.", e);
            mandarAlertas(Alert.AlertType.ERROR, "Error", "", "Falló la exportación: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error general durante la exportación del informe.", e);
        }
    }

    /**
     * Maneja la acción del botón de eliminación de un personaje.
     *
     * @param event Evento de acción generado por el botón.
     * @author Marco
     */
    @FXML
    public void handleEliminar(ActionEvent event) {
        if (personajeSlug == null || personajeSlug.isEmpty()) {
            mandarAlertas(Alert.AlertType.ERROR, "Error", "", "No se ha cargado el identificador del personaje (slug) para eliminar.");
            return;
        }

        logger.info("Botón 'Eliminar' presionado para SLUG: {}", personajeSlug);

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(resources.getString("eliminar.confirm.titulo"));
        confirmAlert.setHeaderText(resources.getString("eliminar.confirm.header"));
        confirmAlert.setContentText(resources.getString("eliminar.confirm.contenido"));

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);

            if (exito) {
                mandarAlertas(Alert.AlertType.INFORMATION, "Éxito", "", "El personaje ha sido eliminado del registro.");
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                mandarAlertas(Alert.AlertType.ERROR, "Error", "", "No se pudo eliminar el personaje del archivo CSV. Verifique logs.");
            }
        } else {
            logger.info("Eliminación cancelada por el usuario.");
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
        alerta.showAndWait();
        logger.debug("Alerta mostrada: tipo={}, mensaje={}", tipo, mensaje);
    }

    /**
     * Obtiene el valor textual de una etiqueta con formato "Clave: Valor".
     *
     * @param label Etiqueta desde la que se obtiene el texto.
     * @return Texto posterior al delimitador “: ” o el texto completo si no existe.
     * @author Marco
     */
    private String obtenerValor(Label label) {
        if (label == null) {
            logger.warn("Se intentó obtener el valor de una etiqueta nula.");
            return "";
        }
        String text = label.getText();
        if (text == null || text.trim().isEmpty()) {
            logger.warn("La etiqueta {} no contiene texto.", label.getId());
            return "";
        }

        int separatorIndex = text.indexOf(": ");

        if (separatorIndex != -1) {
            return text.substring(separatorIndex + 2).trim();
        }

        return text.trim();
    }
}