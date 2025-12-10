package es.potersitos.util;

// Importaciones necesarias
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase estática modificada para leer datos y devolverlos como List<Map<String, String>>
 * para evitar el uso de la clase modelo Personaje.java.
 *
 * @author Marco / Modificado por Gemini
 * @version 1.4 (Ruta confirmada, lógica de eliminación refinada)
 */
public class PersonajeCSVManager {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(PersonajeCSVManager.class);

    // --- RUTA CLAVE ---
    private static final String RUTA_ARCHIVO_DATOS = "C:\\Users\\dm2\\Reto3_Hogwarts_Anuario\\todosPersonajes.csv";

    /** Índice donde se encuentra el SLUG del personaje en la línea (se asume que es el 3er campo, índice 2). */
    private static final int INDICE_SLUG = 2;

    // --- Array con las claves de los 28 atributos de tu modelo Python ---
    private static final String[] CLAVES_PERSONAJE = {
            "id", "type", "slug", "alias_names", "animagus", "blood_status", "boggart",
            "born", "died", "eye_color", "family_members", "gender", "hair_color",
            "height", "house", "image", "jobs", "marital_status", "name", "nationality",
            "patronus", "romances", "skin_color", "species", "titles", "wands",
            "weight", "wiki"
    };

    // ------------------------------------------------------------------------
    // MÉTODOS DE LECTURA
    // ------------------------------------------------------------------------

    /**
     * Lee todos los personajes del archivo de datos (todosPersonajes.csv) y los devuelve como una lista de Mapas (clave -> valor).
     *
     * @return Una lista de Map<String, String>, donde cada mapa es un personaje.
     */
    public static List<Map<String, String>> leerTodosLosPersonajes() {
        List<Map<String, String>> listaPersonajesMapeados = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(RUTA_ARCHIVO_DATOS))) {
            String lineaActual;

            // Si el archivo tiene una línea de cabecera (títulos de columnas), descomenta la siguiente línea para saltarla.
            // reader.readLine();

            while ((lineaActual = reader.readLine()) != null) {
                if (lineaActual.trim().isEmpty()) continue; // Ignorar líneas vacías

                // --- Se asume que el delimitador es la coma (',') ---
                String[] datos = lineaActual.split(",", -1);

                // Debe haber al menos tantos datos como CLAVES_PERSONAJE
                if (datos.length >= CLAVES_PERSONAJE.length) {
                    Map<String, String> personajeMap = new HashMap<>();

                    // Recorremos los datos y los asignamos al mapa usando las claves predefinidas
                    for (int i = 0; i < CLAVES_PERSONAJE.length; i++) {
                        // Usamos un valor seguro ("") si el dato es null o está fuera de límites
                        String valor = (i < datos.length) ? datos[i].trim() : "";
                        personajeMap.put(CLAVES_PERSONAJE[i], valor);
                    }

                    listaPersonajesMapeados.add(personajeMap);
                } else {
                    logger.warn("Línea omitida en el archivo {} por formato incorrecto (solo {} de {} campos).",
                            RUTA_ARCHIVO_DATOS, datos.length, CLAVES_PERSONAJE.length);
                }
            }
            logger.info("Lectura de datos finalizada. {} personajes cargados desde {}", listaPersonajesMapeados.size(), RUTA_ARCHIVO_DATOS);
        } catch (IOException e) {
            // Este error suele indicar que la RUTA_ARCHIVO_DATOS es incorrecta o no tiene permisos
            logger.error("Error de E/S al intentar leer el archivo de datos: {}. Compruebe ruta y permisos.", RUTA_ARCHIVO_DATOS, e);
        }

        return listaPersonajesMapeados;
    }


    // ------------------------------------------------------------------------
    // MÉTODOS DE ESCRITURA/ELIMINACIÓN
    // ------------------------------------------------------------------------

    /**
     * Elimina una línea del archivo de datos basándose en el SLUG del personaje.
     *
     * @param slug El SLUG (identificador) del personaje a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     * @author Marco
     */
    public static boolean eliminarPersonajePorSlug(String slug) {
        File inputFile = new File(RUTA_ARCHIVO_DATOS);
        File tempFile = new File(inputFile.getParent(), "tempPersonajes.tmp");

        boolean eliminado = false;

        // 1. Escribir todas las líneas EXCEPTO la eliminada en un archivo temporal
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String lineaActual;
            while ((lineaActual = reader.readLine()) != null) {
                if (lineaActual.trim().isEmpty()) continue;

                String[] datos = lineaActual.split(",", -1);

                if (datos.length > INDICE_SLUG) {
                    if (datos[INDICE_SLUG].trim().equalsIgnoreCase(slug)) {
                        eliminado = true;
                        logger.info("Personaje con SLUG '{}' marcado para eliminación.", slug);
                        continue; // No escribimos esta línea
                    }
                }
                writer.write(lineaActual + System.lineSeparator());
            }

            writer.flush();
            logger.debug("Archivo temporal creado. Eliminado: {}", eliminado);

        } catch (IOException e) {
            logger.error("Error de E/S durante el proceso de eliminación.", e);
            // Asegurarse de borrar el temporal en caso de fallo intermedio
            tempFile.delete();
            return false;
        }

        // 2. Renombrar el archivo temporal al original
        if (eliminado) {
            if (inputFile.delete()) {
                if (tempFile.renameTo(inputFile)) {
                    logger.info("Eliminación exitosa. Archivo de datos reescrito.");
                    return true;
                } else {
                    logger.error("Fallo al renombrar el archivo temporal. Debe mover/copiar manualmente: {}", tempFile.getAbsolutePath());
                    // Dejar el archivo temporal para recuperación si no se puede renombrar
                    return false;
                }
            } else {
                logger.error("Fallo al eliminar el archivo original. Permisos denegados.");
                tempFile.delete();
                return false;
            }
        } else {
            // Si no se encontró el slug, simplemente borramos el temporal
            tempFile.delete();
            logger.warn("El SLUG '{}' no fue encontrado en el archivo de datos.", slug);
            return false;
        }
    }
}