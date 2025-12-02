package es.potersitos.controladores;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import es.potersitos.modelos.PersonajeJava;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ControladorNuevoPersonaje {

    // ---- Campos de entrada ----
    @FXML private TextField nameField;

    @FXML private TextArea aliasField;
    @FXML private TextArea familyField;
    @FXML private TextArea romancesField;
    @FXML private TextArea jobsField;
    @FXML private TextArea titlesField;
    @FXML private TextArea wandsField;
    @FXML private TextArea schoolShipField;

    @FXML private TextField animagusField;
    @FXML private TextField bloodStatusField;
    @FXML private TextField boggartField;
    @FXML private TextField nacidoField;
    @FXML private TextField fallecidoField;
    @FXML private TextField colorOjosField;
    @FXML private TextField familiaresField;
    @FXML private TextField generoField;
    @FXML private TextField colorPeloField;
    @FXML private TextField alturaField;
    @FXML private TextField casaField;
    @FXML private TextField imagenField;
    @FXML private TextField trabajosExtraField;
    @FXML private TextField estadoCivilField;
    @FXML private TextField nacionalidadField;
    @FXML private TextField patronusField;
    @FXML private TextField colorPielField;
    @FXML private TextField especieField;
    @FXML private TextField pesoField;

    @FXML private ImageView imageView;

    private PersonajeJava personaje;

    // ---- Asignar personaje y mostrar datos ----
    public void setPersonaje(PersonajeJava p) {
        this.personaje = p;
        mostrarDatos();
    }

    private void mostrarDatos() {
        if (personaje == null) return;

        nameField.setText(safe(personaje.name));
        aliasField.setText(listToString(personaje.alias_names));
        familyField.setText(listToString(personaje.family_members));
        romancesField.setText(listToString(personaje.romances));
        jobsField.setText(listToString(personaje.jobs));

        titlesField.setText(listToString(personaje.titles));
        wandsField.setText(listToString(personaje.wands));
        schoolShipField.setText(safe(personaje.school_ship));

        animagusField.setText(safe(personaje.animagus));
        bloodStatusField.setText(safe(personaje.bloodStatus));
        boggartField.setText(safe(personaje.boggart));
        nacidoField.setText(safe(personaje.nacido));
        fallecidoField.setText(safe(personaje.fallecido));
        colorOjosField.setText(safe(personaje.colorOjos));
        familiaresField.setText(safe(personaje.familiares));
        generoField.setText(safe(personaje.genero));
        colorPeloField.setText(safe(personaje.colorPelo));
        alturaField.setText(safe(personaje.altura));
        casaField.setText(safe(personaje.casa));
        imagenField.setText(safe(personaje.imagen));
        trabajosExtraField.setText(safe(personaje.trabajos));
        estadoCivilField.setText(safe(personaje.estadoCivil));
        nacionalidadField.setText(safe(personaje.nacionalidad));
        patronusField.setText(safe(personaje.patronus));
        colorPielField.setText(safe(personaje.colorPiel));
        especieField.setText(safe(personaje.especie));
        pesoField.setText(safe(personaje.peso));

        // Cargar imagen
        if (personaje.image_blob != null) {
            byte[] imgBytes = Base64.getDecoder().decode(personaje.image_blob);
            imageView.setImage(new Image(new ByteArrayInputStream(imgBytes)));
        }
    }

    // ---- Helpers ----
    private String listToString(java.util.List<String> list) {
        return list == null ? "" : String.join("\n", list);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
