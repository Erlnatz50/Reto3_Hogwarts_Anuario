# Reto3_Hogwarts_Anuario

## 📖 Descripción
Aplicación de escritorio desarrollada para el Reto3 Hogwarts que gestiona un anuario completo de alumnos de Hogwarts. Obtiene datos reales desde la [API PotterDB](https://api.potterdb.com/v1/characters) y permite:
- Descarga y almacenamiento en CSV, XML y archivo binario
- Gestión CRUD completa de alumnos
- Filtros avanzados por casa, nombre, nacionalidad, género y especie
- Visualización de imágenes como blobs
- Generación de PDFs individuales o grupales o completos
- Interfaz gráfica intuitiva con JavaFX 24

## 🎯 Objetivos del Proyecto
- ✅ Obtener datos JSON de la API PotterDB
- ✅ Gestión completa CSV (añadir/listar/buscar/eliminar)
- ✅ Conversión CSV → XML → Archivo binario
- ✅ Almacenamiento/recuperación de imágenes (blobs)
- ✅ Filtros por: casa, nombre, nacionalidad, género, especie
- ✅ Generación de PDFs (individual/grupal/completo)
- ✅ Interfaz gráfica responsive

## 📂 Estructura del Proyecto
````
src/
├── main/
│   ├── java/es/potersitos/
│   │   ├── App.java (Aplicación JavaFX)
│   │   ├── Lanzador.java (Clase Lanzadora)
│   │   ├── main.py (crea un ejecutable)
│   │   ├── modelos/ (Personaje.py)
│   │   ├── util/ (PersonajeCSVManager.java)
│   │   ├── servicios/ (ServicioArchivos.py)
│   │   └── controladores/ (ControladorFichaPersonaje.java, ControladorVisualizarPersonajes.java, ControladorNuevoPersonaje.java, ControladorDatos.java)
│   └── resources/es/potersitos/
│   │   ├── fxml/ (visualizarPersonajes.fxml, fichaPersonaje.fxml, nuevoPersonaje.fxml, ventanaDatos.fxml)
│   │   ├── css/ (estilo.css, estiloDatos.css, estiloNuevo.css)
│   │   ├── img/ (imagenes que vayamos a usar)
│   │   ├── jasper/ (ficha_personaje.jrxml)
│   │   ├── META-INF/ (MANIFEST.MF)
│   │   ├── mensajes/ (mensaje_es.properties, mensaje_eu.properties, mensaje_en.properties)
````

## ✨ Funcionalidades Principales
- 📥 Descarga API PotterDB → Todos los personajes de Hogwarts
- 📊 Gestión CSV → Añadir/editar/eliminar/buscar alumnos
- 🔍 Filtros Inteligentes → Casa (Gryffindor, Slytherin...), nombre, nacionalidad (Francés, Búlgaro...), género (Masculino, Femenino), especie (Humano, Elfo...)
- 📄 Conversión Múltiple → CSV → XML → Binario
- 🖼️ Imágenes Blobs → Visualización y almacenamiento de fotos
- 📄 Generación PDF → Anuario completo o grupales o fichas individuales
- 🌙 Tema Hogwarts → Estilos CSS personalizados
- 🔄 Internacionalización → Español, Inglés y Euskera


## 🚀 Instalación y ejecución
Requisitos
- JDK 24 (OpenJDK recomendado)
- Maven 3.9+
- IntelliJ IDEA (recomendado)

1. Clonar
````
git clone https://github.com/Erlnatz50/Reto3_Hogwarts_Anuario.git
````
2. Ejecutar
````
# Opción 1: Maven JavaFX Plugin
mvn javafx:run

# Opción 2: JAR ejecutable
java -jar target/Reto3_Hogwarts_Anuario-1.0.0-SNAPSHOT.jar

# Opción 3: IDE 
Ejecutar es.potersitos.Lanzador
````

## ✨ Autores
- 👤 Telmo Castillo
- 👤 Nizam Abdel-Ghaffar
- 👤 Marco Muro
- 👤 Erlantz García
