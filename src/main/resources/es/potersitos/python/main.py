import sys
import os
import requests
import csv
import pickle
import xml.etree.ElementTree as element_tree
import json


class Personaje:
    """
    Representa a un personaje con múltiples atributos que provienen de la API.

    Atributos:
        id (str): Identificador único del personaje.
        type (str): Tipo de recurso.
        slug (str): Slug descriptivo.
        name (str): Nombre del personaje.
        alias_names (list[str]): Nombres alternativos o alias.
        animagus (str | None): Información de animagus.
        blood_status (str | None): Estado de sangre.
        boggart (str | None): Boggart asociado.
        born (str | None): Fecha de nacimiento.
        died (str | None): Fecha de fallecimiento.
        eye_color (str | None): Color de ojos.
        family_members (list[str]): Miembros de la familia.
        gender (str | None): Género.
        hair_color (str | None): Color de cabello.
        height (str | None): Altura.
        house (str | None): Casa (Gryffindor, etc.).
        image (str | None): Nombre del archivo de imagen local.
        jobs (list[str]): Ocupaciones.
        marital_status (str | None): Estado civil.
        nationality (str | None): Nacionalidad.
        patronus (str | None): Patronus.
        romances (list[str]): Romances conocidos.
        skin_color (str | None): Color de piel.
        species (str | None): Especie.
        titles (list[str]): Títulos.
        wands (list[dict]): Varitas.
        weight (str | None): Peso.
        wiki (str | None): Enlace a la wikipedia.
    """

    def __init__(
            self,
            id: str = "",
            type: str = "",
            slug: str = "",
            name: str = "",
            alias_names: list[str] | None = None,
            animagus: str | None = None,
            blood_status: str | None = None,
            boggart: str | None = None,
            born: str | None = None,
            died: str | None = None,
            eye_color: str | None = None,
            family_members: list[str] | None = None,
            gender: str | None = None,
            hair_color: str | None = None,
            height: str | None = None,
            house: str | None = None,
            image: str | None = None,
            jobs: list[str] | None = None,
            marital_status: str | None = None,
            nationality: str | None = None,
            patronus: str | None = None,
            romances: list[str] | None = None,
            skin_color: str | None = None,
            species: str | None = None,
            titles: list[str] | None = None,
            wands: list[dict] | None = None,
            weight: str | None = None,
            wiki: str | None = None,
    ):
        """
        Inicializa una instancia de Personaje con los datos proporcionados.

        Parámetros:
            id (str): Identificador único del personaje.
            type (str): Tipo de recurso.
            slug (str): Slug descriptivo.
            name (str): Nombre del personaje.
            alias_names (list[str]): Nombres alternativos o alias.
            animagus (str | None): Información de animagus.
            blood_status (str | None): Estado de sangre.
            boggart (str | None): Boggart asociado.
            born (str | None): Fecha de nacimiento.
            died (str | None): Fecha de fallecimiento.
            eye_color (str | None): Color de ojos.
            family_members (list[str]): Miembros de la familia.
            gender (str | None): Género.
            hair_color (str | None): Color de cabello.
            height (str | None): Altura.
            house (str | None): Casa (Gryffindor, etc.).
            image (str | None): nombre del archivo de imagen local.
            jobs (list[str]): Ocupaciones.
            marital_status (str | None): Estado civil.
            nationality (str | None): Nacionalidad.
            patronus (str | None): Patronus.
            romances (list[str]): Romances conocidos.
            skin_color (str | None): Color de piel.
            species (str | None): Especie.
            titles (list[str]): Títulos.
            wands (list[dict]): Varitas.
            weight (str | None): Peso.
            wiki (str | None): Enlace a la wikipedia.
        """

        self.id = id
        self.type = type
        self.slug = slug
        self.alias_names = alias_names
        self.animagus = animagus
        self.blood_status = blood_status
        self.boggart = boggart
        self.born = born
        self.died = died
        self.eye_color = eye_color
        self.family_members = family_members
        self.gender = gender
        self.hair_color = hair_color
        self.height = height
        self.house = house
        self.image = image
        self.jobs = jobs
        self.marital_status = marital_status
        self.name = name
        self.nationality = nationality
        self.patronus = patronus
        self.romances = romances
        self.skin_color = skin_color
        self.species = species
        self.titles = titles
        self.wands = wands
        self.weight = weight
        self.wiki = wiki


class ServicioArchivos:
    """
    Proporciona métodos estáticos para guardar una lista de objetos Personaje en distintos formatos de archivo (CSV, XML, binario).
    """

    @staticmethod
    def guardar_csv(personajes: list[Personaje], ruta: str):
        """
        Guarda una lista de personajes en el archivo CSV.

        Parámetros:
            personajes (list[Personaje]): Lista de personajes a guardar.
            ruta (str): Ruta del archivo CSV de salida.
        """
        if not personajes:
            return
        try:
            with open(ruta, "w", newline="", encoding="utf-8") as f:
                writer = csv.writer(f)
                headers = list(personajes[0].__dict__.keys())
                writer.writerow(headers)
                for p in personajes:
                    row = [
                        json.dumps(v, ensure_ascii=False) if isinstance(v, list) and v else (v or "")
                        for v in p.__dict__.values()
                    ]
                    writer.writerow(row)
        except Exception:
            raise

    @staticmethod
    def guardar_xml(personajes: list[Personaje], ruta: str):
        """
        Guarda una lista de personajes en un archivo XML.

        Parámetros:
            personajes (list[Personaje]): Lista de personajes a guardar.
            ruta (str): Ruta del archivo XML de salida.
        """
        if not personajes:
            return
        try:
            root = element_tree.Element("characters")
            for p in personajes:
                char_elem = element_tree.SubElement(root, "character")
                for field, value in p.__dict__.items():
                    elem = element_tree.SubElement(char_elem, field)
                    elem.text = json.dumps(value, ensure_ascii=False) if isinstance(value, list) and value else str(value or "")
            tree = element_tree.ElementTree(root)
            tree.write(ruta, encoding="utf-8", xml_declaration=True)
        except Exception:
            raise

    @staticmethod
    def guardar_bin(personajes: list[Personaje], ruta: str):
        """
        Guarda una lista de personajes en formato binario usando pickle.

        Parámetros:
            personajes (list[Personaje]): Lista de personajes a guardar.
            ruta (str): Ruta del archivo binario de salida.
        """
        try:
            with open(ruta, "wb") as f:
                pickle.dump(personajes, f)
        except Exception:
            raise


def descargar_imagen(image_url: str, slug: str) -> str:
    """
    Descarga la imagen de un personaje desde una URL y la guarda en una carpeta local.

    La imagen se guarda en: Reto3_Hogwarts_Anuario/imagenes

    Parámetros:
        image_url (str): URL de la imagen del personaje.
        slug (str): Slug del personaje, usado como nombre del archivo.

    Retorna:
        str: Nombre del archivo de imagen guardado localmente, o cadena vacía si no se pudo descargar.
    """
    if not image_url or not slug:
        return ""

    try:
        base_dir = os.path.join(
            os.path.expanduser("~"),
            "Reto3_Hogwarts_Anuario",
            "imagenes"
        )
        os.makedirs(base_dir, exist_ok=True)

        ext = ".png" if image_url.lower().endswith(".png") else ".jpg"
        nombre_archivo = f"{slug}{ext}"
        ruta_final = os.path.join(base_dir, nombre_archivo)

        if os.path.exists(ruta_final):
            return nombre_archivo

        resp = requests.get(image_url, timeout=10)
        if resp.status_code == 200:
            with open(ruta_final, "wb") as f:
                f.write(resp.content)
            return nombre_archivo

    except Exception:
        raise

    return ""


def fetch_personajes() -> list[Personaje]:
    """
    Obtiene todos los personajes desde la API de PotterDB.

    Realiza llamadas paginadas hasta que no existan más resultados.

    Retorna:
        list[Personaje]: Lista completa de personajes obtenidos desde la API.
    """
    url = "https://api.potterdb.com/v1/characters"
    personajes = []
    page = 1

    while True:
        resp = requests.get(url, params={"page[number]": page, "page[size]": 100})
        if resp.status_code != 200:
            break

        data = resp.json()
        for item in data.get("data", []):
            attrs = item.get("attributes", {})

            image_url = attrs.get("image")
            imagen_local = descargar_imagen(image_url, attrs.get("slug", ""))

            p = Personaje(
                id=item.get("id", ""),
                type=item.get("type", ""),
                slug=attrs.get("slug", ""),
                name=attrs.get("name", ""),
                alias_names=attrs.get("alias_names", []),
                animagus=attrs.get("animagus"),
                blood_status=attrs.get("blood_status"),
                boggart=attrs.get("boggart"),
                born=attrs.get("born"),
                died=attrs.get("died"),
                eye_color=attrs.get("eye_color"),
                family_members=attrs.get("family_members", []),
                gender=attrs.get("gender"),
                hair_color=attrs.get("hair_color"),
                height=attrs.get("height"),
                house=attrs.get("house"),
                image=imagen_local,
                jobs=attrs.get("jobs", []),
                marital_status=attrs.get("marital_status"),
                nationality=attrs.get("nationality"),
                patronus=attrs.get("patronus"),
                romances=attrs.get("romances", []),
                skin_color=attrs.get("skin_color"),
                species=attrs.get("species"),
                titles=attrs.get("titles", []),
                wands=attrs.get("wands", []),
                weight=attrs.get("weight"),
                wiki=attrs.get("wiki"),
            )
            personajes.append(p)

        links = data.get("links", {})
        if not links.get("next"):
            break
        page += 1

    return personajes


def main():
    """
    Función principal del programa.

    Lee las rutas de salida desde la línea de comandos, obtiene los personajes desde la API
    y los guarda en formatos CSV, XML y binario.
    """
    if len(sys.argv) != 4:
        sys.exit(1)

    csv_path, xml_path, bin_path = sys.argv[1:4]

    personajes = fetch_personajes()
    servicio = ServicioArchivos()

    servicio.guardar_csv(personajes, csv_path)
    servicio.guardar_xml(personajes, xml_path)
    servicio.guardar_bin(personajes, bin_path)


if __name__ == "__main__":
    main()
