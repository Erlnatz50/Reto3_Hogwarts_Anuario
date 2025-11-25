module es.erlantzg {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires jdk.compiler;
    requires java.desktop;
    requires es.erlantzg;

    opens es.potersitos.controladores to javafx.fxml;
    opens es.potersitos.modelos to javafx.base;

    opens es.potersitos to javafx.fxml;
    exports es.potersitos;
}
