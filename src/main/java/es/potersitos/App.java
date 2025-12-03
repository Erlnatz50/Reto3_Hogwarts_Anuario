package es.potersitos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicaci\u00f3n JavaFX.
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage stage) {
        String APP_TITLE = "Gesti\u00f3n de Usuarios Hogwarts Anuario";
        String ERROR_CSS_CONTENT = "No se ha podido cargar la hoja de estilos CSS";
        String ERROR_INIT_HEADER = "Error al iniciar la aplicaci\u00f3n";
        String ERROR_INIT_CONTENT = "Se ha producido un error al intentar cargar la aplicaci\u00f3n";

        try{
            // CORRECCIÓN FINAL: Carga el FXML principal que sí existe: ventana.fxml
            logger.debug("Cargando el archivo FXML: ventana.fxml");
            FXMLLoader loaded = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventana.fxml"));

            Scene scene = new Scene(loaded.load());
            logger.info("FXML cargado correctamente");

            var archivoCSS = getClass().getResource("/es/potersitos/css/estilo.css");
            if(archivoCSS != null){
                logger.info("CSS cargado correctamente");
                scene.getStylesheets().add(archivoCSS.toExternalForm());
            } else{
                logger.error("No se ha podido cargar el CSS");
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("CSS no encontrado");
                alerta.setHeaderText(null);
                alerta.setContentText(ERROR_CSS_CONTENT);
                alerta.showAndWait();
            }

            stage.setTitle(APP_TITLE);
            stage.setScene(scene);
            stage.setResizable(true);

            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.setMaxWidth(1200);
            stage.setMaxHeight(900);
            stage.show();

        } catch (Exception e) {
            logger.error("Error al intentar cargar la aplicaci\u00f3n: {}", e.getMessage(), e);
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(ERROR_INIT_HEADER);
            alerta.setContentText(ERROR_INIT_CONTENT);
            alerta.showAndWait();
        }
    }

    @Override
    public void stop(){
        logger.info("Aplicaci\u00f3n finalizada correctamente");
    }
}