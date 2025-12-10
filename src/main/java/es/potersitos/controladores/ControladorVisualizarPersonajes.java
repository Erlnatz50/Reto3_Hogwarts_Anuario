package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.event.ActionEvent;
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
 * @author Telmo / Modificado por Gemini
 * @version 1.9 (Control de nulidad en UI y paginación)
 */
public class ControladorVisualizarPersonajes {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    // --- VARIABLES DE PAGINACIÓN INTERNAS ---
    private final int personajesPorPagina = 10;
    private int paginaActual = 1;
    private int totalPaginas;
    // -------------------------------------

    // NOTA: Estas variables están protegidas en actualizarTextosUI() y initialize()
    @FXML
    public Button btnFiltrar, btnCerrarFiltro, btnSeleccionar, btnExportar, btnAplicarFiltro, btnLimpiarFiltro;

    // --- ELEMENTOS FXML DE PAGINACIÓN ---
    @FXML
    private Button btnAnterior;
    @FXML
    private Button btnSiguiente;
    @FXML
    private Label lblPaginaActual;
    // ------------------------------------

    @FXML
    public MenuBar menuBar;

    @FXML
    public MenuItem menuSalir, menuNuevo, menuImportar, menuGuardar;

    @FXML
    private TilePane tilePanePersonajes;

    @FXML
    private VBox filterPanel;

    @FXML
    private Accordion accordionFiltros;

    @FXML
    private TextField searchField;

    private List<ControladorFichaPersonaje> listaControladores;
    private List<Map<String, String>> listaPersonajesMapeados;
    private ResourceBundle resources;

    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        menuNuevo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        menuImportar.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        menuGuardar.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuSalir.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        this.listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
        this.listaControladores = new ArrayList<>();

        int totalPersonajes = this.listaPersonajesMapeados.size();
        this.totalPaginas = (int) Math.ceil((double) totalPersonajes / personajesPorPagina);
        logger.info("Total de personajes: {}. Calculadas {} páginas.", totalPersonajes, totalPaginas);

        actualizarTextosUI();
        cargarPersonajes(listaPersonajesMapeados);

        if (searchField != null) searchField.textProperty().addListener((observable, oldValue, newValue) -> filtrarPersonajes());
        configurarListenersFiltros();
    }

    // -----------------------------------------------------------
    // MÉTODOS DE IDIOMA Y TRADUCCIÓN
    // -----------------------------------------------------------

    @FXML
    void idiomaEspaniol() {
        cambiarIdioma(Locale.of("es"));
    }

    @FXML
    void idiomaEuskera() {
        cambiarIdioma(Locale.of("eu"));
    }

    @FXML
    void idiomaIngles() {
        cambiarIdioma(Locale.ENGLISH);
    }

    private void cambiarIdioma(Locale nuevoLocale) {
        try {
            resources = ResourceBundle.getBundle("es.potersitos.mensaje", nuevoLocale);
            logger.info("Idioma cambiado a: {}", nuevoLocale);
            actualizarTextosUI();
            cargarPersonajes(listaPersonajesMapeados);
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
        // --- Barra de Menú (Añadida protección contra Null) ---
        if (menuBar != null && menuBar.getMenus().size() >= 3) {
            menuBar.getMenus().get(0).setText(resources.getString("menu.archivo"));
            menuBar.getMenus().get(1).setText(resources.getString("menu.idioma"));
            menuBar.getMenus().get(2).setText(resources.getString("menu.ayuda"));
        }

        if (menuNuevo != null) menuNuevo.setText(resources.getString("menu.archivo.nuevo"));
        if (menuImportar != null) menuImportar.setText(resources.getString("menu.archivo.importar"));
        if (menuGuardar != null) menuGuardar.setText(resources.getString("menu.archivo.guardar"));
        if (menuSalir != null) menuSalir.setText(resources.getString("menu.archivo.salir"));

        // --- Botones y Campos Principales ---
        if (searchField != null) searchField.setPromptText(resources.getString("visualizar.search.prompt"));
        if (btnFiltrar != null) btnFiltrar.setText(resources.getString("visualizar.filtro.titulo"));

        if (btnSeleccionar != null) {
            if (btnExportar != null && btnExportar.isVisible()) {
                btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
            } else {
                btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
            }
        }

        if (btnExportar != null) btnExportar.setText(resources.getString("visualizar.btn.exportar"));

        // --- PAGINACIÓN ---
        if (btnAnterior != null) btnAnterior.setText(resources.getString("paginacion.anterior"));
        if (btnSiguiente != null) btnSiguiente.setText(resources.getString("paginacion.siguiente"));

        // --- FILTROS Y CONTROLES DEL ACORDEÓN ---
        if (btnAplicarFiltro != null) btnAplicarFiltro.setText(resources.getString("visualizar.filtro.aplicar"));
        if (btnLimpiarFiltro != null) btnLimpiarFiltro.setText(resources.getString("visualizar.filtro.limpiar"));

        if (accordionFiltros != null && !accordionFiltros.getPanes().isEmpty()) {
            List<TitledPane> panes = accordionFiltros.getPanes();

            if (panes.size() > 0) panes.get(0).setText(resources.getString("filtro.titulo.casa"));
            if (panes.size() > 1) panes.get(1).setText(resources.getString("filtro.titulo.nacionalidad"));
            if (panes.size() > 2) panes.get(2).setText(resources.getString("filtro.titulo.especie"));
            if (panes.size() > 3) panes.get(3).setText(resources.getString("filtro.titulo.genero"));

            actualizarCheckBoxesDelPanel(panes.get(0), new String[]{
                    "filtro.valor.gryffindor", "filtro.valor.slytherin",
                    "filtro.valor.hufflepuff", "filtro.valor.ravenclaw"
            });
            // (El resto de la lógica de actualización de CheckBoxes se mantiene igual)
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

    // -----------------------------------------------------------
    // MÉTODOS DE DATOS Y PAGINACIÓN
    // -----------------------------------------------------------

    private void cargarPersonajes(List<Map<String, String>> personajes) {
        if (tilePanePersonajes == null) return;

        tilePanePersonajes.getChildren().clear();
        listaControladores.clear();

        int totalPersonajesLista = personajes.size();

        if (totalPersonajesLista == 0) {
            logger.warn("Lista de personajes vacía.");
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

    public void setPaginaActual(int nuevaPagina) {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            this.paginaActual = nuevaPagina;
            cargarPersonajes(listaPersonajesMapeados);
            logger.debug("Página cambiada a {}", nuevaPagina);
        } else {
            logger.warn("Número de página {} fuera de rango (1 - {}).", nuevaPagina, totalPaginas);
        }
    }

    @FXML
    private void paginaAnterior() {
        if (paginaActual > 1) {
            setPaginaActual(paginaActual - 1);
        }
    }

    @FXML
    private void siguientePagina() {
        if (paginaActual < totalPaginas) {
            setPaginaActual(paginaActual + 1);
        }
    }

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

    // -----------------------------------------------------------
    // MÉTODOS DE FILTRADO Y LÓGICA DE UI
    // -----------------------------------------------------------

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

    @FXML
    private void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        logger.info("Panel de filtros {}", isVisible ? "ocultado" : "mostrado");
    }

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

    @FXML
    private void aplicarFiltros() {
        filtrarPersonajes();
    }

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

    @FXML
    private void exportarSeleccionados() {
        // Lógica de exportación
    }

    @FXML
    public void onNuevo() {
        // Lógica de onNuevo
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

    @FXML
    public void crearArchivos() {
        // Lógica de crearArchivos
    }

    @FXML
    public void exportarPersonajes() {
        // Implementación futura.
    }

    @FXML
    public void documentacion() throws IOException {
        // Implementación futura
    }

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

    private void mandarAlertas(Alert.AlertType tipo, String titulo, String mensajeTitulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(mensajeTitulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        logger.debug("Alerta mostrada: tipo={}, mensaje={}", tipo, mensaje);
    }
}