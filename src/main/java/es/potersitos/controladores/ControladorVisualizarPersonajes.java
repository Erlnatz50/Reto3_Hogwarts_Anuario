package es.potersitos.controladores;

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ControladorVisualizarPersonajes implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    @FXML
    private TilePane tilePanePersonajes;

    @FXML
    private VBox filterPanel;

    @FXML
    private Accordion accordionFiltros;

    @FXML
    private TextField searchField;

    private ResourceBundle resources;

    // Inner class to represent a character
    private static class Personaje {
        String nombre;
        String casa;
        String imagePath;
        String slug; // <--- CAMBIO 1: Añadido SLUG

        public Personaje(String nombre, String casa, String imagePath, String slug) { // <--- CAMBIO 2: Constructor con SLUG
            this.nombre = nombre;
            this.casa = casa;
            this.imagePath = imagePath;
            this.slug = slug;
        }
    }

    private List<Personaje> listaPersonajes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        logger.info("Inicializando ControladorVisualizarPersonajes...");

        // Initialize dummy data
        inicializarDatosPrueba();

        // Load all characters initially
        cargarPersonajes(listaPersonajes);

        // 1. Configurar el listener de texto para filtrar en tiempo real (manteniendo el cambio)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPersonajes();
        });

        // 2. Configurar listeners para los CheckBoxes (manteniendo el cambio)
        configurarListenersFiltros();
    }

    private void inicializarDatosPrueba() {
        for (int i = 1; i <= 9; i++) {
            String casa = "Gryffindor";
            if (i % 4 == 0)
                casa = "Slytherin";
            else if (i % 4 == 2)
                casa = "Hufflepuff";
            else if (i % 4 == 3)
                casa = "Ravenclaw";

            // [MODIFICADO]: Pasamos un SLUG de prueba
            listaPersonajes.add(new Personaje("Personaje " + i, casa, "path/to/image.png", "personaje-" + i));
        }
        // Add a specific one for testing search
        listaPersonajes.add(new Personaje("Harry Potter", "Gryffindor", "path/to/image.png", "harry-potter"));
    }

    private void cargarPersonajes(List<Personaje> personajes) {
        tilePanePersonajes.getChildren().clear();
        for (Personaje p : personajes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();

                // [MODIFICADO] Pasamos el SLUG. Asumo que setData en el controlador de ficha
                // o un método similar puede manejar el slug.
                controller.setData(p.nombre, p.casa, p.imagePath);
                // [IMPORTANTE] Llamamos a setPersonajeSlug para asegurar que el slug llega al controlador
                controller.setPersonajeSlug(p.slug);

                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
            }
        }
    }

    /**
     * Configura un listener para la propiedad 'selected' de cada CheckBox
     * dentro del Accordion para aplicar el filtro inmediatamente.
     */
    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if ("Casa".equals(pane.getText())) {
                    javafx.scene.Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (javafx.scene.Node node : ((VBox) content).getChildren()) {
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
                javafx.scene.Node content = pane.getContent();
                if (content instanceof VBox) {
                    for (javafx.scene.Node node : ((VBox) content).getChildren()) {
                        if (node instanceof CheckBox) {
                            ((CheckBox) node).setSelected(false);
                        }
                    }
                }
            }
        }
        // Reload all characters after clearing filters
        cargarPersonajes(listaPersonajes);
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
                    javafx.scene.Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (javafx.scene.Node node : ((VBox) content).getChildren()) {
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

        List<Personaje> filtrados = listaPersonajes.stream()
                .filter(p -> p.nombre.toLowerCase().contains(searchText))
                .filter(p -> selectedCasas.isEmpty() || selectedCasas.contains(p.casa))
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: " + filtrados.size());
        cargarPersonajes(filtrados);
    }
}