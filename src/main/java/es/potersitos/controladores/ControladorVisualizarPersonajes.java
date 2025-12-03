package es.potersitos.controladores;

import es.potersitos.dao.UsuariosDAO;
import es.potersitos.modelos.Usuarios;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ControladorVisualizarPersonajes implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);
    private UsuariosDAO usuariosDAO;

    // --- VARIABLES DE PAGINACIÓN ---
    private final int ITEMS_PER_PAGE = 24;
    private int currentPage = 1;
    private List<Usuarios> allUsuarios = new ArrayList<>();
    private List<Usuarios> filteredUsuarios = new ArrayList<>();

    // --- ELEMENTOS FXML CRÍTICOS PARA EL FILTRO ---
    @FXML private VBox filterPanel; // CLAVE: Contenedor del panel lateral
    @FXML private Button botonAbrirFiltros; // Botón "Filtrar"
    @FXML private Accordion accordionFiltros;
    // --- Otros elementos ---
    @FXML private TilePane tilePanePersonajes;
    @FXML private TextField searchField;
    @FXML private HBox paginacionHBox;
    @FXML private Label estadoLabel;

    private ResourceBundle resources;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.usuariosDAO = new UsuariosDAO();
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        cargarTodosLosUsuarios();
        this.filteredUsuarios.addAll(allUsuarios);

        renderizarPaginaActual();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPersonajes();
        });

        configurarListenersFiltros();
    }

    // --- MÉTODO CLAVE PARA EL BOTÓN "FILTRAR" ---
    @FXML
    private void toggleFilterPanel() {
        // Verifica si el panel está visible
        boolean isVisible = filterPanel.isVisible();

        // 1. Alterna la visibilidad
        filterPanel.setVisible(!isVisible);

        // 2. CLAVE: Alterna el managed property para que el BorderPane ajuste el espacio
        filterPanel.setManaged(!isVisible);

        logger.info("Panel de filtros " + (isVisible ? "ocultado" : "mostrado"));
    }

    // [MÉTODOS DE DATOS Y LÓGICA (cargarTodosLosUsuarios, filtrarPersonajes, etc.)]
    // ... (El resto del código de la lógica de datos y paginación debe estar aquí) ...

    private void cargarTodosLosUsuarios() {
        this.allUsuarios = usuariosDAO.obtenerTodos();
        logger.info("Total de usuarios cargados: {}", allUsuarios.size());
    }

    private void cargarFichas(List<Usuarios> personajes) {
        tilePanePersonajes.getChildren().clear();
        for (Usuarios u : personajes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();
                ControladorFichaPersonaje controller = loader.getController();

                controller.setData(u.getName(), u.getHouse(), u.getImageUrl());
                controller.setPersonajeSlug(u.getId());

                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje: " + u.getName(), e);
            }
        }
    }

    private void renderizarPaginaActual() {
        int totalItems = filteredUsuarios.size();
        if (totalItems == 0) {
            tilePanePersonajes.getChildren().clear();
            // ...
            return;
        }
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
        List<Usuarios> usuariosPagina = filteredUsuarios.subList(startIndex, endIndex);

        cargarFichas(usuariosPagina);
        // ... (resto de la paginación)
    }

    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if ("Casa".equals(pane.getText())) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox) {
                                CheckBox cb = (CheckBox) node;
                                cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                    filtrarPersonajes();
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML private void limpiarFiltros(javafx.event.ActionEvent event) {
        // ...
    }
    @FXML private void aplicarFiltros() {
        // ...
    }
    private void filtrarPersonajes() {
        // ...
    }
    private void actualizarBotonesPaginacion(int totalItems) {
        // ...
    }
    @FXML private void manejarBotonExportar() {
        // ...
    }
}