module es.erlantzg {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires jdk.compiler;
    requires java.desktop;
    requires javafx.graphics;

    requires com.google.gson;

    // ¡CORRECCIÓN CLAVE! Requerido para usar JDBC (java.sql.Connection, etc.)
    requires java.sql;

    opens es.potersitos.controladores to javafx.fxml;
    opens es.potersitos.modelos to javafx.base, com.google.gson;
    opens es.potersitos to javafx.graphics, javafx.fxml;

    exports es.potersitos;
}