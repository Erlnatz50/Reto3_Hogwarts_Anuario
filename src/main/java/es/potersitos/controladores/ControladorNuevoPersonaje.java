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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

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

    /** Slug del personaje después de ser guardado (útil si cambió) */
    private String slugActualizado;

    @FXML
    private ImageView imageView;

    /** Ruta local donde se buscan imágenes de personajes */
    private static final String RUTA_LOCAL_IMAGENES = System.getProperty("user.home") + File.separator
            + "Reto3_Hogwarts_Anuario" + File.separator + "imagenes" + File.separator;

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

        // Listener para previsualizar la imagen cuando el usuario escribe la URL
        imageField.textProperty()
                .addListener((observable, oldValue, newValue) -> actualizarPrevisualizacionImagen(newValue));

        logger.info("ControladorNuevoPersonaje inicializado. Idioma detectado: {}",
                resources != null ? resources.getLocale() : "Desconocido");
    }

    /**
     * Actualiza la previsualización de la imagen basada en el texto del campo
     * imageField.
     * 
     * @param urlImagen URL o ruta de la imagen
     */
    private void actualizarPrevisualizacionImagen(String urlImagen) {
        if (imageView == null)
            return;

        if (urlImagen == null || urlImagen.isBlank()) {
            cargarImagenPorDefecto();
            return;
        }

        try {
            // 1. Intenta cargar como URL remota
            if (urlImagen.startsWith("http")) {
                imageView.setImage(new Image(urlImagen, true));
                return;
            }

            // 2. Intenta cargar como ruta absoluta local
            File archivo = new File(urlImagen);
            if (archivo.isAbsolute() && archivo.exists()) {
                imageView.setImage(new Image(archivo.toURI().toString()));
                return;
            }

            // 3. Intenta buscar en la carpeta de imagenes local
            File pathLocal = new File(RUTA_LOCAL_IMAGENES, urlImagen);
            if (pathLocal.exists()) {
                imageView.setImage(new Image(pathLocal.toURI().toString()));
                return;
            }

            // 4. Si no se encuentra, intentar con el slug (si está disponible)
            String slug = slugField.getText().trim();
            if (!slug.isEmpty()) {
                String[] extensiones = { ".jpg", ".png", ".jpeg", ".JPG", ".PNG", ".JPEG" };
                for (String ext : extensiones) {
                    File pathSlug = new File(RUTA_LOCAL_IMAGENES, slug + ext);
                    if (pathSlug.exists()) {
                        imageView.setImage(new Image(pathSlug.toURI().toString()));
                        return;
                    }
                }
            }

            cargarImagenPorDefecto();
        } catch (Exception e) {
            logger.warn("Error en previsualización de imagen: {}", urlImagen);
            cargarImagenPorDefecto();
        }
    }

    private void cargarImagenPorDefecto() {
        try {
            imageView.setImage(new Image(Objects
                    .requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png"))));
        } catch (Exception e) {
            logger.warn("No se pudo cargar la imagen por defecto");
        }
    }

    /**
     * Establece un callback que se ejecutará cuando un personaje
     * haya sido guardado correctamente (ya sea creado o actualizado).
     *
     * @param callback callback un objeto {@link Runnable} que se ejecutará tras
     *                 guardar el personaje.
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
            if (!editMode) {
                idField.setText(UUID.randomUUID().toString());

                String nombre = nameField.getText().trim();
                if (nombre.isEmpty()) {
                    mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"),
                            resources.getString("alerta.titulo"), resources.getString("alerta.mensaje"));
                    return;
                } else {
                    List<String> slugsExistentes = cargarSlugsExistentes();
                    String slug = generarSlugUnico(nameField.getText().trim(), slugsExistentes);
                    slugField.setText(slug);
                }
            }

            String urlImagen = imageField.getText().trim();
            String nombreImagenLocal = "";

            if (!urlImagen.isEmpty()) {
                if (urlImagen.startsWith("http")) {
                    nombreImagenLocal = descargarImagen(urlImagen, slugField.getText().trim());
                } else {
                    nombreImagenLocal = urlImagen;
                }
            }

            Map<String, String> mapaDatos = construirMapaDatos();

            if (!nombreImagenLocal.isEmpty()) {
                mapaDatos.put("image", nombreImagenLocal);
            }

            Path baseDir = Paths.get(System.getProperty("user.home"), "Reto3_Hogwarts_Anuario");
            Files.createDirectories(baseDir);

            if (editMode) {
                boolean exito = PersonajeCSVManager.actualizarPersonaje(mapaDatos);
                if (exito) {
                    slugActualizado = mapaDatos.get("slug");
                    mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"),
                            resources.getString("menu.archivo.guardar"), resources.getString("personajeActualizado"));
                } else {
                    logger.warn("No se pudo actualizar el personaje en el CSV");
                    mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"),
                            resources.getString("falloAlGuardarPersonaje"),
                            "No se pudo encontrar el personaje para actualizar.");
                    return; // No cerrar la ventana si falló
                }
            } else {
                String[] datos = construirArrayLegacy(mapaDatos);
                guardarCSV(baseDir, datos);
                guardarXML(baseDir, datos);
                guardarBinario(baseDir, datos);

                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"),
                        resources.getString("personajeGuardado"), resources.getString("personajeGuardadoMensaje"));
            }

            cancelarButton.getScene().getWindow().hide();

            if (onPersonajeGuardado != null) {
                onPersonajeGuardado.run();
            }

        } catch (Exception e) {
            logger.error("Error al guardar el personaje", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"),
                    resources.getString("falloAlGuardarPersonaje"), e.getMessage());
        }
    }

    /**
     * Genera un slug único a partir del nombre del personaje.
     *
     * @param nombre          Nombre del personaje
     * @param slugsExistentes Lista de slugs ya existentes
     * @return Slug único generado
     * @author Erlantz
     */
    private String generarSlugUnico(String nombre, List<String> slugsExistentes) {
        String baseSlug = nombre.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        String slug = baseSlug;
        int contador = 1;
        while (slugsExistentes.contains(slug)) {
            slug = baseSlug + "-" + contador;
            contador++;
        }
        return slug;
    }

    /**
     * Carga los slugs de los personajes ya existentes desde el archivo CSV.
     *
     * @return Lista de slugs existentes
     * @author Erlantz
     */
    private List<String> cargarSlugsExistentes() {
        List<String> slugs = new ArrayList<>();
        Path csvPath = Paths.get(System.getProperty("user.home"), "Reto3_Hogwarts_Anuario", "todosPersonajes.csv");
        if (Files.exists(csvPath)) {
            try {
                List<String> lineas = Files.readAllLines(csvPath);
                for (String linea : lineas) {
                    String[] campos = linea.split(",");
                    if (campos.length > 2) {
                        slugs.add(campos[2]);
                    }
                }
            } catch (IOException e) {
                logger.warn("No se pudieron cargar los slugs existentes", e);
            }
        }
        return slugs;
    }

    /**
     * Descarga una imagen desde una URL y la guarda localmente asociada a un
     * personaje.
     *
     * @param urlImagen URL de la imagen a descargar
     * @param slug      Slug del personaje, usado como nombre del archivo
     * @author Erlantz
     */
    private String descargarImagen(String urlImagen, String slug) {
        if (urlImagen == null || urlImagen.isEmpty())
            return "";

        String nombreArchivo = slug + ".jpg";
        File destino = new File(RUTA_LOCAL_IMAGENES, nombreArchivo);

        try {
            if (!destino.getParentFile().exists()) {
                destino.getParentFile().mkdirs();
            }
            try (InputStream in = new URL(urlImagen).openStream()) {
                Files.copy(in, destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Imagen descargada/actualizada en: {}", destino.getAbsolutePath());
            return nombreArchivo;
        } catch (Exception e) {
            logger.error("No se pudo descargar/actualizar la imagen desde URL: {}", urlImagen, e);
            return "";
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
        actualizarPrevisualizacionImagen(imageField.getText());
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
        return new String[] {
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

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(binPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
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

        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));

        alerta.showAndWait();
        logger.debug("Alerta mostrada: {}", titulo);
    }

    /**
     * @return El slug del personaje después de la operación (ej. tras
     *         actualizarse).
     */
    public String getSlugActualizado() {
        return slugActualizado;
    }
}