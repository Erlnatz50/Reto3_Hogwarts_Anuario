package es.potersitos.controladores;

import es.potersitos.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Controlador encargado de gestionar la vista de personajes.
 * Permite filtrar, cambiar idioma, seleccionar y exportar personajes.
 *
 * @author Telmo
 * @version 1.0
 */
public class ControladorVisualizarPersonajes {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    /** Botones principales del panel */
    @FXML
    public Button btnFiltrar, btnCerrarFiltro, btnSeleccionar, btnExportar;

    /** Barra de menús principal de la aplicación */
    @FXML
    public MenuBar menuBar;

    /** Elementos del menú Archivo **/
    @FXML
    public MenuItem menuSalir, menuNuevo, menuImportar, menuGuardar;

    /** Contenedor de las fichas de personajes */
    @FXML
    private TilePane tilePanePersonajes;

    /** Panel lateral de filtros */
    @FXML
    private VBox filterPanel;

    /** Contenedor de secciones de filtros */
    @FXML
    private Accordion accordionFiltros;

    /** Campo de búsqueda de personajes */
    @FXML
    private TextField searchField;

    /** Lista de controladores de fichas cargadas */
    private List<ControladorFichaPersonaje> listaControladores;

    /** Lista completa de personajes */
    private List<Personaje> listaPersonajes;

    /** Bundle del sistema de internacionalización */
    private ResourceBundle resources;

    /**
     * Clase interna que representa un personaje con datos básicos.
     *
     * @author Telmo
     */
    private static class Personaje {
        String nombre, casa, imagePath, slug;

        public Personaje(String nombre, String casa, String imagePath, String slug) {
            this.nombre = nombre;
            this.casa = casa;
            this.imagePath = imagePath;
            this.slug = slug;
        }
    }

    /**
     * Inicializa el controlador configurando el idioma, datos de ejemplo y filtros.
     *
     * @author Telmo
     */
    @FXML
    public void initialize() {
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        // atajos de teclado
        menuNuevo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        menuImportar.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        menuGuardar.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuSalir.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        this.listaPersonajes = new ArrayList<>();
        this.listaControladores = new ArrayList<>();

        inicializarDatosPrueba();
        cargarPersonajes(listaPersonajes);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filtrarPersonajes());
        configurarListenersFiltros();
    }

    /**
     * Cambia el idioma de la interfaz al Español.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaEspaniol() {
        cambiarIdioma(Locale.of("es"));
    }

    /**
     * Cambia el idioma de la interfaz al Euskera.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaEuskera() {
        cambiarIdioma(Locale.of("eu"));
    }

    /**
     * Cambia el idioma de la interfaz al Inglés.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaIngles() {
        cambiarIdioma(Locale.ENGLISH);
    }

    /**
     * Cambia el idioma actual de la interfaz gráfica.
     *
     * @param nuevoLocale Nueva configuración regional (Locale) que se va a aplicar.
     * @author Erlantz
     */
    private void cambiarIdioma(Locale nuevoLocale) {
        try {
            resources = ResourceBundle.getBundle("es.potersitos.mensaje", nuevoLocale);
            logger.info("Idioma cambiado a: {}", nuevoLocale);

            menuSalir.setText(resources.getString("menu.archivo.salir"));

        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null, "No se pudo cambiar el idioma: " + e.getMessage());
        }
    }

    /**
     * Inicializa una lista de personajes de prueba.
     *
     * @author Telmo
     */
    private void inicializarDatosPrueba() {
        for (int i = 1; i <= 9; i++) {
            String casa = switch (i % 4) {
                case 0 -> "Slytherin";
                case 2 -> "Hufflepuff";
                case 3 -> "Ravenclaw";
                default -> "Gryffindor";
            };

            listaPersonajes.add(new Personaje("Personaje " + i, casa, "path/to/image.png", "personaje-" + i));
        }
        listaPersonajes.add(new Personaje("Harry Potter", "Gryffindor", "path/to/image.png", "harry-potter"));
    }

    /**
     * Carga una lista de personajes en el panel de fichas.
     *
     * @param personajes Lista de personajes a mostrar
     * @author Telmo
     */
    private void cargarPersonajes(List<Personaje> personajes) {
        tilePanePersonajes.getChildren().clear();
        listaControladores.clear();

        for (Personaje p : personajes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();
                controller.setData(p.nombre, p.casa, p.imagePath);
                controller.setPersonajeSlug(p.slug);

                listaControladores.add(controller);
                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
            }
        }
    }

    /**
     * Configura un listener para la propiedad 'selected' de cada CheckBox
     * dentro del Accordion para aplicar el filtro inmediatamente.
     *
     * @author Marco
     */
    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if ("Casa".equals(pane.getText())) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox cb) {
                                cb.selectedProperty().addListener((observable, oldValue, newValue) -> filtrarPersonajes());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Muestra u oculta el panel de filtros.
     *
     * @author Telmo
     */
    @FXML
    private void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        logger.info("Panel de filtros {}", isVisible ? "ocultado" : "mostrado");
    }

    /**
     * Limpia todos los filtros y muestra todos los personajes.
     *
     * @author Telmo
     */
    @FXML
    private void limpiarFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                Node content = pane.getContent();
                if (content instanceof VBox) {
                    for (Node node : ((VBox) content).getChildren()) {
                        if (node instanceof CheckBox) {
                            ((CheckBox) node).setSelected(false);
                        }
                    }
                }
            }
        }
        cargarPersonajes(listaPersonajes);
    }

    /**
     * Aplica los filtros activos a la lista de personajes.
     *
     * @author Telmo
     */
    @FXML
    private void aplicarFiltros() {
        filtrarPersonajes();
    }

    /**
     * Filtra la lista de personajes según el texto de búsqueda y filtros activos.
     *
     * @author Telmo
     */
    private void filtrarPersonajes() {
        String searchText = searchField.getText().toLowerCase();
        List<String> selectedCasas = new ArrayList<>();

        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if ("Casa".equals(pane.getText())) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox cb) {
                                if (cb.isSelected()) {
                                    selectedCasas.add(cb.getText());
                                }
                            }
                        }
                    }
                }
            }
        }

        List<Personaje> filtrados = listaPersonajes.stream()
                .filter(p -> p.nombre.toLowerCase().contains(searchText))
                .filter(p -> selectedCasas.isEmpty() || selectedCasas.contains(p.casa))
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: {}", filtrados.size());
        cargarPersonajes(filtrados);
    }

    /**
     * Activa o desactiva el modo de selección múltiple.
     *
     * @author Telmo
     */
    @FXML
    private void toggleSelectionMode() {
        boolean isSelectionMode = !btnExportar.isVisible();

        btnExportar.setVisible(isSelectionMode);
        btnExportar.setManaged(isSelectionMode);
        if (isSelectionMode) {
            btnSeleccionar.setText("CANCELAR");
        } else {
            btnSeleccionar.setText("SELECCIONAR");
        }

        for (ControladorFichaPersonaje controller : listaControladores) {
            controller.setSelectionMode(isSelectionMode);
        }
    }

    /**
     * Exporta los personajes seleccionados.
     *
     * @author Telmo
     */
    @FXML
    private void exportarSeleccionados() {
        List<String> seleccionados = new ArrayList<>();
        for (ControladorFichaPersonaje controller : listaControladores) {
            if (controller.isSelected()) {
                seleccionados.add(controller.getNombre());
            }
        }

        if (seleccionados.isEmpty()) {
            mandarAlertas(Alert.AlertType.INFORMATION, "Exportar", "", "No has seleccionado ningún personaje");
        } else {
            mandarAlertas(Alert.AlertType.INFORMATION, "Exportar", "", "Exportando: " + String.join(", ", seleccionados));
            // Lógica de exportación real (JasperReports)
        }
    }

    /**
     * Abre una ventana para crear un nuevo personaje.
     *
     * @author Erlantz
     */
    public void onNuevo() {
        try {
            var fxmlResource = getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml");
            if (fxmlResource == null) {
                logger.error("FXML no encontrado: /es/potersitos/fxml/nuevoPersonaje.fxml");
                return;
            }

            ResourceBundle bundle = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.of("es"));
            FXMLLoader loader = new FXMLLoader(fxmlResource, bundle);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloNuevo.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                    logger.debug("Hoja de estilo CSS aplicada correctamente.");
                }
            } catch (Exception e) {
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            Stage stage = new Stage();
            stage.setTitle("Crear Nuevo Personaje");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            logger.error("Error al abrir el formulario de nuevo personaje", e);
        }
    }

    /**
     * Crea los 3 tipos de archivos (CSV, XML y BINARIO).
     *
     * @author Erlantz
     */
    public void crearArchivos() {
        // Implementación futura.
    }

    /**
     * Exporta los alumnos seleccionados a PDF.
     *
     * @author Telmo
     */
    public void exportarPersonajes() {
        // Implementación futura.
    }

    /**
     * Cierra la ventana principal de la aplicación.
     *
     * @author Erlantz
     */
    @FXML
    private void salir() {
        if (menuBar != null && menuBar.getScene() != null) {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            logger.info("Cerrando aplicación desde menú Archivo → Salir...");
            stage.close();
        } else {
            logger.error("No se pudo obtener el Stage para cerrar la ventana.");
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
}