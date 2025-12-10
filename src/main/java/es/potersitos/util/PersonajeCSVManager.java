package es.potersitos.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad estática para gestionar la lectura y eliminación de personajes desde/hacia archivos CSV.
 *
 * @author Marco
 * @version 1.0
 */
public class PersonajeCSVManager {

    /** Logger para registrar operaciones y errores. */
    private static final Logger logger = LoggerFactory.getLogger(PersonajeCSVManager.class);

    /** Nombre de la carpeta de datos en el directorio home del usuario. */
    private static final String NOMBRE_CARPETA = "Reto3_Hogwarts_Anuario";

    /** Nombre del archivo CSV principal. */
    private static final String NOMBRE_ARCHIVO = "todosPersonajes.csv";

    /** Mapeo de las 28 columnas del CSV del universo Harry Potter a claves de búsqueda. */
    private static final String[] CLAVES_PERSONAJE = {
            "id", "type", "slug", "alias_names", "animagus", "blood_status", "boggart",
            "born", "died", "eye_color", "family_members", "gender", "hair_color",
            "height", "house", "image", "jobs", "marital_status", "name", "nationality",
            "patronus", "romances", "skin_color", "species", "titles", "wands",
            "weight", "wiki"
    };

    /**
     * Lee todos los personajes del archivo de datos (todosPersonajes.csv) y los devuelve como una lista de Mapas (clave -> valor).
     *
     * @return Lista de mapas donde cada mapa representa un personaje con sus 28 atributos.
     * @author Nizam
     */
    public static List<Map<String, String>> leerTodosLosPersonajes() {
        List<Map<String, String>> personajes = new ArrayList<>();
        String rutaCompleta = obtenerRutaCompletaCSV();

        File archivo = new File(rutaCompleta);
        if (!archivo.exists()){
            logger.warn("Archivo CSV no encontrado: {}", rutaCompleta);
            return Collections.emptyList();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                procesarLineaCSV(linea, personajes);
            }
            logger.info("Lectura de datos finalizada. {} personajes cargados desde {}", personajes.size(), rutaCompleta);
        } catch (IOException e) {
            logger.error("Error de E/S al intentar leer el archivo de datos {}: {}.", rutaCompleta, e.getMessage());
        }

        return personajes;
    }

    /**
     * Procesa una línea individual del CSV y la convierte en un mapa de personaje.
     *
     * @param linea Línea completa del CSV
     * @param personajes Lista destino para agregar el personaje procesado
     * @author Nizam
     */
    private static void procesarLineaCSV(String linea, List<Map<String, String>> personajes) {
        if (linea.trim().isEmpty()) return;

        String[] datos = linea.split(",", -1);
        if (datos.length < CLAVES_PERSONAJE.length) {
            logger.warn("Línea inválida: {} campos (requiere {})", datos.length, CLAVES_PERSONAJE.length);
            return;
        }

        Map<String, String> personaje = new HashMap<>();
        for (int i = 0; i < CLAVES_PERSONAJE.length; i++) {
            String valor = i < datos.length ? datos[i].trim() : "";
            personaje.put(CLAVES_PERSONAJE[i], valor);
        }
        personajes.add(personaje);
    }

    /**
     * Elimina una línea del archivo de datos basándose en el SLUG del personaje.
     *
     * @param slug El SLUG (identificador) del personaje a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     * @author Marco
     */
    public static boolean eliminarPersonajePorSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            logger.warn("SLUG inválido proporcionado para eliminación");
            return false;
        }

        String rutaCSV = obtenerRutaCompletaCSV();
        File archivoCSV = new File(rutaCSV);

        if (!archivoCSV.exists()) {
            logger.warn("No se puede eliminar: archivo CSV no existe ({})", rutaCSV);
            return false;
        }

        List<Map<String, String>> personajes = leerTodosLosPersonajes();
        if (personajes.isEmpty()) {
            logger.warn("Archivo vacío, no hay personajes para eliminar");
            return false;
        }

        boolean eliminado = false;
        List<Map<String, String>> personajesFiltrados = new ArrayList<>();

        for (Map<String, String> personaje : personajes) {
            String personajeSlug = personaje.getOrDefault("slug", "").trim();
            if (personajeSlug.equalsIgnoreCase(slug)) {
                eliminado = true;
                logger.info("Eliminando personaje con SLUG: {}", slug);
            } else {
                personajesFiltrados.add(personaje);
            }
        }

        if (eliminado) {
            return reescribirCSV(personajesFiltrados, rutaCSV);
        } else {
            logger.warn("SLUG '{}' no encontrado en el CSV", slug);
            return false;
        }
    }

    /**
     * Reescribe completamente el archivo CSV con la nueva lista de personajes.
     *
     * @param personajesFiltrados Lista de personajes sin el eliminado
     * @param rutaCSV Ruta del archivo destino
     * @return {@code true} si se escribió correctamente
     */
    private static boolean reescribirCSV(List<Map<String, String>> personajesFiltrados, String rutaCSV) {
        try (FileWriter writer = new FileWriter(rutaCSV)) {
            for (Map<String, String> personaje : personajesFiltrados) {
                StringBuilder linea = new StringBuilder();
                for (int i = 0; i < CLAVES_PERSONAJE.length; i++) {
                    String valor = personaje.getOrDefault(CLAVES_PERSONAJE[i], "");
                    if (valor.contains(",") || valor.contains("\"")) {
                        valor = "\"" + valor.replace("\"", "\"\"") + "\"";
                    }
                    linea.append(valor);
                    if (i < CLAVES_PERSONAJE.length - 1) {
                        linea.append(",");
                    }
                }
                writer.write(linea + System.lineSeparator());
            }
            logger.info("CSV reescrito: {} personajes restantes", personajesFiltrados.size());
            return true;
        } catch (IOException e) {
            logger.error("Error reescribiendo CSV: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Construye la ruta completa del archivo CSV usando el directorio home del usuario actual.
     *
     * @return Ruta absoluta: {@code $HOME/Reto3_Hogwarts_Anuario/todosPersonajes.csv}
     */
    private static String obtenerRutaCompletaCSV() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + NOMBRE_CARPETA + File.separator + NOMBRE_ARCHIVO;
    }
}