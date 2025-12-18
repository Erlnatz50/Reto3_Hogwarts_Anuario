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
import java.util.List;
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

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorNuevoPersonaje.class);

    /** Campos del formulario FXML */
    @FXML
    public TextField idField, typeField, slugField, aliasNamesField, animagusField, bloodStatusField,
            boggartField, bornField, diedField, eyeColorField, trabajoField, miembrosFamiliaField, colorPielField,
            varitaField, genderField, hairColorField, heightField, houseField, imageField, maritalStatusField,
            nameField, nationalityField, patronusField, speciesField, wikipediaField, romancesField, titulosField,
            pesoField;

    /** Botones del formulario FXML */
    @FXML
    public Button cancelarButton, agregarButton;

    /** Bundle del sistema de internacionalización */
    private ResourceBundle resources;

    private boolean editMode = false;

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
     * Válida y guarda nuevos personajes en CSV, XML y binario.
     *
     * @author Erlantz
     */
    @FXML
    public void onAgregar() {
        try {
            // Recoger datos en una estrucutra Map para el CSVManager (actualización)
            Map<String, String> mapaDatos = new HashMap<>();

            mapaDatos.put("id", idField.getText().trim());
            mapaDatos.put("type", typeField.getText().trim());
            mapaDatos.put("slug", slugField.getText().trim());
            mapaDatos.put("alias_names", aliasNamesField.getText().trim());
            mapaDatos.put("animagus", animagusField.getText().trim());
            mapaDatos.put("blood_status", bloodStatusField.getText().trim());
            mapaDatos.put("boggart", boggartField.getText().trim());
            mapaDatos.put("born", bornField.getText().trim());
            mapaDatos.put("died", diedField.getText().trim());
            mapaDatos.put("eye_color", eyeColorField.getText().trim());
            mapaDatos.put("jobs", trabajoField.getText().trim()); // OJO: jobs vs trabajoField
            mapaDatos.put("family_members", miembrosFamiliaField.getText().trim());
            mapaDatos.put("skin_color", colorPielField.getText().trim());
            mapaDatos.put("wands", varitaField.getText().trim());
            mapaDatos.put("gender", genderField.getText().trim());
            mapaDatos.put("hair_color", hairColorField.getText().trim());
            mapaDatos.put("height", heightField.getText().trim());
            mapaDatos.put("house", houseField.getText().trim());
            mapaDatos.put("image", imageField.getText().trim());
            mapaDatos.put("marital_status", maritalStatusField.getText().trim());
            mapaDatos.put("name", nameField.getText().trim());
            mapaDatos.put("nationality", nationalityField.getText().trim());
            mapaDatos.put("patronus", patronusField.getText().trim());
            mapaDatos.put("species", speciesField.getText().trim());
            mapaDatos.put("wiki", wikipediaField.getText().trim());
            mapaDatos.put("romances", romancesField.getText().trim());
            mapaDatos.put("titles", titulosField.getText().trim());
            mapaDatos.put("weight", pesoField.getText().trim());

            // Array para los métodos legacy (guardarXML, guardarBinario, y guardarCSV
            // antiguo)
            // Se mantiene el orden EXACTO que tenías antes
            String[] datos = {
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
                    mapaDatos.get("jobs"),
                    mapaDatos.get("family_members"),
                    mapaDatos.get("skin_color"),
                    mapaDatos.get("wands"),
                    mapaDatos.get("gender"),
                    mapaDatos.get("hair_color"),
                    mapaDatos.get("height"),
                    mapaDatos.get("house"),
                    mapaDatos.get("image"),
                    mapaDatos.get("marital_status"),
                    mapaDatos.get("name"),
                    mapaDatos.get("nationality"),
                    mapaDatos.get("patronus"),
                    mapaDatos.get("species"),
                    mapaDatos.get("wiki"),
                    mapaDatos.get("romances"),
                    mapaDatos.get("titles"),
                    mapaDatos.get("weight")
            };

            Path baseDir = Paths.get(System.getProperty("user.home"), "Reto3_Hogwarts_Anuario");
            Files.createDirectories(baseDir);

            if (editMode) {
                // Modo Edición: Usamos el Manager para actualizar el CSV
                boolean exito = PersonajeCSVManager.actualizarPersonaje(mapaDatos);
                if (exito) {
                    // TODO: Actualizar también XML y Binario si fuera necesario.
                    // Para este requisito, priorizamos CSV.
                    logger.info("Personaje actualizado en CSV: {}", mapaDatos.get("name"));
                    mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"),
                            "Personaje Actualizado", "El personaje ha sido modificado correctamente en el CSV.");
                } else {
                    throw new Exception("No se pudo actualizar el CSV (quizás el slug no coincide).");
                }
            } else {
                // Modo Creación: Comportamiento original (append)
                guardarCSV(baseDir, datos);
                guardarXML(baseDir, datos);
                guardarBinario(baseDir, datos);

                logger.info("Personaje creado correctamente: {}", datos[20]); // datos[20] era name en tu array
                                                                              // original... espera
                // Re-verificando índices del array original: 20 -> patronus??
                // En tu código original: 20 -> nameField (línea 117 de array declaration era
                // index 20 en la lista visual, pero en java array es index 20)
                // Espera, el array original línea 96 tiene 28 elementos.
                // index 20 es "name"?
                // 0:id, 1:type, 2:slug... 18:name, 19:nationality, 20:patronus.
                // En el codigo original:
                // 117: nameField.getText().trim(),
                // nombre es el index 20 en la lista de definicion de arriba:
                // idField (0), type (1), slug (2), alias (3), animagus (4), blood (5), boggart
                // (6), born (7), died (8), eye (9), trabajo (10), miembros (11), colorPiel
                // (12), varita (13), gender (14), hair (15), height (16), house (17), image
                // (18), marital (19), name (20).
                // Sí, name está en posición 20. Correcto.

                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"),
                        resources.getString("personajeGuardado"), resources.getString("personajeGuardadoMensaje"));
            }

            cancelarButton.getScene().getWindow().hide();

        } catch (Exception e) {
            logger.error("Error al guardar el personaje", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"),
                    resources.getString("falloAlGuardarPersonaje"), e.getMessage());
        }
    }

    public void setDatosPersonaje(Map<String, String> datos) {
        this.editMode = true;
        if (agregarButton != null) {
            agregarButton.setText(resources != null ? resources.getString("menu.archivo.guardar") : "Guardar");
        }

        idField.setText(datos.getOrDefault("id", ""));
        typeField.setText(datos.getOrDefault("type", ""));
        slugField.setText(datos.getOrDefault("slug", ""));
        // El slug no debería editarse si es la clave, pero lo dejamos editable si el
        // usuario sabe lo que hace,
        // OJO: Si cambia el slug, actualizarPersonaje fallará porque busca por slug
        // viejo.
        // Lo ideal sería deshabilitar el slug o guardarlo aparte.
        slugField.setEditable(false); // Recomendación: bloquear slug en edición

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
     * Guardar personaje en formato CSV (append).
     *
     * @param baseDir Directorio base
     * @param datos   Array de datos del personaje
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
            throw new RuntimeException("Error CSV: " + e.getMessage());
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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;

            if (Files.exists(xmlPath)) {
                try {
                    doc = builder.parse(xmlPath.toFile());
                    doc.getDocumentElement().normalize();
                } catch (Exception ex) {
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
            transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "4");
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
     * @param datos   Array de datos del personaje
     * @author Erlantz
     */
    private void guardarBinario(Path baseDir, String[] datos) {
        try {
            Path binPath = baseDir.resolve("todosPersonajes.bin");
            List<String[]> personajes = new ArrayList<>();

            if (Files.exists(binPath)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(binPath))) {
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
        if (!Files.exists(csvPath))
            return lista;

        try (var reader = Files.newBufferedReader(csvPath)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lista.add(linea.split(",", -1));
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