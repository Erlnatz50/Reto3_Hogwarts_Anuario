package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ControladorNuevoPersonaje {

    @FXML public TextField idField;
    @FXML public TextField typeField;
    @FXML public TextField slugField;
    @FXML public TextField aliasNamesField;
    @FXML public TextField animagusField;
    @FXML public TextField bloodStatusField;
    @FXML public TextField boggartField;
    @FXML public TextField bornField;
    @FXML public TextField diedField;
    @FXML public TextField eyeColorField;
    @FXML public TextField trabajoField;
    @FXML public TextField miembrosFamiliaField;
    @FXML public TextField colorPielField;
    @FXML public TextField varitaField;
    @FXML public TextField genderField;
    @FXML public TextField hairColorField;
    @FXML public TextField heightField;
    @FXML public TextField houseField;
    @FXML public TextField imageField;
    @FXML public TextField maritalStatusField;
    @FXML public TextField nameField;
    @FXML public TextField nationalityField;
    @FXML public TextField patronusField;
    @FXML public TextField speciesField;
    @FXML public TextField wikipediaField;
    @FXML public TextField romancesField;
    @FXML public TextField titulosField;
    @FXML public TextField pesoField;

    @FXML public Button cancelarButton;
    @FXML public Button agregarButton;

    @FXML
    public void onCancelar() {
        cancelarButton.getScene().getWindow().hide();
    }

    @FXML
    public void onAgregar() {
        try {
            String[] datos = {
                    idField.getText().trim(),
                    typeField.getText().trim(),
                    slugField.getText().trim(),
                    aliasNamesField.getText().trim(),
                    animagusField.getText().trim(),
                    bloodStatusField.getText().trim(),
                    boggartField.getText().trim(),
                    bornField.getText().trim(),
                    diedField.getText().trim(),
                    eyeColorField.getText().trim(),
                    trabajoField.getText().trim(),
                    miembrosFamiliaField.getText().trim(),
                    colorPielField.getText().trim(),
                    varitaField.getText().trim(),
                    genderField.getText().trim(),
                    hairColorField.getText().trim(),
                    heightField.getText().trim(),
                    houseField.getText().trim(),
                    imageField.getText().trim(),
                    maritalStatusField.getText().trim(),
                    nameField.getText().trim(),
                    nationalityField.getText().trim(),
                    patronusField.getText().trim(),
                    speciesField.getText().trim(),
                    wikipediaField.getText().trim(),
                    romancesField.getText().trim(),
                    titulosField.getText().trim(),
                    pesoField.getText().trim()
            };

            Path csvPath = Paths.get(System.getProperty("user.dir"), "datos", "todosPersonajes.csv");
            Files.createDirectories(csvPath.getParent());

            String linea = String.join(",", datos) + "\n";
            Files.write(csvPath, linea.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println("✅ Añadido a: " + csvPath.toAbsolutePath());
            cancelarButton.getScene().getWindow().hide();

        } catch (IOException e) {
            System.err.println("❌ Error CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
