module es.potersitos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires jdk.compiler;
    requires java.desktop;
    requires java.net.http;
    requires java.sql;
    requires jasperreports;
    requires javafx.swing;

    opens es.potersitos.controladores to javafx.fxml;

    opens es.potersitos to javafx.fxml;

    exports es.potersitos;
}
