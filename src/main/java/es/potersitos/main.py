import os
import requests
from modelos.Personaje import Personaje
from services.ServicioArchivos import ServicioArchivos

def fetch_personajes() -> list[Personaje]:
    """
    Llama a la API de Potter DB para obtener todos los personajes,
    y devuelve una lista de instancias de Personaje.
    """
    url = "https://api.potterdb.com/v1/characters"
    personajes = []

    page = 1
    while True:
        resp = requests.get(url, params={"page[number]": page, "page[size]": 100})
        if resp.status_code != 200:
            print(f"Error al obtener datos: {resp.status_code}")
            break

        data = resp.json()
        for item in data.get("data", []):
            attrs = item.get("attributes", {})
            # Construir Personaje a partir de attrs — maneja campos faltantes con get(...)
            p = Personaje(
                id=item.get("id"),
                type=item.get("type"),
                slug=attrs.get("slug", ""),
                alias_names=attrs.get("alias_names", []) or [],
                animagus=attrs.get("animagus"),
                blood_status=attrs.get("blood_status"),
                boggart=attrs.get("boggart"),
                born=attrs.get("born"),
                died=attrs.get("died"),
                eye_color=attrs.get("eye_color"),
                family_members=attrs.get("family_members", []) or [],
                gender=attrs.get("gender"),
                hair_color=attrs.get("hair_color"),
                height=attrs.get("height"),
                house=attrs.get("house"),
                image=attrs.get("image"),
                jobs=attrs.get("jobs", []) or [],
                marital_status=attrs.get("marital_status"),
                name=attrs.get("name"),
                nationality=attrs.get("nationality"),
                patronus=attrs.get("patronus"),
                romances=attrs.get("romances", []) or [],
                skin_color=attrs.get("skin_color"),
                species=attrs.get("species"),
                titles=attrs.get("titles", []) or [],
                wands=attrs.get("wands", []) or [],
                weight=attrs.get("weight"),
                wiki=attrs.get("wiki"),
                image_blob=None
            )
            personajes.append(p)

        # Paginación: si no hay página siguiente, salimos
        links = data.get("links", {})
        if not links.get("next"):
            break
        page += 1

    return personajes

def main():
    personajes = fetch_personajes()

    servicio = ServicioArchivos()

    BASE_PATH = os.path.dirname(os.path.abspath(__file__))

    csv_path = os.path.join(BASE_PATH, "..", "..", "..", "resources", "es", "potersitos", "csv", "all_characters.csv")
    xml_path = os.path.join(BASE_PATH, "..", "..", "..", "resources", "es", "potersitos", "xml", "all_characters.xml")
    bin_path = os.path.join(BASE_PATH, "..", "..", "..", "resources", "es", "potersitos", "bin", "all_characters.bin")

    servicio.guardar_csv(personajes, csv_path)
    servicio.guardar_xml(personajes, xml_path)
    servicio.guardar_bin(personajes, bin_path)

if __name__ == "__main__":
    main()
