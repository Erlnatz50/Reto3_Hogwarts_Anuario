package es.potersitos.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.potersitos.modelos.Usuarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acceso a Datos (DAO) que ejecuta el script Python para obtener datos de la API/CSV.
 */
public class UsuariosDAO {

    private static final Logger logger = LoggerFactory.getLogger(UsuariosDAO.class);
    private final Gson gson = new Gson();

    public UsuariosDAO() {
        logger.info("UsuariosDAO inicializado. Preparado para ejecuci\u00f3n de script Python.");
    }

    private void leerErroresPython(Process process) {
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                logger.error("Error de Python (stderr): {}", errorLine);
            }
        } catch (Exception e) {
            logger.error("Error al leer el stderr de Python: {}", e.getMessage());
        }
    }


    /**
     * Llama al script Python para obtener/actualizar datos de la API y del CSV.
     */
    public List<Usuarios> obtenerTodos() {
        logger.info("DAO: Llamando a script Python para obtener/actualizar datos.");

        String scriptPath = "python_api_scripts" + File.separator + "gestion_datos.py";

        // Usamos "py" para una ejecuci\u00f3n m\u00e1s fiable en Windows/PATH.
        ProcessBuilder pb = new ProcessBuilder("py", scriptPath);

        // Ajustamos el directorio de trabajo para que Python encuentre la carpeta 'data'
        pb.directory(new File(System.getProperty("user.dir")));

        try {
            Process process = pb.start();
            String jsonOutput;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                jsonOutput = reader.readLine();
            }
            leerErroresPython(process);

            if (process.waitFor() != 0) {
                logger.error("El proceso Python finaliz\u00f3 con c\u00f3digo de error.");
                return List.of();
            }

            if (jsonOutput == null || jsonOutput.isEmpty()) {
                logger.warn("Salida de Python vac\u00eda o no se encontraron usuarios.");
                return List.of();
            }

            logger.debug("JSON recibido: {}", jsonOutput);

            Type listType = new TypeToken<ArrayList<Usuarios>>() {}.getType();
            return gson.fromJson(jsonOutput, listType);

        } catch (Exception e) {
            logger.error("Fallo al ejecutar el script de Python: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // NOTA: No es necesario el m\u00e9todo guardar() si la persistencia es solo del CSV/API.
}