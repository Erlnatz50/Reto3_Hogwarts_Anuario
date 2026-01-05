from personajes import Personaje


def test_personaje_creacion() -> None:
    """
    Verifica que se puede crear un objeto Personaje con atributos básicos
    y que sus valores se asignan correctamente.

    Parámetros:
        Ninguno

    Retorna:
        None
    """
    p = Personaje(id="1", name="Harry Potter", house="Gryffindor")

    assert p.id == "1"
    assert p.name == "Harry Potter"
    assert p.house == "Gryffindor"
