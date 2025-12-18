package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import es.potersitos.util.PersonajeCSVManager;

/**
 * Controlador FXML para crear nuevos personajes de Hogwarts Anuario.
 * Guarda datos en tres formatos: CSV, XML y binario.
 * Soporta internacionalización para los mensajes de alerta.
 *
 * @author Erlantz
 * @version 1.0
 */
public class ControladorNuevoPersonaje {

    /** Campos del formulario FXML */
    @FXML
    private TextField idField, typeField, slugField, aliasNamesField, animagusField, bloodStatusField,
            boggartField, bornField, diedField, eyeColorField, trabajoField, miembrosFamiliaField, colorPielField,
            varitaField, genderField, hairColorField, heightField, houseField, imageField, maritalStatusField,
            nameField, nationalityField, patronusField, speciesField, wikipediaField, romancesField, titulosField,
            pesoField;

    /** Botones del formulario FXML */
    @FXML
    private Button cancelarButton, agregarButton;

    /** Bundle del sistema de internacionalización */
    private ResourceBundle resources;

    /** Indica si el formulario está en modo edición. */
    private boolean editMode = false;

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorNuevoPersonaje.class);

    /** Campo para el callback */
    private Runnable onPersonajeGuardado;

    /**
     * Inicializa el controlador.
     * JavaFX inyecta automáticamente el ResourceBundle si se pasó al FXMLLoader.
     */
    @FXML
    private void initialize() {
        if (resources == null) {
            try {
                resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("No se ha podido cargar el ResourceBundle por defecto", e);
            }
        }
        logger.info("ControladorNuevoPersonaje inicializado. Idioma detectado: {}", resources != null ? resources.getLocale() : "Desconocido");
    }

    /**
     * Establece un callback que se ejecutará cuando un personaje
     * haya sido guardado correctamente (ya sea creado o actualizado).
     *
     * @param callback callback un objeto {@link Runnable} que se ejecutará tras guardar el personaje.
     *                 Puede ser {@code null} si no se desea ninguna acción.
     * @author Erlantz
     */
    public void setOnPersonajeGuardado(Runnable callback) {
        onPersonajeGuardado = callback;
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
     * Válida y guarda nuevos personajes en CSV, XML y binario.
     *
     * @author Erlantz
     */
    @FXML
    public void onAgregar() {
        try {
            Map<String, String> mapaDatos = construirMapaDatos();

            Path baseDir = Paths.get(System.getProperty("user.home"), "Reto3_Hogwarts_Anuario");
            Files.createDirectories(baseDir);

            if (editMode) {
                boolean exito = PersonajeCSVManager.actualizarPersonaje(mapaDatos);
                if (!exito) {
                    logger.warn("No se pudo actualizar el personaje en el CSV");
                }
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), resources.getString("menu.archivo.guardar"), resources.getString("personajeActualizado"));
            } else {
                String[] datos = construirArrayLegacy(mapaDatos);
                guardarCSV(baseDir, datos);
                guardarXML(baseDir, datos);
                guardarBinario(baseDir, datos);

                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), resources.getString("personajeGuardado"), resources.getString("personajeGuardadoMensaje"));
            }

            cancelarButton.getScene().getWindow().hide();

            if (onPersonajeGuardado != null) {
                onPersonajeGuardado.run();
            }

        } catch (Exception e) {
            logger.error("Error al guardar el personaje", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), resources.getString("falloAlGuardarPersonaje"), e.getMessage());
        }
    }

    /**
     * Configura el formulario en modo edición y carga los datos del personaje.
     *
     * @param datos mapa con los datos del personaje
     * @author Erlantz
     */
    public void setDatosPersonaje(Map<String, String> datos) {
        this.editMode = true;
        if (agregarButton != null) {
            agregarButton.setText(resources.getString("menu.archivo.guardar"));
        }

        idField.setText(datos.getOrDefault("id", ""));
        typeField.setText(datos.getOrDefault("type", ""));
        slugField.setText(datos.getOrDefault("slug", ""));
        slugField.setEditable(false);

        aliasNamesField.setText(datos.getOrDefault("alias_names", ""));
        animagusField.setText(datos.getOrDefault("animagus", ""));
        bloodStatusField.setText(datos.getOrDefault("blood_status", ""));
        boggartField.setText(datos.getOrDefault("boggart", ""));
        bornField.setText(datos.getOrDefault("born", ""));
        diedField.setText(datos.getOrDefault("died", ""));
        eyeColorField.setText(datos.getOrDefault("eye_color", ""));
        trabajoField.setText(datos.getOrDefault("jobs", ""));
        miembrosFamiliaField.setText(datos.getOrDefault("family_members", ""));
        colorPielField.setText(datos.getOrDefault("skin_color", ""));
        varitaField.setText(datos.getOrDefault("wands", ""));
        genderField.setText(datos.getOrDefault("gender", ""));
        hairColorField.setText(datos.getOrDefault("hair_color", ""));
        heightField.setText(datos.getOrDefault("height", ""));
        houseField.setText(datos.getOrDefault("house", ""));
        imageField.setText(datos.getOrDefault("image", ""));
        maritalStatusField.setText(datos.getOrDefault("marital_status", ""));
        nameField.setText(datos.getOrDefault("name", ""));
        nationalityField.setText(datos.getOrDefault("nationality", ""));
        patronusField.setText(datos.getOrDefault("patronus", ""));
        speciesField.setText(datos.getOrDefault("species", ""));
        wikipediaField.setText(datos.getOrDefault("wiki", ""));
        romancesField.setText(datos.getOrDefault("romances", ""));
        titulosField.setText(datos.getOrDefault("titles", ""));
        pesoField.setText(datos.getOrDefault("weight", ""));
    }

    /**
     * Construye un mapa con los datos introducidos en el formulario.
     *
     * @return mapa clave-valor con los datos del personaje
     * @author Erlantz
     */
    private Map<String, String> construirMapaDatos() {
        Map<String, String> m = new HashMap<>();
        m.put("id", idField.getText().trim());
        m.put("type", typeField.getText().trim());
        m.put("slug", slugField.getText().trim());
        m.put("alias_names", aliasNamesField.getText().trim());
        m.put("animagus", animagusField.getText().trim());
        m.put("blood_status", bloodStatusField.getText().trim());
        m.put("boggart", boggartField.getText().trim());
        m.put("born", bornField.getText().trim());
        m.put("died", diedField.getText().trim());
        m.put("eye_color", eyeColorField.getText().trim());
        m.put("jobs", trabajoField.getText().trim());
        m.put("family_members", miembrosFamiliaField.getText().trim());
        m.put("skin_color", colorPielField.getText().trim());
        m.put("wands", varitaField.getText().trim());
        m.put("gender", genderField.getText().trim());
        m.put("hair_color", hairColorField.getText().trim());
        m.put("height", heightField.getText().trim());
        m.put("house", houseField.getText().trim());
        m.put("image", imageField.getText().trim());
        m.put("marital_status", maritalStatusField.getText().trim());
        m.put("name", nameField.getText().trim());
        m.put("nationality", nationalityField.getText().trim());
        m.put("patronus", patronusField.getText().trim());
        m.put("species", speciesField.getText().trim());
        m.put("wiki", wikipediaField.getText().trim());
        m.put("romances", romancesField.getText().trim());
        m.put("titles", titulosField.getText().trim());
        m.put("weight", pesoField.getText().trim());
        return m;
    }

    /**
     * Construye el array de datos en el orden legacy requerido.
     *
     * @param mapaDatos mapa con los datos del personaje
     * @return array de datos ordenado
     * @author Erlantz
     */
    private String[] construirArrayLegacy(Map<String, String> mapaDatos) {
        return new String[]{
                mapaDatos.get("id"),
                mapaDatos.get("type"),
                mapaDatos.get("slug"),
                mapaDatos.get("alias_names"),
                mapaDatos.get("animagus"),
                mapaDatos.get("blood_status"),
                mapaDatos.get("boggart"),
                mapaDatos.get("born"),
                mapaDatos.get("died"),
                mapaDatos.get("eye_color"),
                mapaDatos.get("family_members"),
                mapaDatos.get("gender"),
                mapaDatos.get("hair_color"),
                mapaDatos.get("height"),
                mapaDatos.get("house"),
                mapaDatos.get("image"),
                mapaDatos.get("jobs"),
                mapaDatos.get("marital_status"),
                mapaDatos.get("name"),
                mapaDatos.get("nationality"),
                mapaDatos.get("patronus"),
                mapaDatos.get("romances"),
                mapaDatos.get("skin_color"),
                mapaDatos.get("species"),
                mapaDatos.get("titles"),
                mapaDatos.get("wands"),
                mapaDatos.get("weight"),
                mapaDatos.get("wiki")
        };
    }

    /**
     * Guardar personaje en formato CSV (append).
     *
     * @param baseDir Directorio base
     * @param datos   Array de datos del personaje
     * @author Erlantz
     */
    private void guardarCSV(Path baseDir, String[] datos) {
        try {
            Path csvPath = baseDir.resolve("todosPersonajes.csv");
            Files.write(csvPath, (String.join(",", datos) + "\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Error al guardar CSV", e);
        }
    }

    /**
     * Guarda personaje en formato XML (DOM Parser).
     *
     * @param baseDir Directorio base
     * @param datos   Array de datos del personaje
     * @author Erlantz
     */
    private void guardarXML(Path baseDir, String[] datos) {
        try {
            Path xmlPath = baseDir.resolve("todosPersonajes.xml");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc;

            if (Files.exists(xmlPath)) {
                doc = builder.parse(xmlPath.toFile());
                doc.getDocumentElement().normalize();
            } else {
                doc = builder.newDocument();
                Element root = doc.createElement("characters");
                doc.appendChild(root);
            }

            Element root = doc.getDocumentElement();
            Element character = doc.createElement("character");

            for (int i = 0; i < datos.length; i++) {
                Element field = doc.createElement("field" + i);
                field.setTextContent(datos[i]);
                character.appendChild(field);
            }
            root.appendChild(character);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(xmlPath.toFile()));

        } catch (Exception e) {
            logger.error("Error al guardar XML", e);
        }
    }

    /**
     * Guarda personaje en formato binario.
     *
     * @param baseDir Directorio base
     * @param datos   Array de datos del personaje
     * @author Erlantz
     */
    private void guardarBinario(Path baseDir, String[] datos) {
        try {
            Path binPath = baseDir.resolve("todosPersonajes.bin");
            List<String[]> personajes = new ArrayList<>();

            if (Files.exists(binPath)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(binPath))) {
                    Object obj = ois.readObject();

                    if (obj instanceof List<?> lista) {
                        for (Object elemento : lista) {
                            if (elemento instanceof String[] arr) {
                                personajes.add(arr);
                            }
                        }
                    } else {
                        logger.warn("El archivo binario no contiene una lista válida");
                    }

                } catch (Exception e) {
                    logger.warn("Error al leer binario, se reinicia el archivo", e);
                }
            }

            personajes.add(datos);

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(binPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                oos.writeObject(personajes);
            }

        } catch (Exception e) {
            logger.error("Error al guardar binario", e);
        }
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