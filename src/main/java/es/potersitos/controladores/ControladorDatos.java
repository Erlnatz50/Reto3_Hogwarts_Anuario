package es.potersitos.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

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
 */
public class ControladorDatos implements Initializable {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorDatos.class);

    /** Bundle del sistema de internacionalización */
    private ResourceBundle bundle;

    /** SLUG del personaje actual (necesario para la eliminación) */
    private String personajeSlug;

    // =======================================================
    // Elementos FXML (Deben coincidir con fx:id del FXML)
    // =======================================================

    @FXML private ImageView imageView;
    @FXML private VBox datosVBox;
    @FXML private Label nombreLabel;
    @FXML private Label aliasLabel;
    @FXML private Label animagusLabel;
    @FXML private Label bloodStatusLabel;
    @FXML private Label boggartLabel;
    @FXML private Label nacidoLabel;
    @FXML private Label fallecidoLabel;
    @FXML private Label colorOjosLabel;
    @FXML private Label familiaresLabel;
    @FXML private Label generoLabel;
    @FXML private Label colorPeloLabel;
    @FXML private Label alturaLabel;
    @FXML private Label casaLabel;
    @FXML private Label imagenLabel;
    @FXML private Label trabajosLabel;
    @FXML private Label estadoCivilLabel;
    @FXML private Label nacionalidadLabel;
    @FXML private Label patronusLabel;
    @FXML private Label romancesLabel;
    @FXML private Label colorPielLabel;
    @FXML private Label especieLabel;
    @FXML private Label titulosLabel;
    @FXML private Label varitasLabel;
    @FXML private Label pesoLabel;
    @FXML private Button actualizarButton;
    @FXML private Button exportarButton;
    @FXML private Button eliminarButton;
    @FXML private Button closeButton;

    /**
     * Método de inicialización.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources;
        logger.info("ControladorVentana inicializado correctamente.");

        // Simulación: Cargar datos de prueba al iniciar la ventana
        cargarDatosPrueba();
    }

    // [MANTENIDO] Setter para recibir el SLUG
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
    }

    // =======================================================
    // Métodos de Lógica
    // =======================================================

    /**
     * Simula la carga de datos de un personaje de prueba en la interfaz.
     */
    public void cargarDatosPrueba() {
        logger.debug("Cargando datos de prueba...");
        try {
            String rutaImagen = "/es/potersitos/img/foto.png";
            Image image = new Image(getClass().getResourceAsStream(rutaImagen));
            imageView.setImage(image);
            logger.info("Imagen cargada con éxito desde: " + rutaImagen);
        } catch (Exception e) {
            logger.error(
                    "Error al cargar la imagen. Verifica la ruta y el nombre del archivo: /es/potersitos/img/foto.png",
                    e);
        }

        nombreLabel.setText(bundle.getString("nombre.label") + ": Harry James Potter");
        this.personajeSlug = "harry-potter";
        aliasLabel.setText(bundle.getString("alias.label") + ": El Niño que Vivió");
        animagusLabel.setText(bundle.getString("animagus.label") + ": No");
        bloodStatusLabel.setText(bundle.getString("bloodStatus.label") + ": Mestizo");
        boggartLabel.setText(bundle.getString("boggart.label") + ": Dementor");
        nacidoLabel.setText(bundle.getString("nacido.label") + ": 31 de Julio de 1980");
        fallecidoLabel.setText(bundle.getString("fallecido.label") + ": N/A");
        colorOjosLabel.setText(bundle.getString("colorOjos.label") + ": Verde");
        familiaresLabel.setText(bundle.getString("familiares.label") + ": Ginny Weasley (Esposa)");
        generoLabel.setText(bundle.getString("genero.label") + ": Masculino");
        colorPeloLabel.setText(bundle.getString("colorPelo.label") + ": Negro");
        alturaLabel.setText(bundle.getString("altura.label") + ": 1.75m");
        casaLabel.setText(bundle.getString("casa.label") + ": Gryffindor");
        imagenLabel.setText(bundle.getString("imagen.label") + ": (Ruta a la imagen de Harry)");
        trabajosLabel.setText(bundle.getString("trabajos.label") + ": Jefe de Aurores");
        estadoCivilLabel.setText(bundle.getString("estadoCivil.label") + ": Casado");
        nacionalidadLabel.setText(bundle.getString("nacionalidad.label") + ": Británica");
        patronusLabel.setText(bundle.getString("patronus.label") + ": Ciervo");
        romancesLabel.setText(bundle.getString("romances.label") + ": Ginny Weasley, Cho Chang");
        colorPielLabel.setText(bundle.getString("colorPiel.label") + ": Clara");
        especieLabel.setText(bundle.getString("especie.label") + ": Humano");
        titulosLabel.setText(bundle.getString("titulos.label") + ": Maestro de la Muerte");
        varitasLabel.setText(bundle.getString("varitas.label") + ": Acebo y pluma de fénix");
        pesoLabel.setText(bundle.getString("peso.label") + ": Aproximado");
    }

    // =======================================================
    // Métodos de Acción
    // =======================================================

    @FXML
    private void cerrarVentana(ActionEvent event) {
        logger.info("Botón de cierre ('X') presionado. Cerrando ventana.");
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleActualizar(ActionEvent event) {
        logger.info("Botón Actualizar presionado");
        String mensaje = (bundle != null && bundle.containsKey("actualizar.msg"))
                ? bundle.getString("actualizar.msg")
                : "Funcionalidad de actualizar ejecutada.";

        mostrarAlerta("Actualizar", mensaje);
    }

    @FXML
    public void handleExportar(ActionEvent event) {
        logger.info("Botón Exportar presionado");
        try {
            // 1. Cargar el reporte
            InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml");
            if (reportStream == null) {
                mostrarAlerta("Error", "No se encuentra el archivo del reporte.");
                return;
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 2. Preparar parámetros
            Map<String, Object> parameters = new HashMap<>();
            // Las llamadas a obtenerValor() que daban error
            parameters.put("Casa", obtenerValor(casaLabel));
            parameters.put("Genero", obtenerValor(generoLabel));
            parameters.put("Especie", obtenerValor(especieLabel));
            parameters.put("Ojos", obtenerValor(colorOjosLabel));
            parameters.put("Pelo", obtenerValor(colorPeloLabel));
            parameters.put("Piel", obtenerValor(colorPielLabel));
            parameters.put("Patronus", obtenerValor(patronusLabel));

            // Imagen
            InputStream imagenStream = getClass().getResourceAsStream("/es/potersitos/img/foto.png");
            parameters.put("Imagen", imagenStream);

            // 3. Llenar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // 4. Mostrar visor
            JasperViewer.viewReport(jasperPrint, false);

        } catch (JRException e) {
            logger.error("Error al exportar el reporte", e);
            mostrarAlerta("Error", "Falló la exportación: " + e.getMessage());
        }
    }

    @FXML
    public void handleEliminar(ActionEvent event) {
        if (personajeSlug == null || personajeSlug.isEmpty()) {
            mostrarAlerta("Error", "No se ha cargado el identificador del personaje (slug) para eliminar.");
            return;
        }

        logger.info("Botón Eliminar presionado para SLUG: {}", personajeSlug);

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(bundle.getString("eliminar.confirm.titulo"));
        confirmAlert.setHeaderText(bundle.getString("eliminar.confirm.header"));
        confirmAlert.setContentText(bundle.getString("eliminar.confirm.contenido"));

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);

            if (exito) {
                mostrarAlerta("Éxito", "El personaje ha sido eliminado del registro.");
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar el personaje del archivo CSV. Verifique logs.");
            }
        } else {
            logger.info("Eliminación cancelada por el usuario.");
        }
    }

    /**
     * Método auxiliar para mostrar alertas simples.
     */
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    /**
     * [MEJORADO] Extrae el valor de la etiqueta asumiendo el formato "Clave: Valor".
     */
    private String obtenerValor(Label label) {
        // Validación robusta: verifica si la etiqueta o su texto son nulos.
        if (label == null) {
            logger.warn("Se intentó obtener el valor de una etiqueta nula.");
            return "";
        }
        String text = label.getText();
        if (text == null || text.trim().isEmpty()) {
            logger.warn("La etiqueta {} no contiene texto.", label.getId());
            return "";
        }

        // Verifica si el texto contiene el delimitador esperado
        int separatorIndex = text.indexOf(": ");

        if (separatorIndex != -1) {
            // Devuelve la parte después de ": "
            // Usamos substring para mayor eficiencia que split en este caso
            return text.substring(separatorIndex + 2).trim();
        }

        // Si no hay delimitador, devuelve el texto completo (o vacío si solo hay espacios)
        return text.trim();
    }
}