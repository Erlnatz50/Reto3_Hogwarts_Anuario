package es.potersitos.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase estática para gestionar la lectura y eliminación de personajes en el archivo CSV.
 */
public class PersonajeCSVManager {

    private static final Logger logger = LoggerFactory.getLogger(PersonajeCSVManager.class);
    // Ruta donde se encuentra el archivo CSV dentro de tu proyecto (puede variar)
    private static final String RUTA_CSV = "src/main/resources/es/potersitos/csv/todosPersonajes.csv";
    private static final int INDICE_SLUG = 2; // El SLUG está en la columna 3 (índice 2) del CSV

    /**
     * Elimina una línea del CSV basándose en el SLUG del personaje.
     * * @param slug El SLUG (identificador) del personaje a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public static boolean eliminarPersonajePorSlug(String slug) {
        File inputFile = new File(RUTA_CSV);
        File tempFile = new File(inputFile.getParent(), "tempPersonajes.csv");

        List<String> lineas = new ArrayList<>();
        boolean eliminado = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String lineaActual;
            while ((lineaActual = reader.readLine()) != null) {
                // El SLUG (identificador) se encuentra en la tercera columna (índice 2).
                String[] datos = lineaActual.split(",", -1);

                if (datos.length > INDICE_SLUG) {
                    // Si el SLUG de la línea actual coincide con el que queremos eliminar
                    if (datos[INDICE_SLUG].trim().equalsIgnoreCase(slug)) {
                        eliminado = true;
                        logger.info("Personaje con SLUG '{}' marcado para eliminación.", slug);
                        continue; // No escribimos esta línea en el archivo temporal
                    }
                }
                // Escribimos la línea si no es la que queremos eliminar
                writer.write(lineaActual + System.getProperty("line.separator"));
            }

            writer.flush();
            logger.info("Archivo temporal creado. Eliminado: {}", eliminado);

        } catch (IOException e) {
            logger.error("Error de E/S al intentar eliminar el personaje del CSV.", e);
            return false;
        }

        if (eliminado) {
            // Renombrar el archivo temporal al nombre original
            if (inputFile.delete()) {
                if (tempFile.renameTo(inputFile)) {
                    logger.info("Eliminación exitosa. CSV reescrito.");
                    return true;
                } else {
                    logger.error("Fallo al renombrar el archivo temporal.");
                    // Intentamos restaurar el archivo original eliminando el temporal
                    tempFile.delete();
                    return false;
                }
            } else {
                logger.error("Fallo al eliminar el archivo original.");
                tempFile.delete();
                return false;
            }
        } else {
            // Si no se eliminó nada, eliminamos el archivo temporal
            tempFile.delete();
            logger.warn("El SLUG '{}' no fue encontrado en el CSV.", slug);
            return false;
        }
    }
}