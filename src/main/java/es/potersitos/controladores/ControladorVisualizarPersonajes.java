package es.potersitos.controladores;

import es.potersitos.dao.UsuariosDAO;
import es.potersitos.modelos.Usuarios;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button; // Necesario para paginación
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label; // Necesario para paginación
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox; // Necesario para paginación
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

    // --- LISTAS DE DATOS ---
    private List<Usuarios> allUsuarios = new ArrayList<>(); // Todos los datos cargados del DAO
    private List<Usuarios> filteredUsuarios = new ArrayList<>(); // Lista filtrada sobre la que paginamos

    // --- ELEMENTOS FXML ---
    @FXML private TilePane tilePanePersonajes;
    @FXML private VBox filterPanel;
    @FXML private Accordion accordionFiltros;
    @FXML private TextField searchField;
    @FXML private HBox paginacionHBox; // Debe existir en tu FXML
    @FXML private Label estadoLabel; // Debe existir en tu FXML

    private ResourceBundle resources;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.usuariosDAO = new UsuariosDAO();
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        cargarTodosLosUsuarios();

        // Al iniciar, los filtrados son todos los usuarios
        this.filteredUsuarios.addAll(allUsuarios);

        // Carga inicial de la primera página
        renderizarPaginaActual();

        // 1. Configurar listener de texto para filtrar en tiempo real
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPersonajes();
        });

        // 2. Configurar listeners para los CheckBoxes
        configurarListenersFiltros();
    }

    private void cargarTodosLosUsuarios() {
        this.allUsuarios = usuariosDAO.obtenerTodos();
        logger.info("Total de usuarios cargados: {}", allUsuarios.size());
    }

    // --- 1. LÓGICA DE RENDERIZADO POR PÁGINA (Paso Clave) ---
    private void renderizarPaginaActual() {
        int totalItems = filteredUsuarios.size();

        if (totalItems == 0) {
            tilePanePersonajes.getChildren().clear();
            actualizarBotonesPaginacion(0);
            estadoLabel.setText("No hay coincidencias.");
            return;
        }

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        // Obtener la sublista de 24 elementos
        List<Usuarios> usuariosPagina = filteredUsuarios.subList(startIndex, endIndex);

        // Cargar visualmente solo la sublista
        cargarFichas(usuariosPagina);

        // Actualizar botones y etiquetas de estado
        actualizarBotonesPaginacion(totalItems);
        estadoLabel.setText(String.format("Mostrando %d - %d de %d usuarios totales.",
                startIndex + 1, endIndex, totalItems));
    }

    // Método auxiliar que carga las fichas visualmente
    private void cargarFichas(List<Usuarios> personajes) {
        tilePanePersonajes.getChildren().clear();
        for (Usuarios u : personajes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();

                // Pasamos los datos del modelo real (Usuarios) a la ficha
                controller.setData(u.getName(), u.getHouse(), u.getImageUrl());
                controller.setPersonajeSlug(u.getId());

                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje: " + u.getName(), e);
            }
        }
    }

    // --- 2. LÓGICA DE FILTROS ---
    @FXML
    private void aplicarFiltros() {
        filtrarPersonajes();
    }

    private void filtrarPersonajes() {
        String searchText = searchField.getText().toLowerCase();
        List<String> selectedCasas = new ArrayList<>();

        // [Lógica para obtener selectedCasas — omitida por brevedad, asume que está aquí]
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if ("Casa".equals(pane.getText())) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox) {
                                CheckBox cb = (CheckBox) node;
                                if (cb.isSelected()) {
                                    selectedCasas.add(cb.getText());
                                }
                            }
                        }
                    }
                }
            }
        }

        // Aplicar filtros a la lista completa
        this.filteredUsuarios = allUsuarios.stream()
                .filter(u -> u.getName().toLowerCase().contains(searchText))
                .filter(u -> selectedCasas.isEmpty() || selectedCasas.contains(u.getHouse()))
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: " + filteredUsuarios.size());

        // Reiniciar a la página 1 y renderizar
        this.currentPage = 1;
        renderizarPaginaActual();
    }

    // --- 3. LÓGICA DE PAGINACIÓN ---

    private void goToPage(int page) {
        int totalPages = (int) Math.ceil((double) filteredUsuarios.size() / ITEMS_PER_PAGE);

        if (page >= 1 && page <= totalPages) {
            this.currentPage = page;
            renderizarPaginaActual();
            // Lógica para hacer scroll al inicio de la página si tienes un ScrollPane
            // [Si tu ScrollPane está inyectado: scrollPaneUsuarios.setVvalue(0.0); ]
        }
    }

    private void actualizarBotonesPaginacion(int totalItems) {
        paginacionHBox.getChildren().clear();
        if (totalItems == 0) return;

        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        Button prevButton = new Button("Anterior");
        // Enlazar el botón Anterior a goToPage(currentPage - 1)
        prevButton.setOnAction(e -> goToPage(currentPage - 1));
        prevButton.setDisable(currentPage == 1);

        Button nextButton = new Button("Siguiente");
        // Enlazar el botón Siguiente a goToPage(currentPage + 1)
        nextButton.setOnAction(e -> goToPage(currentPage + 1));
        nextButton.setDisable(currentPage == totalPages);

        paginacionHBox.getChildren().add(prevButton);

        Label pageInfo = new Label(String.format("Pág. %d de %d", currentPage, totalPages));
        pageInfo.setStyle("-fx-padding: 0 10; -fx-font-weight: bold;");
        paginacionHBox.getChildren().add(pageInfo);

        paginacionHBox.getChildren().add(nextButton);
    }

    // --- MÉTODOS DE ACCIÓN (del FXML) ---

    @FXML
    private void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        logger.info("Panel de filtros " + (isVisible ? "ocultado" : "mostrado"));
    }

    @FXML
    private void limpiarFiltros(javafx.event.ActionEvent event) {
        // [Lógica para deseleccionar todos los checkboxes]

        // Reiniciar filtros y cargar primera página
        this.filteredUsuarios.addAll(allUsuarios); // Restablecer la lista filtrada
        searchField.clear(); // Limpiar búsqueda
        this.currentPage = 1;
        renderizarPaginaActual();
    }

    @FXML
    private void manejarBotonExportar() {
        logger.info("Funcionalidad de Exportar activada para {} elementos.", filteredUsuarios.size());
        // Aquí iría la lógica para exportar la lista 'filteredUsuarios'.
    }

    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
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