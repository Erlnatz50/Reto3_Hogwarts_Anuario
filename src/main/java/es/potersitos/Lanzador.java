package es.potersitos;

import javafx.application.Application; // Importación necesaria para usar Application.launch()

/**
 * Clase lanzadora de la aplicación JavaFX.
 * Se utiliza como punto de entrada (main) y llama directamente a Application.launch().
 *
 * @author Erlantz
 * @version 1.0
 */
public class Lanzador {

    /**
     * Metodo principal de la clase lanzadora.
     * Llama al método launch() de JavaFX para iniciar el ciclo de vida de la aplicación.
     *
     * @param args Argumentos de línea de comandos.
     *
     * @author Erlantz
     */
    public static void main(String[] args){
        // Llamada directa al método estático launch() de la clase Application.
        // Esto inicia el ciclo de vida de la aplicación App, evitando el error de visibilidad.
        Application.launch(App.class, args);
    }
}