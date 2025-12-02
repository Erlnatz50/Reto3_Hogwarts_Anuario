import json
import base64
import os
import sys
from dataclasses import dataclass
from typing import Optional, List

@dataclass
class Personaje:
    # Campos principales
    name: str
    alias_names: Optional[List[str]]
    family_members: Optional[List[str]]
    romances: Optional[List[str]]
    jobs: Optional[List[str]]
    titles: Optional[List[str]]
    description: Optional[str]
    wands: Optional[List[str]]
    school_ship: Optional[str]
    image: Optional[str]
    image_blob: Optional[str]

    # Campos adicionales de datos.fxml
    animagus: Optional[str] = None
    bloodStatus: Optional[str] = None
    boggart: Optional[str] = None
    nacido: Optional[str] = None
    fallecido: Optional[str] = None
    colorOjos: Optional[str] = None
    familiares: Optional[str] = None
    genero: Optional[str] = None
    colorPelo: Optional[str] = None
    altura: Optional[str] = None
    casa: Optional[str] = None
    imagen: Optional[str] = None
    trabajos_extra: Optional[str] = None
    estadoCivil: Optional[str] = None
    nacionalidad: Optional[str] = None
    patronus: Optional[str] = None
    colorPiel: Optional[str] = None
    especie: Optional[str] = None
    varitas: Optional[str] = None
    peso: Optional[str] = None

def guardar_personaje(personaje: Personaje):
    # Crear carpeta base
    base_dir = "personajes"
    os.makedirs(base_dir, exist_ok=True)

    # Crear carpeta específica del personaje
    safe_name = personaje.name if personaje.name else "sin_nombre"
    person_dir = os.path.join(base_dir, safe_name)
    os.makedirs(person_dir, exist_ok=True)

    # Guardar JSON
    json_path = os.path.join(person_dir, "datos.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(personaje.__dict__, f, ensure_ascii=False, indent=4)
    print(f"Datos guardados en {json_path}")

    # Guardar imagen si existe
    if personaje.image_blob:
        image_path = os.path.join(person_dir, "imagen.png")
        with open(image_path, "wb") as img_file:
            img_file.write(base64.b64decode(personaje.image_blob))
        print(f"Imagen guardada en {image_path}")
    else:
        print("No se recibió ninguna imagen en 'image_blob'.")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python guardar_personaje.py <ruta_archivo_json>")
        sys.exit(1)

    json_path = sys.argv[1]

    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        # Crear objeto Personaje con todos los campos posibles
        personaje = Personaje(**data)
        guardar_personaje(personaje)

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)
