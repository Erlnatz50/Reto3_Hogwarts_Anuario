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
 * Gestor de personajes desde un CSV.
 * Proporciona lectura, eliminación y actualización de registros de personajes.
 *
 * @author Nizam
 * @version 1.0
 */
public class PersonajeCSVManager {

    /** Logger para la clase. */
    private static final Logger logger = LoggerFactory.getLogger(PersonajeCSVManager.class);

    /** Nombre de la carpeta donde se almacena el CSV. */
    private static final String NOMBRE_CARPETA = "Reto3_Hogwarts_Anuario";

    /** Nombre del archivo CSV */
    private static final String NOMBRE_ARCHIVO = "todosPersonajes.csv";

    /** Claves/columnas esperadas en el CSV. */
    private static final String[] CLAVES_PERSONAJE = {
            "id",
            "type",
            "slug",
            "alias_names",
            "animagus",
            "blood_status",
            "boggart",
            "born",
            "died",
            "eye_color",
            "family_members",
            "gender",
            "hair_color",
            "height",
            "house",
            "image",
            "jobs",
            "marital_status",
            "name",
            "nationality",
            "patronus",
            "romances",
            "skin_color",
            "species",
            "titles",
            "wands",
            "weight",
            "wiki"
    };

    /**
     * Lee todos los personajes desde el CSV.
     *
     * @return Lista de mapas, donde cada mapa representa un personaje con claves de
     *         CLAVES_PERSONAJE.
     * @author Nizam
     */
    public static List<Map<String, String>> leerTodosLosPersonajes() {
        List<Map<String, String>> personajes = new ArrayList<>();
        String rutaCompleta = obtenerRutaCompletaCSV();
        File archivo = new File(rutaCompleta);

        if (!archivo.exists()) {
            logger.warn("Archivo CSV no encontrado en: {}", rutaCompleta);
            return Collections.emptyList();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean esEncabezado = true;

            while ((linea = reader.readLine()) != null) {
                if (esEncabezado) {
                    esEncabezado = false;
                    continue;
                }
                procesarLineaCSV(linea, personajes);
            }
            logger.info("Carga completada: {} personajes leídos.", personajes.size());
        } catch (IOException e) {
            logger.error("Error leyendo el CSV: {}", e.getMessage());
        }

        return personajes;
    }

    /**
     * Procesa una línea del CSV y la añade a la lista de personajes.
     *
     * @param linea      Línea del CSV.
     * @param personajes Lista donde se almacenarán los mapas de personajes.
     * @author Nizam
     */
    private static void procesarLineaCSV(String linea, List<Map<String, String>> personajes) {
        if (linea == null || linea.trim().isEmpty()) {
            return;
        }

        String[] datos = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        Map<String, String> personaje = new HashMap<>();

        int totalColumnas = Math.min(CLAVES_PERSONAJE.length, datos.length);

        for (int i = 0; i < totalColumnas; i++) {
            String valor = datos[i].trim().replace("\"", "");
            personaje.put(CLAVES_PERSONAJE[i], valor);
        }

        personaje.putIfAbsent("name", "Sin Nombre");
        personaje.putIfAbsent("house", "Sin Casa");

        personajes.add(personaje);
    }

    /**
     * Elimina un personaje por su slug.
     *
     * @param slug Slug del personaje a eliminar.
     * @return true si se eliminó correctamente, false en caso contrario.
     * @author Telmo
     */
    public static boolean eliminarPersonajePorSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }
        String rutaCSV = obtenerRutaCompletaCSV();
        List<Map<String, String>> personajes = leerTodosLosPersonajes();
        boolean eliminado = personajes.removeIf(p -> slug.equalsIgnoreCase(p.getOrDefault("slug", "")));
        if (eliminado) {
            return reescribirCSV(personajes, rutaCSV);
        }
        return false;
    }

    /**
     * Actualiza un personaje con nuevos datos.
     * Intenta identificar al personaje primero por su "id" y luego por su "slug".
     *
     * @param nuevosDatos Mapa con los datos actualizados del personaje.
     * @return true si se actualizó correctamente, false en caso contrario.
     * @author Telmo
     */
    public static boolean actualizarPersonaje(Map<String, String> nuevosDatos) {
        String id = nuevosDatos.get("id");
        String slug = nuevosDatos.get("slug");

        if ((id == null || id.isEmpty()) && (slug == null || slug.isEmpty())) {
            return false;
        }

        String rutaCSV = obtenerRutaCompletaCSV();
        List<Map<String, String>> personajes = leerTodosLosPersonajes();
        boolean encontrado = false;

        for (int i = 0; i < personajes.size(); i++) {
            Map<String, String> p = personajes.get(i);

            if (id != null && !id.isEmpty() && id.equalsIgnoreCase(p.get("id"))) {
                personajes.set(i, nuevosDatos);
                encontrado = true;
                break;
            }

            if (slug != null && !slug.isEmpty() && slug.equalsIgnoreCase(p.get("slug"))) {
                personajes.set(i, nuevosDatos);
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            return reescribirCSV(personajes, rutaCSV);
        }
        return false;
    }

    /**
     * Reescribe el CSV a partir de la lista de personajes.
     *
     * @param lista Lista de personajes (maps).
     * @param ruta  Ruta completa del CSV.
     * @return true si se escribió correctamente, false en caso contrario.
     * @author Nizam
     */
    private static boolean reescribirCSV(List<Map<String, String>> lista, String ruta) {
        try (FileWriter writer = new FileWriter(ruta)) {
            writer.write(String.join(",", CLAVES_PERSONAJE) + "\n");
            for (Map<String, String> p : lista) {
                List<String> valores = new ArrayList<>();
                for (String key : CLAVES_PERSONAJE) {
                    String val = p.getOrDefault(key, "");
                    if (val.contains(","))
                        val = "\"" + val + "\"";
                    valores.add(val);
                }
                writer.write(String.join(",", valores) + "\n");
            }
            return true;
        } catch (IOException e) {
            logger.error("Error al escribir en el CSV: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene la ruta completa del CSV a partir del directorio del usuario.
     *
     * @return Ruta completa del CSV.
     * @author Erlantz
     */
    private static String obtenerRutaCompletaCSV() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + NOMBRE_CARPETA + File.separator + NOMBRE_ARCHIVO;
    }
}