package es.potersitos.controladores;

import es.potersitos.util.PersonajeCSVManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private Button btnFiltrar, btnSeleccionar, btnExportar, btnLimpiarFiltro,
            botonImportar, btnEliminarSeleccionados;

    /** Etiqueta que muestra la página actual del paginador. */
    @FXML
    private Label lblPaginaActual;

    /** Barra de menú principal. */
    @FXML
    private MenuBar menuBar;

    /** Elementos del menú superior. */
    @FXML
    private MenuItem menuSalir, menuNuevo, menuGuardar, menuAcercaDe, menuManual, menuEuskera, menuIngles, menuEspaniol,
            menuVideoManual;

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

    /** Contenedor de botones de paginación. */
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
    private static final int personajesPorPagina = 12;

    /** Página activa actual. */
    private int paginaActual = 1;

    /** Total de páginas calculadas con los personajes disponibles. */
    private int totalPaginas;

    /** Mensaje de error si no hay personajes importados. */
    private Label mensaje;

    private boolean importando = false;

    /** Indica si el modo selección está activo. */
    private boolean selectionModeActive = false;

    /** Conjunto de identificadores “slug” de los personajes seleccionados. */
    private final Set<String> selectedSlugs = new HashSet<>();

    /** Ruta local donde se buscan imágenes de personajes */
    private static final String RUTA_LOCAL_IMAGENES = System.getProperty("user.home") + File.separator
            + "Reto3_Hogwarts_Anuario" + File.separator + "imagenes" + File.separator;

    /**
     * Metodo de inicialización automática FXML.
     *
     * @author Telmo
     */
    @FXML
    public void initialize() {
        resources = ResourceBundle.getBundle("es.potersitos.mensaje", Locale.getDefault());
        botonImportar = new Button("");
        mensaje = new Label("");
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
        menuAcercaDe.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
        menuManual.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
        menuVideoManual.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));
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
     * Activa filtrado automático al seleccionar/des seleccionar cualquier opción.
     *
     * @author Marco
     */
    private void configurarListenersFiltros() {
        if (accordionFiltros != null) {
            for (TitledPane pane : accordionFiltros.getPanes()) {
                Node content = pane.getContent();
                if (content instanceof VBox) {
                    for (Node node : ((VBox) content).getChildren()) {
                        if (node instanceof CheckBox cb) {
                            cb.selectedProperty().addListener((o, ov, nv) -> filtrarPersonajes());
                        }
                    }
                }
            }
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
            if (listaPersonajesMapeados.isEmpty()) {
                mostrarMensajeImportar();
            } else {
                cargarPersonajes(listaPersonajesMapeados);
            }
            filtrarPersonajes();
            logger.info("Idioma cambiado a: {}", nuevoLocale);
        } catch (Exception e) {
            logger.error("Error cambiando idioma", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null,
                    resources.getString("error.cambiar.idioma.mensaje") + e.getMessage());
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
        menuAcercaDe.setText(resources.getString("menu.ayuda.acercade"));
        menuManual.setText(resources.getString("menu.ayuda.documentacion"));
        menuEuskera.setText(resources.getString("menu.idioma.euskera"));
        menuIngles.setText(resources.getString("menu.idioma.ingles"));
        menuEspaniol.setText(resources.getString("menu.idioma.espanol"));
        menuVideoManual.setText(resources.getString("menu.video.tutorial"));
        botonImportar.setText(resources.getString("importar"));
        searchField.setPromptText(resources.getString("visualizar.search.prompt"));
        btnFiltrar.setText(resources.getString("visualizar.filtro.titulo"));
        btnExportar.setText(resources.getString("visualizar.btn.exportar"));
        btnEliminarSeleccionados.setText(resources.getString("eliminar.button").toUpperCase());
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

            mensaje.setText(resources.getString("no.se.encontraron.personajes"));
            mensaje.setStyle(
                    "-fx-text-fill: black; -fx-text-alignment: center; -fx-font-size: 18px; -fx-padding: 40px;");
            mensaje.setWrapText(true);

            VBox contenedor = new VBox(mensaje);
            VBox contenedorBtn = new VBox(botonImportar);
            botonImportar.setOnAction(e -> crearArchivos());

            HBox contenedorCentral = new HBox(contenedorBtn);
            contenedorCentral.setAlignment(Pos.CENTER);
            contenedorCentral.setSpacing(10);

            contenedor.setStyle("-fx-alignment: center;");
            contenedorCentral.setStyle("-fx-alignment: center;");

            tilePanePersonajes.getChildren().addAll(contenedor, contenedorCentral);
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

        int totalPersonajes = personajes.size();
        if (totalPersonajes == 0) {
            actualizarControlesPaginacion();
            return;
        }

        int indiceInicio = (paginaActual - 1) * personajesPorPagina;
        if (indiceInicio >= totalPersonajes) {
            paginaActual = Math.max(1, totalPaginas);
            indiceInicio = (paginaActual - 1) * personajesPorPagina;
        }

        int indiceFin = Math.min(indiceInicio + personajesPorPagina, totalPersonajes);
        List<Map<String, String>> personajesPagina = personajes.subList(indiceInicio, indiceFin);

        logger.info("Cargando Página {}: Personajes de índice {} a {}. (Total: {})",
                paginaActual, indiceInicio, indiceFin, personajesPagina.size());

        for (Map<String, String> p : personajesPagina) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/fichaPersonaje.fxml"));
                loader.setResources(resources);
                VBox card = loader.load();

                ControladorFichaPersonaje controller = loader.getController();

                String slug = p.getOrDefault("slug", "");
                controller.setPersonajeSlug(slug);

                String nombre = p.getOrDefault("name", "N/A");
                String casa = p.getOrDefault("house", "Desconocida");

                String imagenArchivo = p.getOrDefault("image", "");

                controller.setData(nombre, casa, imagenArchivo);

                controller.setOnRefreshListener(this::recargarListaCompleta);
                controller.setOnSelectionChanged(() -> handleSelectionChange(controller));

                if (selectionModeActive) {
                    controller.setSelectionMode(true);
                    if (selectedSlugs.contains(slug)) {
                        controller.setSelected(true);
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
        if (listaPersonajesMapeados == null || listaPersonajesMapeados.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), null,
                    resources.getString("no.importado.alerta.mensaje"));
            return;
        }

        selectionModeActive = !selectionModeActive;

        if (btnSeleccionar != null) {
            if (selectionModeActive) {
                btnSeleccionar.setText(resources.getString("cancelar.button").toUpperCase());
            } else {
                btnSeleccionar.setText(resources.getString("visualizar.btn.seleccionar"));
            }
        }

        for (ControladorFichaPersonaje controller : listaControladores) {
            controller.setSelectionMode(selectionModeActive);
            if (!selectionModeActive) {
                controller.setSelected(false);
            }
        }

        actualizarEstadoBotonExportar();
    }

    /**
     * Habilita o deshabilita el botón de exportar según si hay personajes
     * seleccionados.
     *
     * @author Telmo
     */
    private void actualizarEstadoBotonExportar() {
        if (btnExportar != null)
            btnExportar.setDisable(selectedSlugs.isEmpty());
        if (btnEliminarSeleccionados != null)
            btnEliminarSeleccionados.setDisable(selectedSlugs.isEmpty());
    }

    /**
     * Gestiona los cambios en la selección de personajes.
     *
     * @param controller Instancia del controlador {@link ControladorFichaPersonaje}
     *                   cuyo estado de selección ha cambiado.
     * @author Telmo
     */
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
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "",
                    resources.getString("no.personaje.select"));
            return;
        }

        JasperReport jasperReport;
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                        resources.getString("no.encuentra.jrxml"));
                return;
            }
            jasperReport = JasperCompileManager.compileReport(reportStream);
        } catch (Exception e) {
            logger.error("Error compilando reporte Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                    resources.getString("error.cargar.plantilla.reporte") + " " + e.getMessage());
            return;
        }

        List<JasperPrint> jasperPrints = new ArrayList<>();
        int exportados = 0;

        for (String slug : selectedSlugs) {
            Optional<Map<String, String>> datosOpt = listaPersonajesMapeados.stream()
                    .filter(map -> slug.equals(map.get("slug"))).findFirst();

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

                    parameters.put("Imagen", obtenerStreamImagen(p));

                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,
                            new JREmptyDataSource(1));

                    jasperPrints.add(jasperPrint);
                    exportados++;

                } catch (Exception e) {
                    logger.error("Error generando reporte para {}", slug, e);
                }
            }
        }

        if (jasperPrints.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "",
                    resources.getString("no.generar.ningun.reporte"));
            return;
        }

        try {
            JasperPrint mergedPrint = jasperPrints.getFirst();

            for (int i = 1; i < jasperPrints.size(); i++) {
                JasperPrint nextPrint = jasperPrints.get(i);
                for (JRPrintPage page : nextPrint.getPages()) {
                    mergedPrint.addPage(page);
                }
            }

            JasperViewer.viewReport(mergedPrint, false);

            if (exportados > 0) {
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "",
                        resources.getString("se.han.exportado") + " " + exportados + " "
                                + resources.getString("fichas.en.documento"));
            }

        } catch (Exception e) {
            logger.error("Error al unificar reportes", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                    resources.getString("error.mostrar.reporte") + " " + e.getMessage());
        }
    }

    /**
     * Elimina los personajes seleccionados de la lista y del CSV.
     * Muestra alerta de confirmación antes de proceder.
     *
     * @author Erlantz
     */
    @FXML
    private void eliminarSeleccionados() {
        if (selectedSlugs.isEmpty())
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("eliminar.confirm.titulo"));
        alert.setHeaderText(null);
        alert.setContentText(resources.getString("seguro.eliminar") + " " + selectedSlugs.size() + " "
                + resources.getString("personajes.seleccionados"));

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean algunError = false;
            int eliminados = 0;

            Set<String> slugsParaBorrar = new HashSet<>(selectedSlugs);

            for (String slug : slugsParaBorrar) {
                if (PersonajeCSVManager.eliminarPersonajePorSlug(slug)) {
                    eliminados++;
                } else {
                    algunError = true;
                }
            }

            if (eliminados > 0) {
                selectedSlugs.clear();
                if (selectionModeActive) {
                    actualizarEstadoBotonExportar();
                }
                recargarListaCompleta();

                String msg = resources.getString("se.han.eliminado") + " " + eliminados + " "
                        + resources.getString("personaje.correctamente");
                if (algunError)
                    msg += "\n" + resources.getString("algunos.personajes.no.se.comprobaron");
                mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "", msg);
            } else {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                        resources.getString("no.se.pudo.eliminar.ninguno"));
            }
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
            paginaActual = nuevaPagina;
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
     * Crea y agrega dinámicamente un botón de paginación para una página
     * específica.
     *
     * @param numeroPagina Número de página que representa este botón. Debe estar
     *                     dentro del rango válido de paginación.
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
        paginaActual = 1;
        cargarPersonajes(listaPersonajesMapeados);
    }

    /**
     * Ejecuta el algoritmo de filtrado combinado de texto (Nombre) + CheckBox de
     * múltiples
     * categorías.
     *
     * @author Telmo
     */
    private void filtrarPersonajes() {
        if (listaPersonajesMapeados == null || listaPersonajesMapeados.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), null,
                    resources.getString("no.importado.alerta.mensaje"));
            return;
        }

        String rawText = (searchField != null) ? searchField.getText() : "";
        String searchText = rawText.toLowerCase().trim();

        Set<Integer> selectedHousesIndices = new HashSet<>();
        Set<Integer> selectedNationalityIndices = new HashSet<>();
        Set<Integer> selectedSpeciesIndices = new HashSet<>();
        Set<Integer> selectedGenderIndices = new HashSet<>();

        if (accordionFiltros != null) {
            List<TitledPane> panes = accordionFiltros.getPanes();
            for (int i = 0; i < panes.size(); i++) {
                TitledPane pane = panes.get(i);
                Node content = pane.getContent();
                if (content instanceof VBox) {
                    int checkBoxIndex = 0;
                    for (Node node : ((VBox) content).getChildren()) {
                        if (node instanceof CheckBox cb && cb.isSelected()) {
                            switch (i) {
                                case 0 -> selectedHousesIndices.add(checkBoxIndex);
                                case 1 -> selectedNationalityIndices.add(checkBoxIndex);
                                case 2 -> selectedSpeciesIndices.add(checkBoxIndex);
                                case 3 -> selectedGenderIndices.add(checkBoxIndex);
                            }
                        }
                        if (node instanceof CheckBox)
                            checkBoxIndex++;
                    }
                }
            }
        }

        List<Map<String, String>> filtrados = listaPersonajesMapeados.stream()
                .filter(p -> {
                    String nombrePersonaje = p.getOrDefault("name", "").toLowerCase();

                    if (!searchText.isEmpty() && !nombrePersonaje.contains(searchText)) {
                        return false;
                    }

                    if (!selectedHousesIndices.isEmpty()) {
                        String house = p.getOrDefault("house", "").toLowerCase();
                        boolean match = isMatch(selectedHousesIndices, house.contains("gryffindor"),
                                house.contains("slytherin"), house.contains("hufflepuff"), house.contains("ravenclaw"));
                        if (!match)
                            return false;
                    }

                    if (!selectedNationalityIndices.isEmpty()) {
                        String nac = p.getOrDefault("nationality", "").toLowerCase();
                        boolean match = isMatch(selectedNationalityIndices,
                                (nac.contains("brit") || nac.contains("kingdom") || nac.contains("uk")
                                        || nac.contains("scot") || nac.contains("eng")),
                                (nac.contains("irish") || nac.contains("ireland")),
                                (nac.contains("french") || nac.contains("france")),
                                (nac.contains("bulgar") || nac.contains("bulgaria")));
                        if (!match)
                            return false;
                    }

                    if (!selectedSpeciesIndices.isEmpty()) {
                        String species = p.getOrDefault("species", "").toLowerCase();
                        boolean match = isMatch(selectedSpeciesIndices, species.equals("human"),
                                (species.contains("half") || species.contains("mixed")), (species.contains("elf")),
                                (species.contains("giant")));
                        if (!match)
                            return false;
                    }

                    if (!selectedGenderIndices.isEmpty()) {
                        String gender = p.getOrDefault("gender", "").toLowerCase();
                        boolean match = selectedGenderIndices.contains(0) && gender.equals("male");
                        if (selectedGenderIndices.contains(1) && gender.equals("female"))
                            match = true;
                        return match;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        logger.debug("Filtro aplicado. Coincidencias encontradas: {}", filtrados.size());

        int totalFiltrados = filtrados.size();
        totalPaginas = (int) Math.ceil((double) totalFiltrados / personajesPorPagina);
        paginaActual = 1;

        cargarPersonajes(filtrados);
    }

    private static boolean isMatch(Set<Integer> selectedSpeciesIndices, boolean species, boolean species1,
            boolean species2, boolean species3) {
        boolean match = selectedSpeciesIndices.contains(0) && species;
        if (selectedSpeciesIndices.contains(1) && species1) {
            match = true;
        }
        if (selectedSpeciesIndices.contains(2) && species2) {
            match = true;
        }
        if (selectedSpeciesIndices.contains(3) && species3) {
            match = true;
        }
        return match;
    }

    /**
     * Recarga toda la lista desde CSV y replica filtros.
     * Usado cuando se elimina un personaje desde la ficha.
     */
    private void recargarListaCompleta() {
        logger.info("Recargando lista completa de personajes desde disco...");
        listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
        filtrarPersonajes();
    }

    /**
     * Abre el formulario FXML para crear un nuevo personaje.
     *
     * @author Erlantz
     */
    @FXML
    public void onNuevo() {
        if (listaPersonajesMapeados == null || listaPersonajesMapeados.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), null,
                    resources.getString("no.importado.alerta.mensaje"));
            return;
        }

        try {
            var fxmlResource = getClass().getResource("/es/potersitos/fxml/nuevoPersonaje.fxml");
            if (fxmlResource == null) {
                logger.error("FXML no encontrado: /es/potersitos/fxml/nuevoPersonaje.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource, resources);
            Parent root = loader.load();

            ControladorNuevoPersonaje controller = loader.getController();
            controller.setOnPersonajeGuardado(() -> {
                listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
                limpiarFiltros();
                calcularTotalPaginas();
                setPaginaActual(totalPaginas);
            });

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

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
            stage.initStyle(StageStyle.TRANSPARENT);

            try {
                stage.getIcons().add(new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));
            } catch (Exception e) {
                logger.warn("No se pudo cargar el icono de la ventana");
            }

            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            logger.error("Error al abrir el formulario de nuevo personaje", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"),
                    resources.getString("fallo.abrir.ventana"), e.getMessage());
        }
    }

    /**
     * Crear los archivos CSV, XML y binario.
     * Crear la carpeta imagenes con la imagen de cada personaje.
     *
     * @author Erlantz
     */
    @FXML
    public void crearArchivos() {
        if (importando)
            return;
        importando = true;

        botonImportar.setDisable(true);
        mensaje.setText(resources.getString("procesando.espera"));
        mensaje.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                String userHome = System.getProperty("user.home");
                String proyectoPath = userHome + File.separator + "Reto3_Hogwarts_Anuario";
                new File(proyectoPath).mkdirs();

                String csvPath = proyectoPath + File.separator + "todosPersonajes.csv";
                String xmlPath = proyectoPath + File.separator + "todosPersonajes.xml";
                String binPath = proyectoPath + File.separator + "todosPersonajes.bin";

                String exePath = new File("lib/CrearArchivosPersonajes.exe").exists()
                        ? "lib/CrearArchivosPersonajes.exe"
                        : "../lib/CrearArchivosPersonajes.exe";

                File exeFile = new File(exePath);
                if (!exeFile.exists()) {
                    Platform.runLater(() -> mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                            resources.getString("exe.no.encontrado")));
                    return null;
                }

                try {
                    ProcessBuilder pb = new ProcessBuilder(exePath, csvPath, xmlPath, binPath);
                    pb.redirectErrorStream(true);
                    Process proceso = pb.start();
                    proceso.waitFor();
                } catch (Exception e) {
                    logger.error("Error ejecutando el EXE", e);
                    Platform.runLater(() -> mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                            e.getMessage()));
                }

                return null;
            }
        };

        task.setOnSucceeded(e -> finalizarImportacion());
        task.setOnFailed(e -> finalizarImportacion());

        new Thread(task).start();
    }

    /**
     * Finaliza el proceso de importación de personajes.
     *
     * @author Erlantz
     */
    private void finalizarImportacion() {
        importando = false;
        botonImportar.setDisable(false);
        mensaje.setVisible(false);

        listaPersonajesMapeados = PersonajeCSVManager.leerTodosLosPersonajes();
        calcularTotalPaginas();
        cargarPersonajes(listaPersonajesMapeados);
    }

    /**
     * Exporta la lista completa de personajes visualizados.
     *
     * @author Telmo
     */
    @FXML
    public void exportarPersonajes() {
        if (listaPersonajesMapeados == null || listaPersonajesMapeados.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), null,
                    resources.getString("no.importado.alerta.mensaje"));
            return;
        }

        JasperReport jasperReport;
        try (InputStream reportStream = getClass().getResourceAsStream("/es/potersitos/jasper/ficha_personaje.jrxml")) {
            if (reportStream == null) {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                        resources.getString("no.encuentra.jrxml"));
                return;
            }
            jasperReport = JasperCompileManager.compileReport(reportStream);
        } catch (Exception e) {
            logger.error("Error compilando reporte Jasper", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                    resources.getString("error.cargar.plantilla.reporte") + " " + e.getMessage());
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

                parameters.put("Imagen", obtenerStreamImagen(p));

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,
                        new JREmptyDataSource(1));

                jasperPrints.add(jasperPrint);
                exportados++;

            } catch (Exception e) {
                logger.error("Error generando reporte para {}", p.getOrDefault("name", "desconocido"), e);
            }
        }

        if (jasperPrints.isEmpty()) {
            mandarAlertas(Alert.AlertType.WARNING, resources.getString("advertencia"), "",
                    resources.getString("no.generar.ningun.reporte"));
            return;
        }

        try {
            JasperPrint mergedPrint = jasperPrints.getFirst();
            for (int i = 1; i < jasperPrints.size(); i++) {
                JasperPrint nextPrint = jasperPrints.get(i);
                for (JRPrintPage page : nextPrint.getPages()) {
                    mergedPrint.addPage(page);
                }
            }

            JasperViewer.viewReport(mergedPrint, false);
            mandarAlertas(Alert.AlertType.INFORMATION, resources.getString("exito"), "",
                    resources.getString("exportados") + " " + exportados + " "
                            + resources.getString("personajes.unico.pdf"));

            logger.info("Exportados {} personajes completos", exportados);

        } catch (Exception e) {
            logger.error("Error al unificar reportes", e);
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), "",
                    resources.getString("error.mostrar.pdf") + " " + e.getMessage());
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
            Path manualPath;
            Path path = Paths.get("docs/Manual de usuario - Anuario Hogwarts.pdf");
            if (Files.exists(path)) {
                manualPath = path;
            } else {
                manualPath = Paths.get("../docs/Manual de usuario - Anuario Hogwarts.pdf");
            }

            if (!Files.exists(manualPath)) {
                mandarAlertas(Alert.AlertType.ERROR, "Error", null,
                        resources.getString("no.se.encontro.manual") + " " + manualPath);
                return;
            }

            Desktop.getDesktop().open(manualPath.toFile());

        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, "Error", null,
                    resources.getString("no.se.puede.abrir.pdf") + " " + e.getMessage());
        }
    }

    /**
     * Abre el videotutorial del manual usando el reproductor del sistema.
     *
     * @author Erlantz
     */
    @FXML
    public void videoManual() {
        try {
            Path videoPath;
            Path path = Paths.get("docs/Video Tutorial - Anuario Hogwarts.mp4");
            if (Files.exists(path)) {
                videoPath = path;
            } else {
                videoPath = Paths.get("../docs/Video Tutorial - Anuario Hogwarts.mp4");
            }

            if (!Files.exists(videoPath)) {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null,
                        resources.getString("no.se.encuentra.videoTutorial"));
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null,
                        resources.getString("no.soporta.apertura.videoTutorial"));
                return;
            }

            Desktop.getDesktop().open(videoPath.toFile());

        } catch (Exception e) {
            mandarAlertas(Alert.AlertType.ERROR, resources.getString("error"), null,
                    resources.getString("no.se.puede.abrir.videoTutorial") + " " + e.getMessage());
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
        alert.setTitle(resources.getString("salir"));
        alert.setHeaderText(resources.getString("cerrar.aplicacion"));
        alert.setContentText(resources.getString("deseas.salir.aplicacion"));
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));

        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (menuBar != null && menuBar.getScene() != null) {
                Stage mainStage = (Stage) menuBar.getScene().getWindow();
                logger.info("Cerrando aplicación desde menú Archivo → Salir...");
                mainStage.close();
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

        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/es/potersitos/img/icono-app.png"))));

        alerta.showAndWait();
        logger.debug("Alerta mostrada: tipo={}, mensaje={}", tipo, mensaje);
    }

    /**
     * Obtiene un InputStream de la imagen del personaje.
     * Prioriza archivos locales y usa una imagen por defecto si no existe.
     *
     * @param p Mapa con los datos del personaje.
     * @return {@link InputStream} de la imagen.
     */
    private InputStream obtenerStreamImagen(Map<String, String> p) {
        if (p == null)
            return imagenPorDefecto();

        String slug = p.getOrDefault("slug", "").trim();
        String nombreImagen = p.getOrDefault("image", "").trim();

        if (!nombreImagen.isEmpty()) {
            Path ruta = Paths.get(RUTA_LOCAL_IMAGENES, nombreImagen);
            if (Files.exists(ruta)) {
                try {
                    return Files.newInputStream(ruta);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!slug.isEmpty()) {
            String[] extensiones = { ".jpg", ".png", ".jpeg", ".JPG", ".PNG", ".JPEG" };
            for (String ext : extensiones) {
                Path ruta = Paths.get(RUTA_LOCAL_IMAGENES, slug + ext);
                if (Files.exists(ruta)) {
                    try {
                        return Files.newInputStream(ruta);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return imagenPorDefecto();
    }

    private InputStream imagenPorDefecto() {
        return getClass().getResourceAsStream(
                "/es/potersitos/img/persona_predeterminado.png");
    }
}