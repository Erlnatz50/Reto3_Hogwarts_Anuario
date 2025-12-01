package es.potersitos.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.potersitos.modelos.PersonajeJava;

public class ControladorNuevoPersonaje {

    // Campos FXML
    @FXML private TextField idField;
    @FXML private TextField typeField;
    @FXML private TextField slugField;
    @FXML private TextField nameField;
    @FXML private TextArea aliasField;
    @FXML private TextField animagusField;
    @FXML private TextField bloodStatusField;
    @FXML private TextField boggartField;
    @FXML private TextField bornField;
    @FXML private TextField diedField;
    @FXML private TextField eyeColorField;
    @FXML private TextArea familyField;
    @FXML private TextField genderField;
    @FXML private TextField hairColorField;
    @FXML private TextField heightField;
    @FXML private TextField houseField;
    @FXML private TextArea jobsField;
    @FXML private TextField maritalStatusField;
    @FXML private TextField nationalityField;
    @FXML private TextField patronusField;
    @FXML private TextArea romancesField;
    @FXML private TextField skinColorField;
    @FXML private TextField speciesField;
    @FXML private TextArea titlesField;
    @FXML private TextArea wandsField;
    @FXML private TextField weightField;
    @FXML private TextField wikiField;
    @FXML private ImageView imageView;

    private File imageFile;

    // --------------------------------------------------------------------
    // Cerrar ventana
    // --------------------------------------------------------------------
    @FXML
    public void cerrarVentana(ActionEvent e) {
        Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
        stage.close();
    }

    // --------------------------------------------------------------------
    // Cargar imagen
    // --------------------------------------------------------------------
    @FXML
    public void cargarImagen(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(((Button) e.getSource()).getScene().getWindow());
        if (file != null) {
            imageFile = file;
            try (FileInputStream fis = new FileInputStream(file)) {
                Image img = new Image(fis);
                imageView.setImage(img);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // --------------------------------------------------------------------
    // Guardar personaje llamando al script Python
    // --------------------------------------------------------------------
    @FXML
    public void guardarPersonaje(ActionEvent e) {
        try {
            // Crear DTO desde formulario
            PersonajeJava personaje = crearPersonajeDesdeFormulario();

            // Crear JSON temporal
            ObjectMapper mapper = new ObjectMapper();
            File tempJson = new File("personaje_temp.json");
            mapper.writeValue(tempJson, personaje);

            // Llamar a Python para guardar en CSV/XML/BIN
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "servicios/guardar_personaje.py", // Ajustar ruta según tu proyecto
                    tempJson.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Leer salida del script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();

            // Cerrar ventana
            cerrarVentana(e);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // --------------------------------------------------------------------
    // Crear DTO desde formulario con campos null si están vacíos
    // --------------------------------------------------------------------
    private PersonajeJava crearPersonajeDesdeFormulario() throws IOException {
        PersonajeJava p = new PersonajeJava();

        p.id = nullSiVacio(idField.getText());
        p.type = nullSiVacio(typeField.getText());
        p.slug = nullSiVacio(slugField.getText());
        p.name = nullSiVacio(nameField.getText());

        p.alias_names = listaDesdeTextArea(aliasField);
        p.animagus = nullSiVacio(animagusField.getText());
        p.blood_status = nullSiVacio(bloodStatusField.getText());
        p.boggart = nullSiVacio(boggartField.getText());
        p.born = nullSiVacio(bornField.getText());
        p.died = nullSiVacio(diedField.getText());
        p.eye_color = nullSiVacio(eyeColorField.getText());

        p.family_members = listaDesdeTextArea(familyField);
        p.gender = nullSiVacio(genderField.getText());
        p.hair_color = nullSiVacio(hairColorField.getText());
        p.height = nullSiVacio(heightField.getText());
        p.house = nullSiVacio(houseField.getText());

        p.jobs = listaDesdeTextArea(jobsField);
        p.marital_status = nullSiVacio(maritalStatusField.getText());
        p.nationality = nullSiVacio(nationalityField.getText());
        p.patronus = nullSiVacio(patronusField.getText());

        p.romances = listaDesdeTextArea(romancesField);
        p.skin_color = nullSiVacio(skinColorField.getText());
        p.species = nullSiVacio(speciesField.getText());

        p.titles = listaDesdeTextArea(titlesField);
        p.wands = listaDesdeTextArea(wandsField);

        p.weight = nullSiVacio(weightField.getText());
        p.wiki = nullSiVacio(wikiField.getText());

        // Imagen en Base64
        if (imageFile != null) {
            byte[] bytes = Files.readAllBytes(imageFile.toPath());
            p.image_blob = Base64.getEncoder().encodeToString(bytes);
        } else {
            p.image_blob = null;
        }

        return p;
    }

    // --------------------------------------------------------------------
    // Funciones auxiliares
    // --------------------------------------------------------------------
    private String nullSiVacio(String texto) {
        if (texto == null) return null;
        texto = texto.trim();
        return texto.isEmpty() ? null : texto;
    }

    private List<String> listaDesdeTextArea(TextArea area) {
        if (area == null) return null;
        String contenido = area.getText().trim();
        if (contenido.isEmpty()) return null;
        return Arrays.asList(contenido.split("\n"));
    }
}
