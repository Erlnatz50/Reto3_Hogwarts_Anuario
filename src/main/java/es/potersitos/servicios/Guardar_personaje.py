import json
import sys
from servicios.ServicioArchivos import ServicioArchivos
from modelos import Personaje
import os

def main():
    if len(sys.argv) < 2:
        print("Uso: python guardar_personaje.py archivo.json")
        exit(1)

    ruta_json = sys.argv[1]

    if not os.path.exists(ruta_json):
        print(f"El archivo {ruta_json} no existe")
        exit(1)

    # Cargar personaje desde JSON
    with open(ruta_json, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Crear objeto Personaje de tu modelo
    p = Personaje(**data)

    # Cargar CSV existente (si existe)
    try:
        personajes = ServicioArchivos.cargar_csv("personajes.csv")
    except FileNotFoundError:
        personajes = []
    except Exception as e:
        print(f"Error al cargar CSV existente: {e}")
        personajes = []

    personajes.append(p)

    # Guardar en CSV, XML y BIN
    ServicioArchivos.guardar_csv(personajes, "personajes.csv")
    ServicioArchivos.guardar_xml(personajes, "personajes.xml")
    ServicioArchivos.guardar_bin(personajes, "personajes.bin")

    print("OK")

if __name__ == "__main__":
    main()
