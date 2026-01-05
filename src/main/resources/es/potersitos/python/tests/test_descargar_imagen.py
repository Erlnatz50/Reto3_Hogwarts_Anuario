from pathlib import Path
from personajes import descargar_imagen
from unittest.mock import patch, Mock


def test_descargar_imagen_ok(tmp_path: Path) -> None:
    """
    Verifica que descargar_imagen guarda correctamente una imagen en la carpeta
    local cuando se le proporciona una URL válida y un slug.

    Parámetros:
        tmp_path (Path): Fixture de pytest que proporciona un directorio temporal.

    Retorna:
        None
    """
    fake_response = Mock()
    fake_response.status_code = 200
    fake_response.content = b"fake image content"

    with patch("personajes.os.path.expanduser", return_value=str(tmp_path)):
        with patch("personajes.requests.get", return_value=fake_response):
            nombre = descargar_imagen("http://example.com/image.jpg", "harry-potter")

    assert nombre == "harry-potter.jpg"

    ruta_imagen = tmp_path / "Reto3_Hogwarts_Anuario" / "imagenes" / "harry-potter.jpg"

    assert ruta_imagen.exists()


def test_descargar_imagen_parametros_invalidos() -> None:
    """
    Verifica que descargar_imagen devuelve una cadena vacía
    cuando se le pasan parámetros inválidos.

    Parámetros:
        Ninguno

    Retorna:
        None
    """
    assert descargar_imagen("", "slug") == ""
    assert descargar_imagen("http://example.com/a.jpg", "") == ""
