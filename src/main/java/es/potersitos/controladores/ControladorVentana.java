package es.potersitos.controladores;

import es.potersitos.dao.UsuariosDAO;
import es.potersitos.modelos.Usuarios;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image; // Aunque no se usa Image, se mantiene el import para el tipo
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ControladorVentana {

    private static final Logger logger = LoggerFactory.getLogger(ControladorVentana.class);
    private UsuariosDAO usuariosDAO;

    // --- VARIABLES DE PAGINACIÓN ---
    private static final int ITEMS_PER_PAGE = 24;
    private int currentPage = 1;
    private List<Usuarios> allUsuarios = new ArrayList<>();

    // --- ELEMENTOS INYECTADOS CON @FXML ---
    @FXML private BorderPane rootBorderPane;
    @FXML private GridPane gridUsuarios;
    @FXML private TextField campoBusqueda;
    @FXML private Button botonAbrirFiltros;
    @FXML private HBox paginacionHBox;
    @FXML private Label estadoLabel;
    @FXML private Button botonSeleccionarTodo;
    @FXML private Button botonExportar;

    // Elementos de barra_filtros.fxml
    @FXML private VBox filtroVBox;
    @FXML private Button botonCerrarFiltros;

    @FXML
    public void initialize() {
        this.usuariosDAO = new UsuariosDAO();
        logger.debug("Controlador inicializado. DAO creado.");
        estadoLabel.setText("Pulsa SELECCIONAR TODO para cargar usuarios.");

        cargarTodosYRenderizarPagina();
    }

    // --- MÉTODOS DE CONTROL DE FILTRO ---

    @FXML
    public void manejarAbrirFiltros() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/potersitos/fxml/barra_filtros.fxml"));
            VBox filtroPane = loader.load();

            if (rootBorderPane != null) {
                rootBorderPane.setRight(filtroPane);
            }

            botonAbrirFiltros.setVisible(false);

        } catch (IOException e) {
            logger.error("Error al cargar la barra de filtros (barra_filtros.fxml no encontrado o error en el FXML): {}", e.getMessage(), e);
            estadoLabel.setText("Error al cargar la barra de filtros.");
        }
    }

    @FXML
    public void manejarCerrarFiltros() {
        logger.info("Bot\u00f3n 'X' pulsado. Ocultando la barra de filtros.");

        if (rootBorderPane != null) {
            rootBorderPane.setRight(null);
        }

        botonAbrirFiltros.setVisible(true);
    }

    // --- LÓGICA DE DATOS Y RENDERIZADO (Diseño Simplificado) ---

    private void cargarTodosYRenderizarPagina() {
        try {
            this.allUsuarios = usuariosDAO.obtenerTodos();
            this.currentPage = 1;
            renderizarPaginaActual();
        } catch (Exception e) {
            logger.error("Error al cargar usuarios al iniciar: {}", e.getMessage(), e);
            estadoLabel.setText("ERROR: No se pudo conectar a la API o al CSV.");
        }
    }

    private void renderizarPaginaActual() {
        if (allUsuarios.isEmpty()) {
            gridUsuarios.getChildren().clear();
            estadoLabel.setText("No hay usuarios cargados (API/CSV vac\u00edo).");
            actualizarBotonesPaginacion(0);
            return;
        }

        int totalItems = allUsuarios.size();
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        List<Usuarios> usuariosPagina = allUsuarios.subList(startIndex, endIndex);

        renderizarUsuarios(usuariosPagina);

        estadoLabel.setText(String.format("Usuarios totales: %d | P\u00e1gina %d (Mostrando %d - %d)",
                totalItems, currentPage, startIndex + 1, endIndex));

        actualizarBotonesPaginacion(totalItems);
    }

    /**
     * M\u00e9todo auxiliar para generar la tarjeta visual (Muestra \u00fanicamente Placeholder y Nombre).
     */
    private VBox crearTarjetaUsuario(Usuarios usuario) {
        VBox tarjeta = new VBox(5);
        tarjeta.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        tarjeta.setPrefSize(150, 200);
        tarjeta.setPadding(new Insets(10));
        tarjeta.setStyle("-fx-border-color: #333; -fx-border-width: 1px; -fx-background-color: #f0f0f0;");

        // 1. Placeholder de Imagen
        VBox placeholder = new VBox();
        placeholder.setPrefSize(120, 120);
        placeholder.setStyle("-fx-background-color: #cccccc; -fx-border-color: #aaa;");
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        Label imgLabel = new Label("NO IMG");
        imgLabel.setStyle("-fx-font-size: 10px;");
        placeholder.getChildren().add(imgLabel);

        // 2. Nombre del usuario
        Label nombreLabel = new Label(usuario.getName());
        nombreLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");

        // [ELIMINADO: La l\u00f3gica de 'Casa' ya no est\u00e1 aqu\u00ed]

        // A\u00f1adimos solo el placeholder y el nombre a la tarjeta
        tarjeta.getChildren().addAll(placeholder, nombreLabel);
        return tarjeta;
    }

    private void renderizarUsuarios(List<Usuarios> usuarios) {
        gridUsuarios.getChildren().clear();
        int columna = 0;
        int fila = 0;
        final int COLUMNAS_POR_FILA = 4;

        for (Usuarios usuario : usuarios) {
            VBox tarjeta = crearTarjetaUsuario(usuario);
            GridPane.setMargin(tarjeta, new Insets(5));
            gridUsuarios.add(tarjeta, columna, fila);

            columna++;
            if (columna >= COLUMNAS_POR_FILA) {
                columna = 0;
                fila++;
            }
        }
    }

    private void actualizarBotonesPaginacion(int totalItems) {
        paginacionHBox.getChildren().clear();
        if (totalItems == 0) return;

        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        Button prevButton = new Button("Anterior");
        prevButton.setDisable(currentPage == 1);
        prevButton.setOnAction(e -> goToPage(currentPage - 1));

        Button nextButton = new Button("Siguiente");
        nextButton.setDisable(currentPage == totalPages);
        nextButton.setOnAction(e -> goToPage(currentPage + 1));

        paginacionHBox.getChildren().add(prevButton);

        Label pageInfo = new Label(String.format("P\u00e1gina %d de %d", currentPage, totalPages));
        pageInfo.setPadding(new Insets(0, 10, 0, 10));
        paginacionHBox.getChildren().add(pageInfo);

        paginacionHBox.getChildren().add(nextButton);
    }

    private void goToPage(int page) {
        int totalPages = (int) Math.ceil((double) allUsuarios.size() / ITEMS_PER_PAGE);
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
            renderizarPaginaActual();
        }
    }

    // --- MÉTODOS DE ACCIÓN ---

    @FXML
    public void manejarBotonPulsado() {
        logger.info("El bot\u00f3n 'SELECCIONAR TODO' ha sido pulsado. Forzando recarga de la API.");
        botonSeleccionarTodo.setDisable(true);
        estadoLabel.setText("Recargando datos desde la API...");

        cargarTodosYRenderizarPagina();

        botonSeleccionarTodo.setDisable(false);
    }

    @FXML
    public void manejarBotonExportar() {
        logger.info("El bot\u00f3n 'EXPORTAR' ha sido pulsado.");
        estadoLabel.setText("Exportando usuarios a archivo...");

        try {
            Thread.sleep(500);

            estadoLabel.setText("Exportaci\u00f3n finalizada con \u00e9xito.");
            logger.info("Exportaci\u00f3n terminada.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            estadoLabel.setText("Exportaci\u00f3n cancelada.");
        } catch (Exception e) {
            logger.error("Error durante la exportaci\u00f3n: {}", e.getMessage());
            estadoLabel.setText("Error: Fallo al exportar datos.");
        }
    }
}