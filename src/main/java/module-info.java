module Reto3_Hogwarts_Anuario {

    // --- Módulos JavaFX/Gráficos ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // --- Módulos Core de Java ---
    requires java.sql;

    // --- Módulos de Dependencias Externas ---
    requires org.slf4j;
    requires com.google.gson;

    // --- Apertura de Paquetes ---

    opens es.potersitos.controladores to javafx.fxml;

    // Abre los modelos a JavaFX (para TableView) y a Gson (para deserialización JSON)
    opens es.potersitos.modelos to javafx.base, com.google.gson;

    // Abre el paquete base para la clase App (Lanzador/Start)
    opens es.potersitos to javafx.graphics, javafx.fxml;

    // --- Exports (exportaciones) ---
    exports es.potersitos;
    exports es.potersitos.controladores;
    exports es.potersitos.dao;
    exports es.potersitos.modelos;
}