package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public Button btnFiltrar, btnCerrarFiltro, btnSeleccionar, btnExportar, btnAplicarFiltro, btnLimpiarFiltro;

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
        // Cargar recurso por defecto
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        // Configurar atajos de teclado
        menuNuevo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        menuImportar.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        menuGuardar.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuSalir.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        this.listaPersonajes = new ArrayList<>();
        this.listaControladores = new ArrayList<>();

        inicializarDatosPrueba();

        // Carga inicial de textos y personajes
        actualizarTextosUI();
        cargarPersonajes(listaPersonajes);
        configurarListenersFiltros();

        // Listener de búsqueda
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filtrarPersonajes());
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
     * Cambia el idioma actual de la interfaz gráfica y actualiza todos los textos.
     *
     * @param nuevoLocale Nueva configuración regional (Locale) que se va a aplicar.
     * @author Erlantz
     */
    private void cambiarIdioma(Locale nuevoLocale) {
        try {
            resources = ResourceBundle.getBundle("es.potersitos.mensaje", nuevoLocale);
            logger.info("Idioma cambiado a: {}", nuevoLocale);

            // 1. Actualizar textos estáticos de la interfaz
            actualizarTextosUI();

            // 2. Recargar las cartas de personajes para que se traduzcan sus etiquetas internas
            cargarPersonajes(listaPersonajes);

            // 3. Re-aplicar filtros si fuera necesario para mantener coherencia
            filtrarPersonajes();

        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null, "No se pudo cambiar el idioma: " + e.getMessage());
            logger.error("Error cambiando idioma", e);
        }
    }

    /**
     * Actualiza los textos de todos los controles de la interfaz según el ResourceBundle actual.
     */
    private void actualizarTextosUI() {
        // --- Barra de Menú (Títulos Principales) ---
        // Asumiendo el orden: 0=Archivo, 1=Idioma, 2=Ayuda
        if (menuBar.getMenus().size() >= 3) {
            menuBar.getMenus().get(0).setText(resources.getString("menu.archivo"));
            menuBar.getMenus().get(1).setText(resources.getString("menu.idioma"));
            menuBar.getMenus().get(2).setText(resources.getString("menu.ayuda"));
        }

        // --- Items del Menú ---
        menuNuevo.setText(resources.getString("menu.archivo.nuevo"));
        menuImportar.setText(resources.getString("menu.archivo.importar"));
        menuGuardar.setText(resources.getString("menu.archivo.guardar"));
        menuSalir.setText(resources.getString("menu.archivo.salir"));

        // --- Botones y Campos Principales ---
        searchField.setPromptText(resources.getString("visualizar.search.prompt"));
        btnFiltrar.setText(resources.getString("visualizar.filtro.titulo"));

        // Logica para botón seleccionar (depende si está activo o no)
        if (btnExportar.isVisible()) {
            btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
        } else {
            btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
        }

        btnExportar.setText(resources.getString("visualizar.btn.exportar"));

        // Botones dentro del panel de filtros (si tienen fx:id asignado en el controlador,
        // si no los tienes inyectados, deberías agregarlos con @FXML o buscarlos dinámicamente)
        // btnAplicarFiltro.setText(resources.getString("visualizar.filtro.aplicar"));
        // btnLimpiarFiltro.setText(resources.getString("visualizar.filtro.limpiar"));

        // --- Acordeón de Filtros ---
        if (accordionFiltros != null && !accordionFiltros.getPanes().isEmpty()) {
            // Actualizar Títulos de los Paneles
            // Asumiendo orden: 0=Casa, 1=Nacionalidad, 2=Especie, 3=Género
            List<TitledPane> panes = accordionFiltros.getPanes();

            if (panes.size() > 0) panes.get(0).setText(resources.getString("filtro.titulo.casa"));
            if (panes.size() > 1) panes.get(1).setText(resources.getString("filtro.titulo.nacionalidad"));
            if (panes.size() > 2) panes.get(2).setText(resources.getString("filtro.titulo.especie"));
            if (panes.size() > 3) panes.get(3).setText(resources.getString("filtro.titulo.genero"));

            // Actualizar CheckBoxes dentro de Casa (Panel 0)
            actualizarCheckBoxesDelPanel(panes.get(0), new String[]{
                    "filtro.valor.gryffindor", "filtro.valor.slytherin",
                    "filtro.valor.hufflepuff", "filtro.valor.ravenclaw"
            });

            // Actualizar CheckBoxes dentro de Nacionalidad (Panel 1)
            if (panes.size() > 1) {
                actualizarCheckBoxesDelPanel(panes.get(1), new String[]{
                        "filtro.valor.britanico", "filtro.valor.irlandes",
                        "filtro.valor.frances", "filtro.valor.bulgaro"
                });
            }

            // Actualizar CheckBoxes dentro de Especie (Panel 2)
            if (panes.size() > 2) {
                actualizarCheckBoxesDelPanel(panes.get(2), new String[]{
                        "filtro.valor.humano", "filtro.valor.mestizo",
                        "filtro.valor.elfo", "filtro.valor.gigante"
                });
            }
            // Actualizar CheckBoxes dentro de Genero (Panel 3)
            if (panes.size() > 3) {
                actualizarCheckBoxesDelPanel(panes.get(3), new String[]{
                        "filtro.valor.masculino", "filtro.valor.femenino"
                });
            }
        }
    }

    /**
     * Método auxiliar para actualizar los textos de los CheckBoxes dentro de un TitledPane
     */
    private void actualizarCheckBoxesDelPanel(TitledPane pane, String[] keys) {
        Node content = pane.getContent();
        if (content instanceof VBox) {
            int index = 0;
            for (Node node : ((VBox) content).getChildren()) {
                if (node instanceof CheckBox && index < keys.length) {
                    ((CheckBox) node).setText(resources.getString(keys[index]));
                    index++;
                }
            }
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
                // Importante: Pasar el ResourceBundle actual al loader
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();
                controller.setData(p.nombre, p.casa, p.imagePath);
                controller.setPersonajeSlug(p.slug);

                // Si estamos en modo selección, mantener el estado visual
                if (btnExportar.isVisible()) {
                    controller.setSelectionMode(true);
                }

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
            // Iteramos sobre todos los paneles
            for (TitledPane pane : accordionFiltros.getPanes()) {
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
                // CORRECCIÓN: Usar la clave del recurso en lugar del texto fijo "Casa"
                // para que funcione en cualquier idioma.
                if (pane.getText().equals(resources.getString("filtro.titulo.casa"))) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox cb) {
                                if (cb.isSelected()) {
                                    // Aquí comparamos el texto del checkbox.
                                    // OJO: Si el filtro depende del valor interno ("Gryffindor")
                                    // pero el texto visible cambia ("Gryffindor" suele ser igual),
                                    // asegúrate de que la lógica de filtrado coincida con el dato del personaje.
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
                // Adaptamos el filtro de casa para que coincida aunque esté traducido
                // (Para simplificar, en HP las casas no suelen traducirse mucho, pero si pasara,
                // habría que mapear el texto visible al valor interno).
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

        // Usamos recursos para los textos de los botones
        if (isSelectionMode) {
            btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
        } else {
            btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
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
            mandarAlertas(Alert.AlertType.INFORMATION,
                    resources.getString("menu.archivo.guardar"),
                    resources.getString("visualizar.alerta.seleccion.titulo"),
                    resources.getString("visualizar.alerta.seleccion.mensaje"));
        } else {
            mandarAlertas(Alert.AlertType.INFORMATION,
                    resources.getString("menu.archivo.guardar"),
                    "",
                    "Exportando: " + String.join(", ", seleccionados));
            // Lógica de exportación real (JasperReports)
        }
    }

    /**
     * Abre una ventana para crear un nuevo personaje.
     *
     * @author Erlantz
     */
    @FXML
    public void onNuevo() {
        try {
            var fxmlResource = getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml");
            if (fxmlResource == null) {
                logger.error("FXML no encontrado: /es/potersitos/fxml/nuevoPersonaje.fxml");
                return;
            }

            // Usar el idioma actual seleccionado para la nueva ventana
            FXMLLoader loader = new FXMLLoader(fxmlResource, this.resources);

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
            stage.setTitle(resources.getString("menu.archivo.nuevo")); // Título traducido
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
    @FXML
    public void crearArchivos() {
        try {
            String userHome = System.getProperty("user.home");
            String proyectoPath = userHome + "\\Reto3_Hogwarts_Anuario";
            new File(proyectoPath).mkdirs();

            String csvPath = proyectoPath + "\\todosPersonajes.csv";
            String xmlPath = proyectoPath + "\\todosPersonajes.xml";
            String binPath = proyectoPath + "\\todosPersonajes.bin";

            String exePath = "lib\\CrearArchivosPotter.exe";
            File exeFile = new File(exePath);
            if (!exeFile.exists()) {
                mandarAlertas(Alert.AlertType.ERROR, "ERROR", "", "EXE no encontrado:\n" + exeFile.getAbsolutePath());
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(exePath, csvPath, xmlPath, binPath);
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    output.append(linea).append("\n");
                }
            }

            int exitCode = proceso.waitFor();
            logger.info("PYTHON OUTPUT: {}", output);

            boolean csvOk = new File(csvPath).exists();
            boolean xmlOk = new File(xmlPath).exists();
            boolean binOk = new File(binPath).exists();

            if (csvOk && xmlOk && binOk) {
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "", "3 Archivos creados:\n" + proyectoPath);
            } else {
                mandarAlertas(Alert.AlertType.ERROR, "FALLÓ", "", String.format("ExitCode: %d\nCSV: %s\nXML: %s\nBIN: %s\n\n%s", exitCode, csvOk, xmlOk, binOk, output));
            }
        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", e.getMessage());
        }
    }

    /**
     * Exporta los alumnos seleccionados a PDF.
     *
     * @author Telmo
     */
    @FXML
    public void exportarPersonajes() {
        // Implementación futura.
    }

    /**
     *
     */
    @FXML
    public void documentacion() throws IOException {

    }

    /**
     *
     */
    @FXML
    public void acercaDe() {
        String mensaje = """
        🎓 RETO3 HOGWARTS ANUARIO
        
        📚 Creado por:
        • Erlantz García
        • Telmo Castillo
        • Marco Muro
        • Nizam Abdel-Ghaffar
        
        🏫 DEIN - Desarrollo de Interfaces
        🔮 Universo Harry Potter API
        
        🚀 Python + JavaFX + PyInstaller
        
        💻 Ultima modificación: 19 de Diciembre de 2025
        """;
        mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("menu.ayuda.acercade"), null, mensaje);
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