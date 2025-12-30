package es.potersitos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Clase principal de la aplicación JavaFX.
 * Esta clase extiende {@link Application} y se encarga de:
 * - Cargar la interfaz desde un archivo FXML.
 * - Aplicar la hoja de estilos CSS.
 * - Configurar el stage principal y mostrar la ventana.
 * - Registrar mensajes de log con SLF4J.
 * Contiene también el metodo {@link #main(String[])} para lanzar la aplicación.
 *
 * @author Erlantz
 * @version 1.0
 */
public class App extends Application {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /** Bundle del sistema de internacionalización */
    private ResourceBundle resources;

    /**
     * Metodo principal que se ejecuta al iniciar la aplicación JavaFX.
     * Carga el FXML, aplica el CSS, configura el stage y muestra la ventana.
     * Si ocurre algún error, muestra una alerta y registra el error en el Log.
     *
     * @param stage Stage principal proporcionado por JavaFX.
     *
     * @author Erlantz
     */
    @Override
    public void start(Stage stage) {
        try {
            Locale locale = Locale.getDefault();

            resources = ResourceBundle.getBundle("es.potersitos.mensaje", locale);

            logger.debug("Cargando el archivo FXML: visualizarPersonajes.fxml");
            FXMLLoader loaded = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/visualizarPersonajes.fxml"), resources);

            Scene scene = new Scene(loaded.load());
            logger.info("FXML cargado correctamente");

            var archivoCSS = getClass().getResource("/es/potersitos/css/estilo.css");
            if (archivoCSS != null) {
                logger.info("CSS cargado correctamente: {}", archivoCSS.toExternalForm());
                scene.getStylesheets().add(archivoCSS.toExternalForm());
            } else {
                logger.error("No se ha podido cargar el CSS");
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("cssNoEncontrado"), null, resources.getString("cssNoEncontradoMensaje"));
            }

            stage.setTitle("Anuario de Hogwarts");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.getIcons().add(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png")))
            );

            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setMaxWidth(1300);
            stage.setMaxHeight(700);
            stage.show();

        } catch (Exception e) {
            logger.error("Error al intentar cargar la aplicación {}", e.getMessage());
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), resources.getString("errorIniciarAplicacion"), resources.getString("errorIniciarAplicacionMensaje"));
        }
    }

    /**
     * Metodo que se ejecuta cuando cierra la aplicación.
     * Registra un mensaje de cierre en el archivo de Log.
     *
     * @author Erlantz
     */
    @Override
    public void stop() {
        logger.info("Aplicación finalizada correctamente");
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

        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png")))
        );

        alerta.showAndWait();
        logger.debug("Alerta mostrada: tipo={}, mensaje={}", tipo, mensaje);
    }

    /**
     * Metodo principal que lanza la aplicación JavaFX
     *
     * @param args Argumentos de línea de comandos (no usados).
     *
     * @author Erlantz
     */
    public static void main(String[] args) {
        logger.info("Iniciando aplicación JavaFX...");
        launch();
    }

}