package es.potersitos.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la ventana de datos de personajes.
 * Gestiona la interacción con los elementos del FXML.
 */
public class ControladorVentana implements Initializable {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorVentana.class);

    /** Bundle del sistema de internacionalización */
    private ResourceBundle bundle;

    // =======================================================
    // Elementos FXML (Deben coincidir con fx:id del FXML)
    // =======================================================
    @FXML
    private ImageView imageView;

    @FXML
    private VBox datosVBox;

    // Elementos inyectados para mostrar datos
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

    @FXML
    private Button actualizarButton;

    @FXML
    private Button exportarButton;

    @FXML
    private Button eliminarButton;

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

    // =======================================================
    // Métodos de Lógica
    // =======================================================

    /**
     * Simula la carga de datos de un personaje de prueba en la interfaz.
     */
    public void cargarDatosPrueba() {
        logger.debug("Cargando datos de prueba...");

        // 1. Cargar imagen del personaje
        try {
            // La ruta es relativa al classpath. Nota que la ruta empieza con '/'
            // y luego sigue la estructura de paquetes/carpetas que creaste.
            String rutaImagen = "/es/potersitos/img/aamir_loonat.jpg";

            Image image = new Image(getClass().getResourceAsStream("/es/potersitos/img/foto.png"));
            imageView.setImage(image);

            logger.info("Imagen cargada con éxito desde: " + rutaImagen);

        } catch (Exception e) {
            // Si la imagen no se encuentra, esto registrará un error útil.
            logger.error("Error al cargar la imagen. Verifica la ruta y el nombre del archivo: /es/potersitos/img/aamir_loonat.jpg", e);
            // Opcional: Cargar una imagen de "No disponible"
            // imageView.setImage(new Image(getClass().getResourceAsStream("/ruta/a/no_disponible.png")));
        }

        // 2. Actualizar los Labels con datos de ejemplo
        // Se concatena la etiqueta traducida (%nombre.label) con el valor real.

        nombreLabel.setText(bundle.getString("nombre.label") + ": Harry James Potter");
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

    /**
     * Maneja el clic en el botón Actualizar.
     */
    @FXML
    public void handleActualizar(ActionEvent event) {
        logger.info("Botón Actualizar presionado");
        String mensaje = (bundle != null && bundle.containsKey("actualizar.msg"))
                ? bundle.getString("actualizar.msg")
                : "Funcionalidad de actualizar ejecutada.";

        mostrarAlerta("Actualizar", mensaje);
    }

    /**
     * Maneja el clic en el botón Exportar.
     */
    @FXML
    public void handleExportar(ActionEvent event) {
        logger.info("Botón Exportar presionado");
        mostrarAlerta("Exportar", "Funcionalidad de exportar ejecutada.");
    }

    /**
     * Maneja el clic en el botón Eliminar.
     */
    @FXML
    public void handleEliminar(ActionEvent event) {
        logger.info("Botón Eliminar presionado");
        mostrarAlerta("Eliminar", "Funcionalidad de eliminar ejecutada.");
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
}