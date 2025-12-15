package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class ControladorFichaPersonaje {

    @FXML private VBox cardBox;
    @FXML private ImageView imagePersonaje;
    @FXML private Label labelNombre, labelCasa;
    @FXML private CheckBox checkBoxSeleccionar;
    @FXML private ResourceBundle resources;

    private static final Logger logger = LoggerFactory.getLogger(ControladorFichaPersonaje.class);
    private String personajeSlug;
    private boolean isSelectionMode = false;
    private Runnable onSelectionChanged;

    // RUTA A LAS IMÁGENES
    private static final String RUTA_LOCAL_IMAGENES = "C:\\Users\\dm2\\Reto3_Hogwarts_Anuario\\imagenes\\";

    @FXML
    public void initialize() {
        if (this.resources == null) {
            try {
                this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {}
        }
    }

    public void setPersonajeSlug(String slug) { this.personajeSlug = slug; }

    public void setData(String nombre, String casa, String imagePath) {

        String nombreBonito = formatearTexto(nombre);
        labelNombre.setText(nombreBonito);
        labelCasa.setText(formatearTexto(casa));
        aplicarEstiloCasa(casa);

        try {
            boolean encontrado = false;

            // 1. INTENTO: Minúsculas con guiones (ej: "aamir-loonat") -> Prueba .png y .jpg
            if (nombre != null) {
                String base1 = nombre.toLowerCase().trim().replaceAll("\\s+", "-");
                if (intentarCargarVariasExtensiones(base1)) encontrado = true;
            }

            // 2. INTENTO: Mayúsculas con guiones (ej: "Aamir-Loonat") -> Prueba .png y .jpg
            if (!encontrado && nombreBonito != null) {
                String base2 = nombreBonito.replaceAll("\\s+", "-");
                if (intentarCargarVariasExtensiones(base2)) encontrado = true;
            }

            // 3. INTENTO: Nombre exacto con espacios (ej: "Harry Potter") -> Prueba .png y .jpg
            if (!encontrado && nombreBonito != null) {
                if (intentarCargarVariasExtensiones(nombreBonito)) encontrado = true;
            }

            // 4. INTENTO: Usar nombre base del CSV (ej: "foto123") -> Prueba .png y .jpg
            if (!encontrado && imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                String baseCSV = limpiaNombreArchivo(imagePath);
                if (intentarCargarVariasExtensiones(baseCSV)) encontrado = true;
            }

            // Si falla tras probar todas las combinaciones de nombre y extensiones
            if (!encontrado) {
                cargarImagenPorDefecto();
            }

        } catch (Exception e) {
            cargarImagenPorDefecto();
        }
    }

    /**
     * MÉTODO NUEVO: Prueba un nombre con .png, luego con .jpg, luego con .jpeg
     */
    private boolean intentarCargarVariasExtensiones(String nombreBase) {
        // 1. Probar PNG
        if (cargarImagenFuerzaBruta(nombreBase + ".png")) return true;
        // 2. Probar JPG
        if (cargarImagenFuerzaBruta(nombreBase + ".jpg")) return true;
        // 3. Probar JPEG
        if (cargarImagenFuerzaBruta(nombreBase + ".jpeg")) return true;

        return false;
    }

    /**
     * Carga el archivo usando FileInputStream (Fuerza Bruta para Windows)
     */
    private boolean cargarImagenFuerzaBruta(String nombreArchivo) {
        File archivo = new File(RUTA_LOCAL_IMAGENES + nombreArchivo);
        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                imagePersonaje.setImage(new Image(fis));
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private String formatearTexto(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder res = new StringBuilder();
        for (String p : palabras) if (!p.isEmpty()) res.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
        return res.toString().trim();
    }

    private String limpiaNombreArchivo(String ruta) {
        String nombre = ruta;
        if (nombre.contains("/")) nombre = nombre.substring(nombre.lastIndexOf("/") + 1);
        if (nombre.contains("?")) nombre = nombre.substring(0, nombre.indexOf("?"));
        if (nombre.contains(".")) nombre = nombre.substring(0, nombre.lastIndexOf("."));
        return nombre.replace("%20", " ");
    }

    private void aplicarEstiloCasa(String casa) {
        if (casa == null) return;
        String estilo = "-fx-font-weight: bold; -fx-text-fill: ";
        switch (casa.toLowerCase().trim()) {
            case "gryffindor" -> labelCasa.setStyle(estilo + "#740001;");
            case "slytherin" -> labelCasa.setStyle(estilo + "#1a472a;");
            case "ravenclaw" -> labelCasa.setStyle(estilo + "#0e1a40;");
            case "hufflepuff" -> labelCasa.setStyle(estilo + "#ecb939;");
            default -> labelCasa.setStyle(estilo + "#555555;");
        }
    }

    private void cargarImagenPorDefecto() {
        try {
            InputStream stream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
            if (stream != null) imagePersonaje.setImage(new Image(stream));
        } catch (Exception e) {}
    }

    @FXML private void handleCardClick() {
        if (isSelectionMode) checkBoxSeleccionar.setSelected(!checkBoxSeleccionar.isSelected());
        else abrirDetalles();
    }

    private void abrirDetalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/ventanaDatos.fxml"));
            if (resources != null) loader.setResources(resources);
            Parent root = loader.load();
            ControladorDatos cd = loader.getController();
            if (personajeSlug != null) cd.setPersonajeSlug(personajeSlug);
            Scene scene = new Scene(root);
            try {
                var css = getClass().getResource("/es/potersitos/css/estiloDatos.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception e) {}
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();
        } catch (IOException e) { logger.error("Error", e); }
    }

    public void setSelectionMode(boolean active) {
        isSelectionMode = active;
        if (checkBoxSeleccionar != null) {
            checkBoxSeleccionar.setVisible(active);
            if (!active) checkBoxSeleccionar.setSelected(false);
        }
    }

    public void setOnSelectionChanged(Runnable listener) {
        this.onSelectionChanged = listener;
        if (checkBoxSeleccionar != null) checkBoxSeleccionar.selectedProperty().addListener((o, ov, nv) -> { if (onSelectionChanged != null) onSelectionChanged.run(); });
    }
    public boolean isSelected() { return checkBoxSeleccionar != null && checkBoxSeleccionar.isSelected(); }
    public String getPersonajeSlug() { return personajeSlug; }
}