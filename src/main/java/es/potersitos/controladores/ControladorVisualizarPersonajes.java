package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador encargado de gestionar la vista de personajes.
 * Permite filtrar, cambiar idioma, seleccionar y exportar personajes.
 *
 * @author Telmo
 * @version 1.0
 */
public class ControladorVisualizarPersonajes {

    /** Botones generales de la interfaz. */
    @FXML
    public Button btnFiltrar, btnCerrarFiltro, btnSeleccionar, btnExportar, btnAplicarFiltro, btnLimpiarFiltro;

    /**  */
    private Button botonImportar;

    /**  */
    private boolean selectionModeActive = false;

    /**  */
    private Set<String> selectedSlugs = new HashSet<>();

    /** Etiqueta que muestra la página actual del paginador. */
    @FXML
    private Label lblPaginaActual;

    /** Barra de menú principal. */
    @FXML
    public MenuBar menuBar;

    /** Elementos del menú superior. */
    @FXML
    public MenuItem menuSalir, menuNuevo, menuGuardar;

    /** Contenedor que aloja las fichas de los personajes. */
    @FXML
    private TilePane tilePanePersonajes;

    /** Panel lateral de filtros. */
    @FXML
    private VBox filterPanel;

    /** Acordeón que contiene todos los grupos de filtros. */
    @FXML
    private Accordion accordionFiltros;

    /** Campo de texto usado para realizar búsquedas por nombre. */
    @FXML
    private TextField searchField;

    /**  */
    @FXML
    private HBox paginationContainer;

    /** Lista de controladores asociados a cada ficha de personaje cargado. */
    private List<ControladorFichaPersonaje> listaControladores;

    /** Lista mapeada de todos los personajes leídos desde CSV/XML. */
    private List<Map<String, String>> listaPersonajesMapeados;

    /** Recurso de internacionalización (idioma actual). */
    private ResourceBundle resources;

    /** Logger para registrar eventos y errores. */
    private static final Logger logger = LoggerFactory.getLogger(ControladorVisualizarPersonajes.class);

    /** Número de personajes que se muestran por página. */
    private static final int personajesPorPagina = 10;

    /** Página activa actual. */
    private int paginaActual = 1;

    /** Total de páginas calculadas con los personajes disponibles. */
    private int totalPaginas;

    /**
     * Metodo de inicialización automática FXML.
     *
     * @author Telmo
     */
    @FXML
    public void initialize() {
        this.resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
        botonImportar = new Button("importar");
        configurarAtajosMenu();
        configurarBusqueda();
        configurarListenersFiltros();

        listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
        calcularTotalPaginas();
        actualizarTextosUI();

        if (listaPersonajesMapeados.isEmpty()) {
            mostrarMensajeImportar();
        } else {
            cargarPersonajes(listaPersonajesMapeados);
        }

        logger.info("Vista de personajes inicializada correctamente con {} registros.", listaPersonajesMapeados.size());
    }

    /**
     * Configura los atajos de teclado del menú principal.
     *
     * @author Erlantz
     */
    private void configurarAtajosMenu() {
        menuNuevo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        menuGuardar.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuSalir.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
    }

    /**
     * Configura el listener de búsqueda en tiempo real del campo de texto.
     *
     * @author Erlantz
     */
    private void configurarBusqueda() {
        if (searchField != null) {
            searchField.textProperty().addListener((o, ov, nv) -> filtrarPersonajes());
        }
    }

    /**
     * Calcula el número total de páginas basándose en personajesPorPagina y el
     * tamaño de la lista.
     *
     * @author Nizam
     */
    private void calcularTotalPaginas() {
        int total = Math.max(1, listaPersonajesMapeados.size());
        totalPaginas = (int) Math.ceil((double) total / personajesPorPagina);
    }

    /**
     * Cambia el idioma a español.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaEspaniol() {
        cambiarIdioma(Locale.of("es"));
    }

    /**
     * Cambia el idioma a euskera.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaEuskera() {
        cambiarIdioma(Locale.of("eu"));
    }

    /**
     * Cambia el idioma a inglés.
     *
     * @author Erlantz
     */
    @FXML
    void idiomaIngles() {
        cambiarIdioma(Locale.ENGLISH);
    }

    /**
     * Cambia el idioma (locale) activo y actualiza todos los textos visibles en la
     * interfaz.
     *
     * @param nuevoLocale Nuevo idioma a aplicar.
     * @author Erlantz
     */
    private void cambiarIdioma(Locale nuevoLocale) {
        try {
            resources = ResourceBundle.getBundle("es.potersitos.mensaje", nuevoLocale);
            actualizarTextosUI();
            if (listaPersonajesMapeados.isEmpty()){
                mostrarMensajeImportar();
            } else{
                cargarPersonajes(listaPersonajesMapeados);
            }
            filtrarPersonajes();
            logger.info("Idioma cambiado a: {}", nuevoLocale);
        } catch (Exception e) {
            logger.error("Error cambiando idioma", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null,
                    "No se pudo cambiar el idioma: " + e.getMessage());
        }
    }

    /**
     * Actualiza los textos de todos los controles de la interfaz según el
     * ResourceBundle actual.
     *
     * @author Marco
     */
    private void actualizarTextosUI() {
        if (menuBar != null && menuBar.getMenus().size() >= 3) {
            menuBar.getMenus().get(0).setText(resources.getString("menu.archivo"));
            menuBar.getMenus().get(1).setText(resources.getString("menu.idioma"));
            menuBar.getMenus().get(2).setText(resources.getString("menu.ayuda"));
        }

        menuNuevo.setText(resources.getString("menu.archivo.nuevo"));
        menuGuardar.setText(resources.getString("menu.archivo.guardar"));
        menuSalir.setText(resources.getString("menu.archivo.salir"));
        botonImportar.setText(resources.getString("importar"));
        searchField.setPromptText(resources.getString("visualizar.search.prompt"));
        btnFiltrar.setText(resources.getString("visualizar.filtro.titulo"));
        btnExportar.setText(resources.getString("visualizar.btn.exportar"));
        btnAplicarFiltro.setText(resources.getString("visualizar.filtro.aplicar"));
        btnLimpiarFiltro.setText(resources.getString("visualizar.filtro.limpiar"));

        if (selectionModeActive) {
            btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
        } else {
            btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
        }

        if (accordionFiltros != null && !accordionFiltros.getPanes().isEmpty()) {
            List<TitledPane> panes = accordionFiltros.getPanes();
            panes.get(0).setText(resources.getString("filtro.titulo.casa"));
            if (panes.size() > 1)
                panes.get(1).setText(resources.getString("filtro.titulo.nacionalidad"));
            if (panes.size() > 2)
                panes.get(2).setText(resources.getString("filtro.titulo.especie"));
            if (panes.size() > 3)
                panes.get(3).setText(resources.getString("filtro.titulo.genero"));

            actualizarCheckBoxesDelPanel(panes.get(0), new String[] {
                    "filtro.valor.gryffindor", "filtro.valor.slytherin",
                    "filtro.valor.hufflepuff", "filtro.valor.ravenclaw"
            });
            if (panes.size() > 1) {
                actualizarCheckBoxesDelPanel(panes.get(1), new String[] {
                        "filtro.valor.britanico", "filtro.valor.irlandes",
                        "filtro.valor.frances", "filtro.valor.bulgaro"
                });
            }
            if (panes.size() > 2) {
                actualizarCheckBoxesDelPanel(panes.get(2), new String[] {
                        "filtro.valor.humano", "filtro.valor.mestizo",
                        "filtro.valor.elfo", "filtro.valor.gigante"
                });
            }
            if (panes.size() > 3) {
                actualizarCheckBoxesDelPanel(panes.get(3), new String[] {
                        "filtro.valor.masculino", "filtro.valor.femenino"
                });
            }
        }
        actualizarControlesPaginacion();
    }

    /**
     * Muestra un mensaje centrado indicando que no se han encontrado personajes
     * y que el usuario debe importarlos.
     *
     * @author Erlantz
     */
    private void mostrarMensajeImportar() {
        if (tilePanePersonajes != null) {
            tilePanePersonajes.getChildren().clear();

            Label mensaje = new Label(
                    "No se encontraron personajes.\n\nPor favor, importa los archivos desde el menú 'Archivo → Importar'.");
            mensaje.setStyle(
                    "-fx-text-alignment: center; -fx-font-size: 18px; -fx-text-fill: #555; -fx-padding: 40px;");
            mensaje.setWrapText(true);

            VBox contenedor = new VBox(mensaje);
            VBox contenedorbtn = new VBox(botonImportar);
            botonImportar.setOnAction(e -> crearArchivos());
            contenedor.setStyle("-fx-alignment: center;");
            tilePanePersonajes.getChildren().add(contenedor);
            tilePanePersonajes.getChildren().add(contenedorbtn);
        }
    }

    /**
     * Actualiza los textos de los {@link CheckBox} dentro de un {@link TitledPane}.
     *
     * @param pane Contenedor que aloja los CheckBoxes.
     * @param keys Claves del {@link ResourceBundle} correspondientes a los textos.
     *
     * @author Telmo
     */
    private void actualizarCheckBoxesDelPanel(TitledPane pane, String[] keys) {
        Node content = pane.getContent();
        if (content instanceof VBox) {
            int index = 0;
            for (Node node : ((VBox) content).getChildren()) {
                if (node instanceof CheckBox && index < keys.length) {
                    ((CheckBox) node).setText(resources.getString(keys[index]));
                    index++;
                }
            }
        }
    }

    /**
     * Carga y muestra los personajes de una página específica en el TilePane.
     * Crea dinámicamente las fichas FXML para cada personaje y las posiciona.
     *
     * @param personajes Lista de personajes a mostrar en la página actual.
     * @author Nizam
     */
    private void cargarPersonajes(List<Map<String, String>> personajes) {
        if (tilePanePersonajes == null)
            return;

        tilePanePersonajes.getChildren().clear();
        listaControladores = new ArrayList<>();

        int totalPersonajesLista = personajes.size();
        if (totalPersonajesLista == 0) {
            actualizarControlesPaginacion();
            return;
        }

        int indiceInicio = (paginaActual - 1) * personajesPorPagina;

        if (indiceInicio >= totalPersonajesLista) {
            this.paginaActual = Math.max(1, totalPaginas);
            indiceInicio = (paginaActual - 1) * personajesPorPagina;
        }

        int indiceFin = Math.min(indiceInicio + personajesPorPagina, totalPersonajesLista);

        List<Map<String, String>> personajesPagina = personajes.subList(indiceInicio, indiceFin);
        logger.info("Cargando Página {}: Personajes de índice {} a {}. (Total: {})",
                paginaActual, indiceInicio, indiceFin, personajesPagina.size());

        for (Map<String, String> p : personajesPagina) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();

                String nombre = p.getOrDefault("name", "N/A");
                String casa = p.getOrDefault("house", "Desconocida");
                String imagePath = p.getOrDefault("image", "");
                String slug = p.getOrDefault("slug", UUID.randomUUID().toString());

                controller.setData(nombre, casa, imagePath);
                controller.setPersonajeSlug(slug);

                controller.setOnSelectionChanged(() -> handleSelectionChange(controller));

                if (selectionModeActive) {
                    controller.setSelectionMode(true);
                    if (selectedSlugs.contains(slug)) {
                        //controller.setSelected(true);
                    }
                }

                listaControladores.add(controller);
                tilePanePersonajes.getChildren().add(card);
            } catch (IOException e) {
                logger.error("Error al cargar la ficha del personaje", e);
            }
        }

        if (selectionModeActive) {
            actualizarEstadoBotonExportar();
        }

        actualizarControlesPaginacion();
    }

    /**
     * Alterna entre modo normal y modo selección de personajes para exportación.
     * Muestra/oculta botón Exportar y actualiza texto del botón Seleccionar.
     * Notifica a todas las fichas para activar/desactivar checkboxes.
     *
     * @author Telmo
     */
    @FXML
    private void toggleSelectionMode() {
        selectionModeActive = !selectionModeActive;

        if (btnSeleccionar != null) {
            if (selectionModeActive) {
                btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
            } else {
                btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
                selectedSlugs.clear();
            }
        }

        for (ControladorFichaPersonaje controller : listaControladores) {
            controller.setSelectionMode(selectionModeActive);
            if (!selectionModeActive){
                 //controller.setSelected(false);
            }
        }

        actualizarEstadoBotonExportar();
    }

    /**
     * Habilita o deshabilita el botón de exportar según si hay personajes
     * seleccionados.
     */
    private void actualizarEstadoBotonExportar() {
        if (btnExportar == null)
            return;

        btnExportar.setDisable(selectedSlugs.isEmpty());
    }

    private void handleSelectionChange(ControladorFichaPersonaje controller) {
        if (controller.isSelected()) {
            selectedSlugs.add(controller.getPersonajeSlug());
        } else {
            selectedSlugs.remove(controller.getPersonajeSlug());
        }
        actualizarEstadoBotonExportar();
    }

    /**
     * Exporta los personajes seleccionados en modo selección.
     * Genera un reporte PDF combinado para todos los personajes seleccionados.
     *
     * @author Telmo
     */
    @FXML
    private void exportarSeleccionados() {
        if (selectedSlugs.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "", "No hay personajes seleccionados.");
            return;
        }

        JasperReport jasperReport;
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", "No se encuentra el archivo .jrxml");
                return;
            }
            jasperReport = JasperCompileManager.compileReport(reportStream);
        } catch (Exception e) {
            logger.error("Error compilando reporte Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", "Error al cargar plantilla de reporte: " + e.getMessage());
            return;
        }

        List<JasperPrint> jasperPrints = new ArrayList<>();
        int exportados = 0;

        for (String slug : selectedSlugs) {
            Optional<Map<String, String>> datosOpt = listaPersonajesMapeados.stream()
                    .filter(map -> slug.equals(map.get("slug")))
                    .findFirst();

            if (datosOpt.isPresent()) {
                Map<String, String> p = datosOpt.get();
                try {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("Nombre", p.getOrDefault("name", ""));
                    parameters.put("Alias", p.getOrDefault("alias_names", ""));
                    parameters.put("Casa", p.getOrDefault("house", ""));
                    parameters.put("Genero", p.getOrDefault("gender", ""));
                    parameters.put("Especie", p.getOrDefault("species", ""));
                    parameters.put("Ojos", p.getOrDefault("eye_color", ""));
                    parameters.put("Pelo", p.getOrDefault("hair_color", ""));
                    parameters.put("Piel", p.getOrDefault("skin_color", ""));
                    parameters.put("Patronus", p.getOrDefault("patronus", ""));

                    InputStream imagenStream = getClass()
                            .getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
                    if (imagenStream != null) {
                        parameters.put("Imagen", imagenStream);
                    }

                    JasperPrint jasperPrint = JasperFillManager
                            .fillReport(jasperReport, parameters, new JREmptyDataSource(1));

                    jasperPrints.add(jasperPrint);
                    exportados++;

                } catch (Exception e) {
                    logger.error("Error generando reporte para {}", slug, e);
                }
            }
        }

        if (jasperPrints.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "", "No se pudo generar ningún reporte.");
            return;
        }

        try {
            JasperPrint mergedPrint = jasperPrints.get(0);

            for (int i = 1; i < jasperPrints.size(); i++) {
                JasperPrint nextPrint = jasperPrints.get(i);
                for (JRPrintPage page : nextPrint.getPages()) {
                    mergedPrint.addPage(page);
                }
            }

            JasperViewer.viewReport(mergedPrint, false);

            if (exportados > 0) {
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "", "Se han exportado " + exportados + " fichas en un único documento.");
            }

        } catch (Exception e) {
            logger.error("Error al unificar reportes", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", "Error al unificar/mostrar el reporte: " + e.getMessage());
        }
    }

    /**
     * Cambia la página actual del paginador a la especificada.
     *
     * @param nuevaPagina Número de página a mostrar (1-based index)
     * @author Nizam
     */
    public void setPaginaActual(int nuevaPagina) {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            this.paginaActual = nuevaPagina;
            cargarPersonajes(listaPersonajesMapeados);
            logger.debug("Página cambiada a {}", nuevaPagina);
        } else {
            logger.warn("Número de página {} fuera de rango (1 - {}).", nuevaPagina, totalPaginas);
        }
    }

    /**
     * Actualiza el estado visual de los controles de paginación.
     *
     * @author Erlantz
     */
    private void actualizarControlesPaginacion() {
        if (paginationContainer == null)
            return;

        paginationContainer.getChildren().clear();

        if (totalPaginas <= 1) {
            lblPaginaActual.setText("");
            return;
        }

        lblPaginaActual.setText(String.format("Pág. %d de %d", paginaActual, totalPaginas));

        if (totalPaginas <= 5) {
            for (int i = 1; i <= totalPaginas; i++) {
                agregarBotonPagina(i);
            }
            return;
        }

        agregarBotonPagina(1);

        int x, y, z;
        if (paginaActual <= 3) {
            x = 2;
            y = 3;
            z = 4;
        } else if (paginaActual >= totalPaginas - 2) {
            x = totalPaginas - 3;
            y = totalPaginas - 2;
            z = totalPaginas - 1;
        } else {
            x = paginaActual - 1;
            y = paginaActual;
            z = paginaActual + 1;
        }

        if (x > 2) {
            Label puntos = new Label("...");
            puntos.getStyleClass().add("pagination-dots");
            paginationContainer.getChildren().add(puntos);
        }

        if (x > 1 && x < totalPaginas)
            agregarBotonPagina(x);
        if (y > 1 && y < totalPaginas && y != x)
            agregarBotonPagina(y);
        if (z > 1 && z < totalPaginas && z != x && z != y)
            agregarBotonPagina(z);

        if (z < totalPaginas - 1) {
            Label puntos = new Label("...");
            puntos.getStyleClass().add("pagination-dots");
            paginationContainer.getChildren().add(puntos);
        }

        agregarBotonPagina(totalPaginas);
    }

    /**
     *
     * @param numeroPagina
     * @author Erlantz
     */
    private void agregarBotonPagina(int numeroPagina) {
        Button btn = new Button(String.valueOf(numeroPagina));
        btn.getStyleClass().addAll("page-number-button", "small-button");

        String texto = String.valueOf(numeroPagina);
        if (texto.length() == 2) {
            btn.getStyleClass().add("dos-digitos");
        } else if (texto.length() == 3) {
            btn.getStyleClass().add("tres-digitos");
        }

        if (numeroPagina == paginaActual) {
            btn.setDisable(true);
            btn.getStyleClass().add("active-page");
        } else {
            btn.setOnAction(e -> setPaginaActual(numeroPagina));
        }

        paginationContainer.getChildren().add(btn);
    }

    /**
     * Activa filtrado automático al seleccionar/des seleccionar cualquier opción.
     *
     * @author Marco
     */
    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if (resources != null && pane.getText().equals(resources.getString("filtro.titulo.casa"))) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox cb) {
                                cb.selectedProperty()
                                        .addListener((o, ov, nv) -> filtrarPersonajes());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Alterna la visibilidad del panel lateral de filtros.
     *
     * @author Telmo
     */
    @FXML
    private void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        logger.info("Panel de filtros {}", isVisible ? "ocultado" : "mostrado");
    }

    /**
     * Limpia todos los filtros activos y restaura la vista completa.
     *
     * @author Telmo
     */
    @FXML
    private void limpiarFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                Node content = pane.getContent();
                if (content instanceof VBox) {
                    for (Node node : ((VBox) content).getChildren()) {
                        if (node instanceof CheckBox) {
                            ((CheckBox) node).setSelected(false);
                        }
                    }
                }
            }
        }
        if (searchField != null)
            searchField.setText("");
        this.paginaActual = 1;
        cargarPersonajes(listaPersonajesMapeados);
    }

    /**
     * Aplica manualmente los filtros actuales del panel.
     *
     * @author Telmo
     */
    @FXML
    private void aplicarFiltros() {
        filtrarPersonajes();
    }

    /**
     * Ejecuta el algoritmo de filtrado combinado de texto + CheckBox de casas.
     *
     * @author Telmo
     */
    private void filtrarPersonajes() {
        String searchText = (searchField != null) ? searchField.getText().toLowerCase() : "";
        List<String> selectedCasas = new ArrayList<>();

        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                if (resources != null && pane.getText().equals(resources.getString("filtro.titulo.casa"))) {
                    Node content = pane.getContent();
                    if (content instanceof VBox) {
                        for (Node node : ((VBox) content).getChildren()) {
                            if (node instanceof CheckBox cb) {
                                if (cb.isSelected()) {
                                    selectedCasas.add(cb.getText());
                                }
                            }
                        }
                    }
                }
            }
        }

        List<Map<String, String>> filtrados = listaPersonajesMapeados.stream()
                .filter(p -> p.getOrDefault("name", "").toLowerCase().contains(searchText))
                .filter(p -> selectedCasas.isEmpty() || selectedCasas.contains(p.get("house")))
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: {}", filtrados.size());

        int totalFiltrados = filtrados.size();
        this.totalPaginas = (int) Math.ceil((double) totalFiltrados / personajesPorPagina);
        this.paginaActual = 1;

        cargarPersonajes(filtrados);
    }

    /**
     * Abre el formulario FXML para crear un nuevo personaje.
     *
     * @author Erlantz
     */
    @FXML
    public void onNuevo() {
        try {
            var fxmlResource = getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml");
            if (fxmlResource == null) {
                logger.error("FXML no encontrado: /es/potersitos/fxml/nuevoPersonaje.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource, this.resources);

            Parent root = loader.load();

            Scene scene = new Scene(root);

            try {
                var archivoCSS = getClass().getResource("/es/potersitos/css/estiloNuevo.css");
                if (archivoCSS != null) {
                    scene.getStylesheets().add(archivoCSS.toExternalForm());
                }
            } catch (Exception e) {
                logger.warn("Error al aplicar CSS: {}", e.getMessage());
            }

            Stage stage = new Stage();
            stage.setTitle(resources.getString("menu.archivo.nuevo"));
            stage.setScene(scene);
            stage.getIcons().add(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png")))
            );
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            logger.error("Error al abrir el formulario de nuevo personaje", e);
        }
    }

    /**
     * Ejecuta el programa externo CrearArchivosPotter.exe para generar archivos
     * CSV/XML/BIN.
     *
     * @author Erlantz
     */
    @FXML
    public void crearArchivos() {
        try {
            String userHome = System.getProperty("user.home");
            String proyectoPath = userHome + "\\Reto3_Hogwarts_Anuario";
            new File(proyectoPath).mkdirs();

            String csvPath = proyectoPath + "\\todosPersonajes.csv";
            String xmlPath = proyectoPath + "\\todosPersonajes.xml";
            String binPath = proyectoPath + "\\todosPersonajes.bin";

            String exePath = "lib\\CrearArchivosPotter.exe";
            File exeFile = new File(exePath);
            if (!exeFile.exists()) {
                mandarAlertas(Alert.AlertType.ERROR, "ERROR", "", "EXE no encontrado:\n" + exeFile.getAbsolutePath());
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(exePath, csvPath, xmlPath, binPath);
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    output.append(linea).append("\n");
                }
            }

            int exitCode = proceso.waitFor();
            logger.info("PYTHON OUTPUT: {}", output);

            boolean csvOk = new File(csvPath).exists();
            boolean xmlOk = new File(xmlPath).exists();
            boolean binOk = new File(binPath).exists();

            if (csvOk && xmlOk && binOk) {
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "",
                        "3 Archivos creados:\n" + proyectoPath);

                listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
                calcularTotalPaginas();
                cargarPersonajes(listaPersonajesMapeados);
            } else {
                mandarAlertas(Alert.AlertType.ERROR, "FALLÓ", "", String.format(
                        "ExitCode: %d\nCSV: %s\nXML: %s\nBIN: %s\n\n%s", exitCode, csvOk, xmlOk, binOk, output));
            }
        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", e.getMessage());
        }
    }

    /**
     * Exporta la lista completa de personajes visualizados.
     *
     * @author Telmo
     */
    @FXML
    public void exportarPersonajes() {
        if (listaPersonajesMapeados.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "",
                    "No hay personajes para exportar. Importa primero.");
            return;
        }

        JasperReport jasperReport;
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                        "No se encuentra el archivo .jrxml");
                return;
            }
            jasperReport = JasperCompileManager.compileReport(reportStream);
        } catch (Exception e) {
            logger.error("Error compilando reporte Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "", "Error al cargar plantilla: " + e.getMessage());
            return;
        }

        List<JasperPrint> jasperPrints = new ArrayList<>();
        int exportados = 0;

        for (Map<String, String> p : listaPersonajesMapeados) {
            try {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("Nombre", p.getOrDefault("name", ""));
                parameters.put("Alias", p.getOrDefault("alias_names", ""));
                parameters.put("Casa", p.getOrDefault("house", ""));
                parameters.put("Genero", p.getOrDefault("gender", ""));
                parameters.put("Especie", p.getOrDefault("species", ""));
                parameters.put("Ojos", p.getOrDefault("eye_color", ""));
                parameters.put("Pelo", p.getOrDefault("hair_color", ""));
                parameters.put("Piel", p.getOrDefault("skin_color", ""));
                parameters.put("Patronus", p.getOrDefault("patronus", ""));

                InputStream imagenStream = getClass()
                        .getResourceAsStream("/es/potersitos/img/persona_predeterminado.png");
                if (imagenStream != null) {
                    parameters.put("Imagen", imagenStream);
                }

                JasperPrint jasperPrint = JasperFillManager
                        .fillReport(jasperReport, parameters, new JREmptyDataSource(1));

                jasperPrints.add(jasperPrint);
                exportados++;

            } catch (Exception e) {
                logger.error("Error generando reporte para {}", p.getOrDefault("name", "desconocido"), e);
            }
        }

        if (jasperPrints.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "", "No se pudo generar ningún reporte.");
            return;
        }

        try {
            // Unifica todos los reportes en uno solo
            JasperPrint mergedPrint = jasperPrints.get(0);
            for (int i = 1; i < jasperPrints.size(); i++) {
                JasperPrint nextPrint = jasperPrints.get(i);
                for (JRPrintPage page : nextPrint.getPages()) {
                    mergedPrint.addPage(page);
                }
            }

            JasperViewer.viewReport(mergedPrint, false);
            mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "",
                    String.format("Exportados %d personajes en un único PDF.", exportados));

            logger.info("Exportados {} personajes completos", exportados);

        } catch (Exception e) {
            logger.error("Error al unificar reportes", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                    "Error al mostrar PDF: " + e.getMessage());
        }
    }

    /**
     * Abre el manual de usuario en el visor PDF predeterminado del sistema.
     *
     * @author Erlantz
     */
    @FXML
    public void documentacion() {
        try {
            InputStream input = getClass().getResourceAsStream("/es/potersitos/documentacion/manual.pdf");
            if (input == null) {
                mandarAlertas(Alert.AlertType.ERROR, "Error", null, "No se encontró manual.pdf");
                return;
            }

            File tempFile = File.createTempFile("manual", ".pdf");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                input.transferTo(out);
            }

            Desktop.getDesktop().open(tempFile);

        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, "Error", null, "No se pudo abrir el PDF: " + e.getMessage());
        }
    }

    /**
     * Muestra la ventana "Acerca de" con créditos del equipo y tecnologías usadas.
     *
     * @author Erlantz
     */
    @FXML
    public void acercaDe() {
        String mensaje = """
                RETO3 HOGWARTS ANUARIO

                Creado por:
                • Erlantz García
                • Telmo Castillo
                • Marco Muro
                • Nizam Abdel-Ghaffar

                Python + JavaFX + PyInstaller

                Ultima modificación: 19 de Diciembre de 2025

                Manual de usuario disponible en el menú "Ayuda → Documentación"
                """;
        mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("menu.ayuda.acercade"), null, mensaje);
    }

    /**
     * Cierra la ventana principal de la aplicación desde el menú Archivo → Salir.
     *
     * @author Erlantz
     */
    @FXML
    private void salir() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir");
        alert.setHeaderText("Cerrar aplicación");
        alert.setContentText("¿Deseas salir de la aplicación?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (menuBar != null && menuBar.getScene() != null) {
                Stage stage = (Stage) menuBar.getScene().getWindow();
                logger.info("Cerrando aplicación desde menú Archivo → Salir...");
                stage.close();
            } else {
                logger.error("No se pudo obtener el Stage para cerrar la ventana.");
            }
        } else {
            logger.info("Usuario canceló la salida de la aplicación.");
        }
    }

    /**
     * Muestra una alerta JavaFX con los datos proporcionados.
     *
     * @param tipo          Tipo de alerta (INFO, WARNING, ERROR...)
     * @param titulo        Título de la alerta
     * @param mensajeTitulo Encabezado del mensaje
     * @param mensaje       Contenido del mensaje
     *
     * @author Erlantz
     */
    private void mandarAlertas(Alert.AlertType tipo, String titulo, String mensajeTitulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(mensajeTitulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        logger.debug("Alerta mostrada: tipo={}, mensaje={}", tipo, mensaje);
    }
}