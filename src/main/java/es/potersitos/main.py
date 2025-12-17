import sys
import os
import requests
import csv
import pickle
import xml.etree.ElementTree as ET
import json

class Personaje:
    def __init__(self, id="", type="", slug="", name="", alias_names=None, animagus=None,
                 blood_status=None, boggart=None, born=None, died=None, eye_color=None,
                 family_members=None, gender=None, hair_color=None, height=None, house=None,
                 image=None, jobs=None, marital_status=None, nationality=None, patronus=None,
                 romances=None, skin_color=None, species=None, titles=None, wands=None,
                 weight=None, wiki=None, image_blob=None):

        self.id = id or ""
        self.type = type or ""
        self.slug = slug or ""
        self.alias_names = alias_names or []
        self.animagus = animagus
        self.blood_status = blood_status
        self.boggart = boggart
        self.born = born
        self.died = died
        self.eye_color = eye_color
        self.family_members = family_members or []
        self.gender = gender
        self.hair_color = hair_color
        self.height = height
        self.house = house
        self.image = image
        self.jobs = jobs or []
        self.marital_status = marital_status
        self.name = name
        self.nationality = nationality
        self.patronus = patronus
        self.romances = romances or []
        self.skin_color = skin_color
        self.species = species
        self.titles = titles or []
        self.wands = wands or []
        self.weight = weight
        self.wiki = wiki
        self.image_blob = image_blob

class ServicioArchivos:
    @staticmethod
    def guardar_csv(personajes, ruta):
        if not personajes: return
        try:
            with open(ruta, "w", newline="", encoding="utf-8") as f:
                writer = csv.writer(f)
                headers = list(personajes[0].__dict__.keys())
                writer.writerow(headers)
                for p in personajes:
                    row = [json.dumps(v, ensure_ascii=False) if isinstance(v, list) else (v or "")
                           for v in p.__dict__.values()]
                    writer.writerow(row)
        except Exception:
            pass

    @staticmethod
    def guardar_xml(personajes, ruta):
        if not personajes: return
        try:
            root = ET.Element("characters")
            for p in personajes:
                char_elem = ET.SubElement(root, "character")
                for field, value in p.__dict__.items():
                    elem = ET.SubElement(char_elem, field)
                    elem.text = json.dumps(value, ensure_ascii=False) if isinstance(value, list) else str(value or "")
            tree = ET.ElementTree(root)
            tree.write(ruta, encoding="utf-8", xml_declaration=True)
        except Exception:
            pass

    @staticmethod
    def guardar_bin(personajes, ruta):
        try:
            with open(ruta, "wb") as f:
                pickle.dump(personajes, f)
        except Exception:
            pass

def fetch_personajes():
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
                image=attrs.get("image"),
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
                wiki=attrs.get("wiki")
            )
            personajes.append(p)
        links = data.get("links", {})
        if not links.get("next"): break
        page += 1
    return personajes

def main():
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
