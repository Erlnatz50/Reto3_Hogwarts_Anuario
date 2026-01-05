import os
import pickle
from pathlib import Path
from personajes import Personaje, ServicioArchivos


def test_guardar_csv(tmp_path: Path) -> None:
    """
    Verifica que ServicioArchivos.guardar_csv crea un archivo CSV
    con los datos de los personajes y que se puede leer correctamente.

    Parámetros:
        tmp_path (Path): Fixture de pytest que proporciona un directorio temporal.

    Retorna:
        None
    """
    personajes = [Personaje(id="1", name="Harry")]

    ruta = tmp_path / "test.csv"
    ServicioArchivos.guardar_csv(personajes, ruta)

    assert os.path.exists(ruta)
    assert ruta.read_text(encoding="utf-8").startswith("id")


def test_guardar_xml(tmp_path: Path) -> None:
    """
    Verifica que ServicioArchivos.guardar_xml crea un archivo XML
    con la estructura correcta y los datos de los personajes.

    Parámetros:
        tmp_path (Path): Fixture de pytest que proporciona un directorio temporal.

    Retorna:
        None
    """
    personajes = [Personaje(id="1", name="Hermione")]

    ruta = tmp_path / "test.xml"
    ServicioArchivos.guardar_xml(personajes, ruta)

    contenido = ruta.read_text(encoding="utf-8")
    assert "<characters>" in contenido
    assert "<name>Hermione</name>" in contenido


def test_guardar_bin(tmp_path: Path) -> None:
    """
    Verifica que ServicioArchivos.guardar_bin crea un archivo binario
    que se puede leer con pickle y contiene los personajes correctos.

    Parámetros:
        tmp_path (Path): Fixture de pytest que proporciona un directorio temporal.

    Retorna:
        None
    """
    personajes = [Personaje(id="1", name="Ron")]

    ruta = tmp_path / "test.bin"
    ServicioArchivos.guardar_bin(personajes, ruta)

    with open(ruta, "rb") as f:
        data = pickle.load(f)

    assert data[0].name == "Ron"
