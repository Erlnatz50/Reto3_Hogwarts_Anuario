package es.potersitos.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorVisualizarPersonajes implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    public MenuItem nuevoMenuItem;

    @FXML
    private TilePane tilePanePersonajes;

    @FXML
    private VBox filterPanel;

    @FXML
    private Accordion accordionFiltros;

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        logger.info("Inicializando ControladorVisualizarPersonajes...");
        cargarPersonajes();
    }

    private void cargarPersonajes() {
        // Simulating loading 9 characters for the 3x3 grid
        for (int i = 1; i <= 9; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                VBox card = loader.load();

                //ControladorFichaPersonaje controller = loader.getController();
                //controller.setResources(resources);
                //controller.setData("Personaje " + i, "Casa " + (i % 4 + 1), "path/to/image.png");

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
            for (javafx.scene.control.TitledPane pane : accordionFiltros.getPanes()) {
                javafx.scene.Node content = pane.getContent();
                if (content instanceof VBox) {
                    for (javafx.scene.Node node : ((VBox) content).getChildren()) {
                        if (node instanceof javafx.scene.control.CheckBox) {
                            ((javafx.scene.control.CheckBox) node).setSelected(false);
                        }
                    }
                }
            }
        }
    }

    public void onNuevo(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nuevo Personaje");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
