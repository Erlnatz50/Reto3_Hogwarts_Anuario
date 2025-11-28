package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorVisualizarPersonajes implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    @FXML
    public TextField searchField;

    @FXML
    private TilePane tilePanePersonajes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando ControladorVisualizarPersonajes...");
        cargarPersonajes();
    }

    private void cargarPersonajes() {
        // Simulating loading 9 characters for the 3x3 grid
        for (int i = 1; i <= 9; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();
                controller.setData("Personaje " + i, "Casa " + (i % 4 + 1), "path/to/image.png");

                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
            }
        }
    }
}