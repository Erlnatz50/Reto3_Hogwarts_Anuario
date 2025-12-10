package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
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

    /** Botones generales de la interfaz. */
    @FXML
    public Button btnFiltrar, btnCerrarFiltro, btnSeleccionar, btnExportar, btnAplicarFiltro, btnLimpiarFiltro, btnSiguiente, btnAnterior;

    /** Etiqueta que muestra la página actual del paginador. */
    @FXML
    private Label lblPaginaActual;

    /** Barra de menú principal. */
    @FXML
    public MenuBar menuBar;

    /** Elementos del menú superior. */
    @FXML
    public MenuItem menuSalir, menuNuevo, menuImportar, menuGuardar;

    /** Contenedor que aloja las fichas de los personajes. */
    @FXML
    private TilePane tilePanePersonajes;

    /** Panel lateral de filtros. */
    @FXML
    private VBox filterPanel;

    /** Acordeón que contiene todos los grupos de filtros. */
    @FXML
    private Accordion accordionFiltros;

    /** Campo de texto usado para realizar búsquedas por nombre. */
    @FXML
    private TextField searchField;

    /** Lista de controladores asociados a cada ficha de personaje cargado. */
    private List<ControladorFichaPersonaje> listaControladores;

    /** Lista mapeada de todos los personajes leídos desde CSV/XML. */
    private List<Map<String, String>> listaPersonajesMapeados;

    /** Recurso de internacionalización (idioma actual). */
    private ResourceBundle resources;

    /** Logger para registrar eventos y errores. */
    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    /** Número de personajes que se muestran por página. */
    private static final int personajesPorPagina = 10;

    /** Página activa actual. */
    private int paginaActual = 1;

    /** Total de páginas calculadas con los personajes disponibles. */
    private int totalPaginas;

    /**
     * Metodo de inicialización del controlador.
     *
     * @author Telmo
     */
    @FXML
    public void initialize() {
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());

        configurarAtajosMenu();

        configurarBusqueda();

        configurarListenersFiltros();

        listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();

        calcularTotalPaginas();

        actualizarTextosUI();

        if(listaPersonajesMapeados.isEmpty()){
            mostrarMensajeImportar();
        } else {
            cargarPersonajes(listaPersonajesMapeados);
        }

        logger.info("Vista de personajes inicializada correctamente con {} registros.", listaPersonajesMapeados.size());
    }

    /**
     *
     */
    private void configurarAtajosMenu(){
        menuNuevo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        menuImportar.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        menuGuardar.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuSalir.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
    }

    /**
     *
     */
    private void configurarBusqueda(){
        if (searchField != null){
            searchField.textProperty().addListener((o, ov, nv) -> filtrarPersonajes());
        }
    }

    /**
     *
     */
    private void calcularTotalPaginas(){
        int total = Math.max(1, listaPersonajesMapeados.size());
        totalPaginas = (int) Math.ceil((double) total / personajesPorPagina);
    }

    /**
     * Muestra un mensaje centrado indicando que no se han encontrado personajes
     * y que el usuario debe importarlos.
     *
     * @author Erlantz
     */
    private void mostrarMensajeImportar() {
        if (tilePanePersonajes != null) {
            tilePanePersonajes.getChildren().clear();

            Label mensaje = new Label("⚠️ No se encontraron personajes.\n\nPor favor, importa los archivos desde el menú 'Archivo → Importar'.");
            mensaje.setStyle("-fx-text-alignment: center; -fx-font-size: 18px; -fx-text-fill: #555; -fx-padding: 40px;");
            mensaje.setWrapText(true);

            VBox contenedor = new VBox(mensaje);
            contenedor.setStyle("-fx-alignment: center;");
            tilePanePersonajes.getChildren().add(contenedor);
        }
    }

    /**
     * Cambia el idioma a español.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaEspaniol() {
        cambiarIdioma(Locale.of("es"));
    }

    /**
     * Cambia el idioma a euskera.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaEuskera() {
        cambiarIdioma(Locale.of("eu"));
    }

    /**
     * Cambia el idioma a inglés.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaIngles() {
        cambiarIdioma(Locale.ENGLISH);
    }

    /**
     * Cambia el idioma (locale) activo y actualiza todos los textos visibles en la interfaz.
     *
     * @param nuevoLocale Nuevo idioma a aplicar.
     * @author Erlantz
     */
    private void cambiarIdioma(Locale nuevoLocale) {
        try {
            resources = ResourceBundle.getBundle("es.potersitos.mensaje", nuevoLocale);
            actualizarTextosUI();
            cargarPersonajes(listaPersonajesMapeados);
            filtrarPersonajes();
            logger.info("Idioma cambiado a: {}", nuevoLocale);
        } catch (Exception e) {
            logger.error("Error cambiando idioma", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null, "No se pudo cambiar el idioma: " + e.getMessage());
        }
    }

    /**
     * Actualiza los textos de todos los controles de la interfaz según el ResourceBundle actual.
     */
    private void actualizarTextosUI() {
        if (menuBar != null && menuBar.getMenus().size() >= 3) {
            menuBar.getMenus().get(0).setText(resources.getString("menu.archivo"));
            menuBar.getMenus().get(1).setText(resources.getString("menu.idioma"));
            menuBar.getMenus().get(2).setText(resources.getString("menu.ayuda"));
        }

        menuNuevo.setText(resources.getString("menu.archivo.nuevo"));
        menuImportar.setText(resources.getString("menu.archivo.importar"));
        menuGuardar.setText(resources.getString("menu.archivo.guardar"));
        menuSalir.setText(resources.getString("menu.archivo.salir"));

        searchField.setPromptText(resources.getString("visualizar.search.prompt"));
        btnFiltrar.setText(resources.getString("visualizar.filtro.titulo"));
        btnExportar.setText(resources.getString("visualizar.btn.exportar"));
        btnAnterior.setText(resources.getString("paginacion.anterior"));
        btnSiguiente.setText(resources.getString("paginacion.siguiente"));
        btnAplicarFiltro.setText(resources.getString("visualizar.filtro.aplicar"));
        btnLimpiarFiltro.setText(resources.getString("visualizar.filtro.limpiar"));

        if (btnExportar.isVisible()) {
            btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
        } else {
            btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
        }

        if (accordionFiltros != null && !accordionFiltros.getPanes().isEmpty()) {
            List<TitledPane> panes = accordionFiltros.getPanes();
            panes.get(0).setText(resources.getString("filtro.titulo.casa"));
            if (panes.size() > 1) panes.get(1).setText(resources.getString("filtro.titulo.nacionalidad"));
            if (panes.size() > 2) panes.get(2).setText(resources.getString("filtro.titulo.especie"));
            if (panes.size() > 3) panes.get(3).setText(resources.getString("filtro.titulo.genero"));

            actualizarCheckBoxesDelPanel(panes.get(0), new String[]{
                    "filtro.valor.gryffindor", "filtro.valor.slytherin",
                    "filtro.valor.hufflepuff", "filtro.valor.ravenclaw"
            });
            if (panes.size() > 1) {
                actualizarCheckBoxesDelPanel(panes.get(1), new String[]{
                        "filtro.valor.britanico", "filtro.valor.irlandes",
                        "filtro.valor.frances", "filtro.valor.bulgaro"
                });
            }
            if (panes.size() > 2) {
                actualizarCheckBoxesDelPanel(panes.get(2), new String[]{
                        "filtro.valor.humano", "filtro.valor.mestizo",
                        "filtro.valor.elfo", "filtro.valor.gigante"
                });
            }
            if (panes.size() > 3) {
                actualizarCheckBoxesDelPanel(panes.get(3), new String[]{
                        "filtro.valor.masculino", "filtro.valor.femenino"
                });
            }
        }
        actualizarControlesPaginacion();
    }

    /**
     * Actualiza los textos de los {@link CheckBox} dentro de un {@link TitledPane}.
     *
     * @param pane Contenedor que aloja los CheckBoxes.
     * @param keys Claves del {@link ResourceBundle} correspondientes a los textos.
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
     *
     *
     * @param personajes
     */
    private void cargarPersonajes(List<Map<String, String>> personajes) {
        if (tilePanePersonajes == null) return;

        tilePanePersonajes.getChildren().clear();
        listaControladores = new ArrayList<>();

        int totalPersonajesLista = personajes.size();
        if (totalPersonajesLista == 0) {
            actualizarControlesPaginacion();
            return;
        }

        int indiceInicio = (paginaActual - 1) * personajesPorPagina;

        if (indiceInicio >= totalPersonajesLista) {
            this.paginaActual = Math.max(1, totalPaginas);
            indiceInicio = (paginaActual - 1) * personajesPorPagina;
        }

        int indiceFin = Math.min(indiceInicio + personajesPorPagina, totalPersonajesLista);

        List<Map<String, String>> personajesPagina = personajes.subList(indiceInicio, indiceFin);
        logger.info("Cargando Página {}: Personajes de índice {} a {}. (Total: {})",
                paginaActual, indiceInicio, indiceFin, personajesPagina.size());

        for (Map<String, String> p : personajesPagina) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();

                String nombre = p.getOrDefault("name", "N/A");
                String casa = p.getOrDefault("house", "Desconocida");
                String imagePath = p.getOrDefault("image", "");
                String slug = p.getOrDefault("slug", UUID.randomUUID().toString());

                controller.setData(nombre, casa, imagePath);
                controller.setPersonajeSlug(slug);

                if (btnExportar != null && btnExportar.isVisible()) {
                    controller.setSelectionMode(true);
                }

                listaControladores.add(controller);
                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
            }
        }
        actualizarControlesPaginacion();
    }

    /**
     *
     *
     * @param nuevaPagina
     */
    public void setPaginaActual(int nuevaPagina) {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            this.paginaActual = nuevaPagina;
            cargarPersonajes(listaPersonajesMapeados);
            logger.debug("Página cambiada a {}", nuevaPagina);
        } else {
            logger.warn("Número de página {} fuera de rango (1 - {}).", nuevaPagina, totalPaginas);
        }
    }

    /**
     *
     */
    @FXML
    private void paginaAnterior() {
        if (paginaActual > 1) {
            setPaginaActual(paginaActual - 1);
        }
    }

    /**
     *
     */
    @FXML
    private void siguientePagina() {
        if (paginaActual < totalPaginas) {
            setPaginaActual(paginaActual + 1);
        }
    }

    /**
     *
     */
    private void actualizarControlesPaginacion() {
        if (lblPaginaActual == null || btnAnterior == null || btnSiguiente == null) {
            return;
        }

        if (totalPaginas == 0) {
            lblPaginaActual.setText("Página 0 de 0");
            btnAnterior.setDisable(true);
            btnSiguiente.setDisable(true);
        } else {
            lblPaginaActual.setText(String.format("Página %d de %d", paginaActual, totalPaginas));
            btnAnterior.setDisable(paginaActual == 1);
            btnSiguiente.setDisable(paginaActual == totalPaginas);
        }
    }

    /**
     *
     */
    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if (resources != null && pane.getText().equals(resources.getString("filtro.titulo.casa"))) {
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
     *
     */
    @FXML
    private void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        logger.info("Panel de filtros {}", isVisible ? "ocultado" : "mostrado");
    }

    /**
     *
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
        if (searchField != null) searchField.setText("");
        this.paginaActual = 1;
        cargarPersonajes(listaPersonajesMapeados);
    }

    /**
     *
     */
    @FXML
    private void aplicarFiltros() {
        filtrarPersonajes();
    }

    /**
     *
     */
    private void filtrarPersonajes() {
        String searchText = (searchField != null) ? searchField.getText().toLowerCase() : "";
        List<String> selectedCasas = new ArrayList<>();

        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if (resources != null && pane.getText().equals(resources.getString("filtro.titulo.casa"))) {
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

        List<Map<String, String>> filtrados = listaPersonajesMapeados.stream()
                .filter(p -> p.getOrDefault("name", "").toLowerCase().contains(searchText))
                .filter(p -> selectedCasas.isEmpty() || selectedCasas.contains(p.get("house")))
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: {}", filtrados.size());

        int totalFiltrados = filtrados.size();
        this.totalPaginas = (int) Math.ceil((double) totalFiltrados / personajesPorPagina);
        this.paginaActual = 1;

        cargarPersonajes(filtrados);
    }

    /**
     *
     */
    @FXML
    private void toggleSelectionMode() {
        boolean isSelectionMode = (btnExportar != null && !btnExportar.isVisible());

        if (btnExportar != null) {
            btnExportar.setVisible(isSelectionMode);
            btnExportar.setManaged(isSelectionMode);
        }

        if (btnSeleccionar != null) {
            if (isSelectionMode) {
                btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
            } else {
                btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
            }
        }

        for (ControladorFichaPersonaje controller : listaControladores) {
            controller.setSelectionMode(isSelectionMode);
        }
    }

    /**
     *
     */
    @FXML
    private void exportarSeleccionados() {
        // Lógica de exportación
    }

    /**
     *
     */
    @FXML
    public void onNuevo() {
        try {
            var fxmlResource = getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml");
            if (fxmlResource == null) {
                logger.error("FXML no encontrado: /es/potersitos/fxml/nuevoPersonaje.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource, this.resources);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloNuevo.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                }
            } catch (Exception e) {
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            Stage stage = new Stage();
            stage.setTitle(resources.getString("menu.archivo.nuevo"));
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            logger.error("Error al abrir el formulario de nuevo personaje", e);
        }
    }


    /**
     *
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

                listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
                calcularTotalPaginas();
                cargarPersonajes(listaPersonajesMapeados);
            } else {
                mandarAlertas(Alert.AlertType.ERROR, "FALLÓ", "", String.format("ExitCode: %d\nCSV: %s\nXML: %s\nBIN: %s\n\n%s", exitCode, csvOk, xmlOk, binOk, output));
            }
        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", e.getMessage());
        }
    }

    /**
     *
     */
    @FXML
    public void exportarPersonajes() {
        // Implementación futura.
    }

    /**
     *
     */
    @FXML
    public void documentacion() {
        // Implementación futura
    }

    /**
     *
     */
    @FXML
    public void acercaDe() {
        // Lógica de acercaDe
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
     *
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
     *
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