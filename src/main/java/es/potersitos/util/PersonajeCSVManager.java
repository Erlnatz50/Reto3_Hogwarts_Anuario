package es.potersitos.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonajeCSVManager {

    private static final Logger logger = LoggerFactory.getLogger(PersonajeCSVManager.class);
    private static final String NOMBRE_CARPETA = "Reto3_Hogwarts_Anuario";
    private static final String NOMBRE_ARCHIVO = "todosPersonajes.csv";

    // ORDEN EXACTO basado en tu captura de pantalla
    private static final String[] CLAVES_PERSONAJE = {
            "id", // 0
            "type", // 1 (Aquí dice 'character', por eso te salía mal antes)
            "slug", // 2
            "alias_names", // 3
            "animagus", // 4
            "blood_status", // 5
            "boggart", // 6
            "born", // 7
            "died", // 8
            "eye_color", // 9
            "family_members", // 10
            "gender", // 11
            "hair_color", // 12
            "height", // 13
            "house", // 14
            "image", // 15
            "jobs", // 16
            "marital_status", // 17
            "name", // 18 <-- AQUÍ está el nombre real
            "nationality", // 19
            "patronus", // 20
            "romances", // 21
            "skin_color", // 22
            "species", // 23
            "titles", // 24
            "wands", // 25
            "weight", // 26
            "wiki" // 27
    };

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
                // Saltamos la primera línea de títulos
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

    private static void procesarLineaCSV(String linea, List<Map<String, String>> personajes) {
        if (linea.trim().isEmpty())
            return;

        // Regex para separar por comas ignorando las que están entre comillas
        String[] datos = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        Map<String, String> personaje = new HashMap<>();

        // Mapeamos cada valor a su columna correspondiente
        int totalColumnas = Math.min(CLAVES_PERSONAJE.length, datos.length);

        for (int i = 0; i < totalColumnas; i++) {
            // Quitamos espacios y comillas extra
            String valor = datos[i].trim().replace("\"", "");
            personaje.put(CLAVES_PERSONAJE[i], valor);
        }

        // Valores por defecto para evitar errores visuales
        personaje.putIfAbsent("name", "Sin Nombre");
        personaje.putIfAbsent("house", "Sin Casa");

        personajes.add(personaje);
    }

    // --- Métodos de eliminación y utilidades ---

    public static boolean eliminarPersonajePorSlug(String slug) {
        if (slug == null || slug.trim().isEmpty())
            return false;
        String rutaCSV = obtenerRutaCompletaCSV();
        List<Map<String, String>> personajes = leerTodosLosPersonajes();
        boolean eliminado = personajes.removeIf(p -> slug.equalsIgnoreCase(p.getOrDefault("slug", "")));
        if (eliminado)
            return reescribirCSV(personajes, rutaCSV);
        return false;
    }

    public static boolean actualizarPersonaje(Map<String, String> nuevosDatos) {
        String slug = nuevosDatos.get("slug");
        if (slug == null || slug.isEmpty())
            return false;

        String rutaCSV = obtenerRutaCompletaCSV();
        List<Map<String, String>> personajes = leerTodosLosPersonajes();
        boolean encontrado = false;

        for (int i = 0; i < personajes.size(); i++) {
            Map<String, String> p = personajes.get(i);
            if (slug.equalsIgnoreCase(p.get("slug"))) {
                personajes.set(i, nuevosDatos); // Reemplazamos
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            return reescribirCSV(personajes, rutaCSV);
        }
        return false;
    }

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
            return false;
        }
    }

    private static String obtenerRutaCompletaCSV() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + NOMBRE_CARPETA + File.separator + NOMBRE_ARCHIVO;
    }
}