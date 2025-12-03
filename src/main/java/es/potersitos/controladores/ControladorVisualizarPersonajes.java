package es.potersitos.controladores;

import es.potersitos.dao.UsuariosDAO;
import es.potersitos.modelos.Usuarios;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.Node; // NECESARIO para el filtro

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ControladorVisualizarPersonajes implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);
    private UsuariosDAO usuariosDAO;

    @FXML private TilePane tilePanePersonajes;
    @FXML private VBox filterPanel;
    @FXML private Accordion accordionFiltros;
    @FXML private TextField searchField;

    private ResourceBundle resources;
    private List<Usuarios> allUsuarios = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.usuariosDAO = new UsuariosDAO();
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        cargarTodosLosUsuarios();
        cargarPersonajes(allUsuarios);

        // 1. Configurar listener de texto para filtrar en tiempo real
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPersonajes();
        });

        // 2. Configurar listeners para los CheckBoxes (El método completo está abajo)
        configurarListenersFiltros();
    }

    private void cargarTodosLosUsuarios() {
        logger.debug("Llamando a UsuariosDAO para obtener datos.");
        this.allUsuarios = usuariosDAO.obtenerTodos();
        logger.info("Total de usuarios cargados: {}", allUsuarios.size());
    }

    private void cargarPersonajes(List<Usuarios> personajes) {
        tilePanePersonajes.getChildren().clear();
        for (Usuarios u : personajes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();

                // Pasamos los datos del modelo real (Usuarios) a la ficha
                controller.setData(
                        u.getName(),
                        u.getHouse(),
                        u.getImageUrl()
                );
                controller.setPersonajeSlug(u.getId());

                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
            }
        }
    }

    // MÉTODO FALTANTE (AHORA COMPLETO)
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


    @FXML
    private void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        logger.info("Panel de filtros " + (isVisible ? "ocultado" : "mostrado"));
    }

    @FXML
    private void limpiarFiltros(javafx.event.ActionEvent event) {
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
        // Reload all characters after clearing filters
        cargarPersonajes(allUsuarios);
    }

    @FXML
    private void aplicarFiltros() {
        filtrarPersonajes();
    }

    private void filtrarPersonajes() {
        String searchText = searchField.getText().toLowerCase();
        List<String> selectedCasas = new ArrayList<>();

        // Get selected houses from the filter
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

        // Aplicar filtros al modelo real
        List<Usuarios> filtrados = allUsuarios.stream()
                .filter(u -> u.getName().toLowerCase().contains(searchText))
                .filter(u -> selectedCasas.isEmpty() || selectedCasas.contains(u.getHouse()))
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: " + filtrados.size());
        cargarPersonajes(filtrados);
    }

    // Método para manejar el botón EXPORTAR (asumido del FXML)
    @FXML
    private void manejarBotonExportar() {
        logger.info("Funcionalidad de Exportar activada.");
        // Aquí iría la lógica para exportar la lista filtrada a un archivo.
    }
}