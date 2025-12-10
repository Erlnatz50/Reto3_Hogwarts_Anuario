package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador FXML para crear nuevos personajes de Hogwarts Anuario.
 * Guarda datos en tres formatos: CSV, XML y binario.
 * Soporta internacionalización para los mensajes de alerta.
 *
 * @author Erlantz
 * @version 1.0
 */
public class ControladorNuevoPersonaje {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorNuevoPersonaje.class);

    /** Campos del formulario FXML */
    @FXML
    public TextField idField, typeField, slugField, aliasNamesField, animagusField, bloodStatusField,
            boggartField, bornField, diedField, eyeColorField, trabajoField, miembrosFamiliaField, colorPielField,
            varitaField, genderField, hairColorField, heightField, houseField, imageField, maritalStatusField,
            nameField, nationalityField, patronusField, speciesField, wikipediaField, romancesField, titulosField, pesoField;

    /** Botones del formulario FXML */
    @FXML
    public Button cancelarButton, agregarButton;

    /** Bundle del sistema de internacionalización */
    private ResourceBundle resources;

    /**
     * Inicializa el controlador.
     * JavaFX inyecta automáticamente el ResourceBundle si se pasó al FXMLLoader.
     */
    @FXML
    private void initialize() {
        // Si el loader no inyectó los recursos (por ejemplo, al abrirse de forma aislada), cargamos el defecto.
        if (resources == null) {
            try {
                resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("No se pudo cargar el ResourceBundle por defecto.", e);
            }
        }

        logger.info("ControladorNuevoPersonaje inicializado. Idioma detectado: {}",
                resources != null ? resources.getLocale() : "Desconocido");
    }

    /**
     * Cierra la ventana del formulario.
     *
     * @author Erlantz
     */
    @FXML
    public void onCancelar() {
        logger.info("Usuario canceló la creación de un nuevo personaje");
        cancelarButton.getScene().getWindow().hide();
    }

    /**
     * Valida y guarda nuevos personajes en CSV, XML y binario.
     *
     * @author Erlantz
     */
    @FXML
    public void onAgregar() {
        try {
            // Recogemos los datos (sin validación compleja por ahora, tal como estaba en el original)
            String[] datos = {
                    idField.getText().trim(),
                    typeField.getText().trim(),
                    slugField.getText().trim(),
                    aliasNamesField.getText().trim(),
                    animagusField.getText().trim(),
                    bloodStatusField.getText().trim(),
                    boggartField.getText().trim(),
                    bornField.getText().trim(),
                    diedField.getText().trim(),
                    eyeColorField.getText().trim(),
                    trabajoField.getText().trim(),
                    miembrosFamiliaField.getText().trim(),
                    colorPielField.getText().trim(),
                    varitaField.getText().trim(),
                    genderField.getText().trim(),
                    hairColorField.getText().trim(),
                    heightField.getText().trim(),
                    houseField.getText().trim(),
                    imageField.getText().trim(),
                    maritalStatusField.getText().trim(),
                    nameField.getText().trim(),
                    nationalityField.getText().trim(),
                    patronusField.getText().trim(),
                    speciesField.getText().trim(),
                    wikipediaField.getText().trim(),
                    romancesField.getText().trim(),
                    titulosField.getText().trim(),
                    pesoField.getText().trim()
            };

            // Definimos la ruta de guardado
            Path baseDir = Paths.get(System.getProperty("user.home"), "Reto3_Hogwarts_Anuario");
            Files.createDirectories(baseDir);
            logger.debug("Directorio verificado/creado: {}", baseDir);

            // Guardamos en los 3 formatos
            guardarCSV(baseDir, datos);
            guardarXML(baseDir, datos);
            guardarBinario(baseDir, datos);

            logger.info("Personaje creado correctamente: {}", datos[20]); // datos[20] es el nombre

            // Usamos las claves del ResourceBundle para los mensajes
            mandarAlertas(Alert.AlertType.INFORMATION,
                    resources.getString("exito"),
                    resources.getString("personajeGuardado"),
                    resources.getString("personajeGuardadoMensaje"));

            cancelarButton.getScene().getWindow().hide();

        } catch (Exception e) {
            logger.error("Error al guardar el personaje", e);
            mandarAlertas(Alert.AlertType.ERROR,
                    resources.getString("error"),
                    resources.getString("falloAlGuardarPersonaje"),
                    e.getMessage());
        }
    }

    /**
     * Guardar personaje en formato CSV (append).
     *
     * @param baseDir Directorio base
     * @param datos Array de datos del personaje
     * @author Erlantz
     */
    private void guardarCSV(Path baseDir, String[] datos) {
        try {
            Path csvPath = baseDir.resolve("todosPersonajes.csv");
            String lineaCSV = String.join(",", datos) + "\n";
            Files.write(csvPath, lineaCSV.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.debug("CSV guardado: {}", csvPath);
        } catch (IOException e) {
            logger.error("Error al guardar en archivo CSV: {}", e.getMessage());
            throw new RuntimeException("Error CSV: " + e.getMessage()); // Re-lanzamos para que salte la alerta
        }
    }

    /**
     * Guarda personaje en formato XML (DOM Parser).
     *
     * @param baseDir Directorio base
     * @param datos Array de datos del personaje
     * @author Erlantz
     */
    private void guardarXML(Path baseDir, String[] datos) {
        try {
            Path xmlPath = baseDir.resolve("todosPersonajes.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;

            if (Files.exists(xmlPath)) {
                try {
                    doc = builder.parse(xmlPath.toFile());
                    doc.getDocumentElement().normalize();
                } catch (Exception ex) {
                    // Si el XML está corrupto o vacío, creamos uno nuevo
                    doc = builder.newDocument();
                    Element root = doc.createElement("characters");
                    doc.appendChild(root);
                }
            } else {
                doc = builder.newDocument();
                Element root = doc.createElement("characters");
                doc.appendChild(root);
            }

            Element root = doc.getDocumentElement();
            String[] nombresCampos = {
                    "id", "type", "slug", "alias_names", "animagus", "blood_status",
                    "boggart", "born", "died", "eye_color", "family_members", "gender",
                    "hair_color", "height", "house", "image", "jobs", "marital_status",
                    "name", "nationality", "patronus", "romances", "skin_color",
                    "species", "titles", "wands", "weight", "wiki"
            };

            Element character = doc.createElement("character");
            for (int i = 0; i < nombresCampos.length; i++) {
                if (i < datos.length) {
                    Element field = doc.createElement(nombresCampos[i]);
                    field.setTextContent(datos[i]);
                    character.appendChild(field);
                }
            }
            root.appendChild(character);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(xmlPath.toFile()));

            logger.debug("XML guardado: {}", xmlPath);
        } catch (Exception e) {
            logger.error("Error al guardar en archivo XML: {}", e.getMessage());
            throw new RuntimeException("Error XML: " + e.getMessage());
        }
    }

    /**
     * Guarda personaje en formato binario.
     *
     * @param baseDir Directorio base
     * @param datos Array de datos del personaje
     * @author Erlantz
     */
    private void guardarBinario(Path baseDir, String[] datos) {
        try {
            Path binPath = baseDir.resolve("todosPersonajes.bin");
            List<String[]> personajes = new ArrayList<>();

            if (Files.exists(binPath)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(binPath))) {
                    @SuppressWarnings("unchecked")
                    List<String[]> personajesLeidos = (List<String[]>) ois.readObject();
                    personajes = personajesLeidos != null ? personajesLeidos : new ArrayList<>();
                } catch (Exception e) {
                    logger.warn("Binario corrupto o versión anterior, intentando reconstruir desde CSV...");
                    personajes = reconstruirDesdeCSV(baseDir.resolve("todosPersonajes.csv"));
                }
            }
            personajes.add(datos);

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(binPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                oos.writeObject(personajes);
            }
            logger.debug("Binario guardado. Total personajes: {}", personajes.size());
        } catch (Exception e) {
            logger.error("Error al guardar en archivo binario: {}", e.getMessage());
            throw new RuntimeException("Error Binario: " + e.getMessage());
        }
    }

    /**
     * Reconstruye lista de personajes desde CSV si binario está corrupto.
     *
     * @param csvPath Ruta al archivo CSV
     * @return Lista de personajes reconstruida
     * @author Erlantz
     */
    private List<String[]> reconstruirDesdeCSV(Path csvPath) {
        List<String[]> lista = new ArrayList<>();
        if (!Files.exists(csvPath)) return lista;

        try (var reader = Files.newBufferedReader(csvPath)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lista.add(linea.split(",", -1)); // -1 para mantener columnas vacías
                }
            }
        } catch (Exception e) {
            logger.error("Error al reconstruir CSV: {}", e.getMessage());
        }
        return lista;
    }

    /**
     * Muestra una alerta JavaFX con los datos proporcionados.
     *
     * @param tipo          Tipo de alerta (INFO, WARNING, ERROR...)
     * @param titulo        Título de la alerta
     * @param mensajeTitulo Encabezado del mensaje
     * @param mensaje       Contenido del mensaje
     * @author Erlantz
     */
    private void mandarAlertas(Alert.AlertType tipo, String titulo, String mensajeTitulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(mensajeTitulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        logger.debug("Alerta mostrada: {}", titulo);
    }
}