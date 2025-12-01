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

        public Personaje(String nombre, String casa, String imagePath) {
            this.nombre = nombre;
            this.casa = casa;
            this.imagePath = imagePath;
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

        // Set up search field listener (Enter key)
        searchField.setOnAction(event -> filtrarPersonajes());
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

            listaPersonajes.add(new Personaje("Personaje " + i, casa, "path/to/image.png"));
        }
        // Add a specific one for testing search
        listaPersonajes.add(new Personaje("Harry Potter", "Gryffindor", "path/to/image.png"));
    }

    private void cargarPersonajes(List<Personaje> personajes) {
        tilePanePersonajes.getChildren().clear();
        for (Personaje p : personajes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();
                controller.setResources(resources);
                controller.setData(p.nombre, p.casa, p.imagePath);

                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
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

        cargarPersonajes(filtrados);
    }
}
