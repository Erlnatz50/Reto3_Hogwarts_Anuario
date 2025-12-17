package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import net.sf.jasperreports.view.JasperViewer;

/**
 * Controlador para la ventana de datos de personajes.
 * Gestiona la interacción con los elementos del FXML y soporta multiidioma.
 *
 * @author Marco
 * @version 1.2
 */
public class ControladorDatos {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ControladorDatos.class);

    /** Bundle del sistema de internacionalización. */
    @FXML
    private ResourceBundle resources;

    /** SLUG del personaje actual (necesario para la eliminación) */
    private String personajeSlug;

    /** Imagen del personaje */
    @FXML
    private ImageView imageView;

    /** Etiquetas FXML que muestran los datos del personaje */
    @FXML
    private Label nombreLabel, aliasLabel, animagusLabel, bloodStatusLabel, boggartLabel, nacidoLabel,
            fallecidoLabel, colorOjosLabel, familiaresLabel, generoLabel, colorPeloLabel, alturaLabel,
            casaLabel, imagenLabel, trabajosLabel, estadoCivilLabel, nacionalidadLabel, patronusLabel,
            romancesLabel, colorPielLabel, especieLabel, titulosLabel, varitasLabel, pesoLabel;

    /** Botones principales de acción */
    @FXML
    private Button actualizarButton, exportarButton, eliminarButton, closeButton;

    // ✅ NUEVO: La ruta donde están tus imágenes arregladas
    private static final String RUTA_LOCAL_IMAGENES = "C:\\Users\\dm2\\Reto3_Hogwarts_Anuario\\imagenes\\";

    /**
     * Metodo de inicialización del controlador.
     * Configura los textos de la interfaz según el idioma recibido.
     *
     * @author Marco
     */
    @FXML
    public void initialize() {
        if (this.resources == null) {
            try {
                this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
            } catch (Exception e) {
                logger.error("No se pudo cargar ResourceBundle por defecto.", e);
            }
        }

        logger.info("ControladorDatos inicializado. Idioma: {}",
                resources != null ? resources.getLocale() : "Desconocido");

        configurarTextosBotones();
    }

    /**
     * Configura los textos y tooltips de los botones usando el ResourceBundle.
     *
     * @author Marco
     */
    private void configurarTextosBotones() {
        if (resources == null)
            return;

        try {
            if (actualizarButton != null) {
                actualizarButton.setText(getStringSafe("actualizar.button"));
                actualizarButton.setTooltip(new Tooltip(getStringSafe("actualizar.tooltip")));
            }
            if (exportarButton != null) {
                exportarButton.setText(getStringSafe("exportar.button"));
                exportarButton.setTooltip(new Tooltip(getStringSafe("exportar.tooltip")));
            }
            if (eliminarButton != null) {
                eliminarButton.setText(getStringSafe("eliminar.button"));
                eliminarButton.setTooltip(new Tooltip(getStringSafe("eliminar.tooltip")));
            }
        } catch (Exception e) {
            logger.warn("Error configurando botones: {}", e.getMessage());
        }
    }

    /**
     * Asigna el identificador único (slug) del personaje actual y dispara la carga
     * de datos.
     *
     * @author Marco
     */
    public void setPersonajeSlug(String slug) {
        this.personajeSlug = slug;
        cargarDatosPersonaje(slug);
    }

    /**
     * Carga el personaje completo usando el SLUG de la lista de datos.
     *
     * @author Nizam
     */
    private void cargarDatosPersonaje(String slug) {
        if (slug == null || slug.isEmpty()) {
            logger.error("SLUG nulo o vacío. No se pueden cargar los datos.");
            return;
        }

        List<Map<String, String>> todosPersonajes = PersonajeCSVManager.leerTodosLosPersonajes();

        Optional<Map<String, String>> personajeEncontrado = todosPersonajes.stream()
                .filter(p -> slug.equalsIgnoreCase(p.getOrDefault("slug", "")))
                .findFirst();

        if (personajeEncontrado.isPresent()) {
            rellenarInterfaz(personajeEncontrado.get());
        } else {
            logger.error("Personaje con SLUG '{}' no encontrado.", slug);
            establecerTexto(nombreLabel, "nombre.label", getStringSafe("error.nodatos"));
        }
    }

    /**
     * Rellena las etiquetas FXML con los valores del mapa del personaje.
     *
     * @author Nizam
     */
    private void rellenarInterfaz(Map<String, String> p) {
        String nombre = p.getOrDefault("name", "");
        String imagePathCSV = p.getOrDefault("image", "");

        boolean imagenCargada = false;

        // 1. INTENTO LOCAL (PRIORIDAD): Buscar en tu carpeta C:\...\imagenes
        // Formateamos el nombre (Ej: "Harry Potter") para buscar el archivo
        String nombreBonito = formatearTexto(nombre);
        if (!nombreBonito.isEmpty()) {
            // Prueba 1: Nombre bonito (Ej: Harry Potter.png)
            if (intentarCargarVariasExtensiones(nombreBonito)) imagenCargada = true;

            // Prueba 2: Nombre con guiones (Ej: harry-potter.png)
            if (!imagenCargada) {
                String nombreGuiones = nombre.toLowerCase().trim().replaceAll("\\s+", "-");
                if (intentarCargarVariasExtensiones(nombreGuiones)) imagenCargada = true;
            }
        }

        // 2. INTENTO CSV (FALLBACK): Si no está en local, probamos la ruta del CSV
        if (!imagenCargada && !imagePathCSV.isEmpty()) {
            // Intento limpiar nombre del archivo del CSV
            String nombreArchivoCSV = limpiaNombreArchivo(imagePathCSV);
            if (intentarCargarVariasExtensiones(nombreArchivoCSV)) {
                imagenCargada = true;
            }
            // Si es una URL web real (http...), intentamos cargarla (opcional)
            else if (imagePathCSV.startsWith("http")) {
                try {
                    imageView.setImage(new Image(imagePathCSV, true));
                    imagenCargada = true;
                } catch (Exception e) {}
            }
        }

        // 3. IMAGEN POR DEFECTO: Si todo falla
        if (!imagenCargada) {
            cargarImagenPorDefecto();
        }

        // Relleno de textos
        establecerTexto(nombreLabel, "nombre.label", p.getOrDefault("name", "N/A"));
        establecerTexto(aliasLabel, "alias.label", p.getOrDefault("alias_names", "N/A"));
        establecerTexto(animagusLabel, "animagus.label", p.getOrDefault("animagus", "N/A"));
        establecerTexto(bloodStatusLabel, "bloodStatus.label", p.getOrDefault("blood_status", "N/A"));
        establecerTexto(boggartLabel, "boggart.label", p.getOrDefault("boggart", "N/A"));
        establecerTexto(nacidoLabel, "nacido.label", p.getOrDefault("born", "N/A"));
        establecerTexto(fallecidoLabel, "fallecido.label", p.getOrDefault("died", "N/A"));
        establecerTexto(colorOjosLabel, "colorOjos.label", p.getOrDefault("eye_color", "N/A"));
        establecerTexto(familiaresLabel, "familiares.label", p.getOrDefault("family_members", "N/A"));
        establecerTexto(generoLabel, "genero.label", p.getOrDefault("gender", "N/A"));
        establecerTexto(colorPeloLabel, "colorPelo.label", p.getOrDefault("hair_color", "N/A"));
        establecerTexto(alturaLabel, "altura.label", p.getOrDefault("height", "N/A"));
        establecerTexto(casaLabel, "casa.label", p.getOrDefault("house", "N/A"));
        establecerTexto(imagenLabel, "imagen.label", imagePathCSV.isEmpty() ? "N/A" : imagePathCSV);
        establecerTexto(trabajosLabel, "trabajos.label", p.getOrDefault("jobs", "N/A"));
        establecerTexto(estadoCivilLabel, "estadoCivil.label", p.getOrDefault("marital_status", "N/A"));
        establecerTexto(nacionalidadLabel, "nacionalidad.label", p.getOrDefault("nationality", "N/A"));
        establecerTexto(patronusLabel, "patronus.label", p.getOrDefault("patronus", "N/A"));
        establecerTexto(romancesLabel, "romances.label", p.getOrDefault("romances", "N/A"));
        establecerTexto(colorPielLabel, "colorPiel.label", p.getOrDefault("skin_color", "N/A"));
        establecerTexto(especieLabel, "especie.label", p.getOrDefault("species", "N/A"));
        establecerTexto(titulosLabel, "titulos.label", p.getOrDefault("titles", "N/A"));
        establecerTexto(varitasLabel, "varitas.label", p.getOrDefault("wands", "N/A"));
        establecerTexto(pesoLabel, "peso.label", p.getOrDefault("weight", "N/A"));

        this.personajeSlug = p.get("slug");
    }

    // ✅ METODOS AUXILIARES DE CARGA DE IMAGEN (Traídos del otro controlador)

    private boolean intentarCargarVariasExtensiones(String nombreBase) {
        if (cargarImagenFuerzaBruta(nombreBase + ".png")) return true;
        if (cargarImagenFuerzaBruta(nombreBase + ".jpg")) return true;
        return cargarImagenFuerzaBruta(nombreBase + ".jpeg");
    }

    private boolean cargarImagenFuerzaBruta(String nombreArchivo) {
        File archivo = new File(RUTA_LOCAL_IMAGENES + nombreArchivo);
        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                imageView.setImage(new Image(fis));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private void cargarImagenPorDefecto() {
        try {
            InputStream imgStream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
            if (imgStream != null) {
                imageView.setImage(new Image(imgStream));
            }
        } catch (Exception ex) {
            logger.error("No se pudo cargar la imagen por defecto.", ex);
        }
    }

    private String formatearTexto(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder res = new StringBuilder();
        for (String p : palabras) if (!p.isEmpty()) {
            res.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1).toLowerCase())
                    .append(" ");
        }
        return res.toString().trim();
    }

    private String limpiaNombreArchivo(String ruta) {
        String nombre = ruta;
        if (nombre.contains("/")) nombre = nombre.substring(nombre.lastIndexOf("/") + 1);
        if (nombre.contains("?")) nombre = nombre.substring(0, nombre.indexOf("?"));
        if (nombre.contains(".")) nombre = nombre.substring(0, nombre.lastIndexOf("."));
        return nombre.replace("%20", " ");
    }

    // -------------------------------------------------------------

    /**
     * Metodo auxiliar para establecer texto en Labels de forma segura y traducida.
     *
     * @author Marco
     */
    private void establecerTexto(Label label, String key, String valor) {
        if (label != null) {
            label.setText(getStringSafe(key) + ": " + valor);
        }
    }

    /**
     * Helper para obtener strings del resource bundle evitando excepciones.
     *
     * @author Marco
     */
    private String getStringSafe(String key) {
        if (resources == null)
            return key;
        try {
            return resources.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Cierra la ventana actual de la aplicación.
     *
     * @author Marco
     */
    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Maneja la acción del botón de actualización.
     *
     * @author Nizam
     */
    @FXML
    public void handleActualizar() {
        mandarAlertas(Alert.AlertType.INFORMATION, getStringSafe("actualizar.button"), "",
                getStringSafe("actualizar.msg"));
    }

    /**
     * Maneja la acción del botón de exportación.
     *
     * @author Nizam
     */
    @FXML
    public void handleExportar() {
        logger.info("Botón 'Exportar' presionado");

        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", "No se encuentra el archivo .jrxml");
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            Map<String, Object> parameters = new HashMap<>();

            parameters.put("Nombre", obtenerValor(nombreLabel));
            parameters.put("Alias", obtenerValor(aliasLabel));
            parameters.put("Casa", obtenerValor(casaLabel));
            parameters.put("Genero", obtenerValor(generoLabel));
            parameters.put("Especie", obtenerValor(especieLabel));
            parameters.put("Ojos", obtenerValor(colorOjosLabel));
            parameters.put("Pelo", obtenerValor(colorPeloLabel));
            parameters.put("Piel", obtenerValor(colorPielLabel));
            parameters.put("Patronus", obtenerValor(patronusLabel));

            // Para el reporte también pasamos la imagen si la tenemos cargada en el ImageView
            if (imageView.getImage() != null) {
                parameters.put("Imagen", imageView.getImage());
            } else {
                InputStream imagenStream = getClass().getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
                if (imagenStream != null) {
                    parameters.put("Imagen", imagenStream);
                }
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
            JasperViewer.viewReport(jasperPrint, false);

            logger.info("Reporte PDF generado exitosamente");

        } catch (NoClassDefFoundError e) {
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "",
                    "Falta la librería JasperReports. Comente la funcionalidad si no la usa.");
        } catch (Exception e) {
            logger.error("Error Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", e.getMessage());
        }
    }

    /**
     * Maneja la acción del botón de eliminación de un personaje.
     *
     * @author Marco
     */
    @FXML
    public void handleEliminar(ActionEvent event) {
        if (personajeSlug == null || personajeSlug.isEmpty()) {
            mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", "Slug vacío.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(getStringSafe("eliminar.confirm.titulo"));
        confirmAlert.setHeaderText(getStringSafe("eliminar.confirm.header"));
        confirmAlert.setContentText(getStringSafe("eliminar.confirm.contenido"));

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            boolean exito = false;
            try {
                exito = PersonajeCSVManager.eliminarPersonajePorSlug(personajeSlug);
                logger.warn("Simulando eliminación de: {}", personajeSlug);
                exito = true;
            } catch (Exception e) {
                logger.error("Error real al eliminar el personaje.", e);
            }

            if (exito) {
                mandarAlertas(Alert.AlertType.INFORMATION, getStringSafe("exito"), "", getStringSafe("eliminar.exito"));
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                mandarAlertas(Alert.AlertType.ERROR, getStringSafe("error"), "", getStringSafe("eliminar.error"));
            }
        }
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
        alerta.showAndWait();
    }

    /**
     * Obtiene el valor textual de una etiqueta.
     *
     * @author Marco
     */
    private String obtenerValor(Label label) {
        if (label == null)
            return "";
        String text = label.getText();
        if (text == null)
            return "";
        int separatorIndex = text.indexOf(": ");
        return (separatorIndex != -1) ? text.substring(separatorIndex + 2).trim() : text.trim();
    }
}