# Archivo: python_api_scripts/gestion_datos.py
import requests
import csv
import json
import sys
import os

# --- CONFIGURACIÓN ---
API_URL = "https://api.potterdb.com/v1/characters"
CSV_FILE = 'data/wizards.csv'
CSV_HEADERS = ['id', 'name', 'house', 'wand']

def extract_wizard_data(api_record):
    """Extrae los campos requeridos de un registro de la API."""
    attributes = api_record.get('attributes', {})

    wand_data = attributes.get('wand', None)
    wand_string = json.dumps(wand_data) if isinstance(wand_data, dict) else (wand_data if wand_data else '')

    return {
        'id': api_record.get('id', ''),
        'name': attributes.get('name', ''),
        'house': attributes.get('house', ''),
        'wand': wand_string
    }

def get_data_from_api():
    """Llama a la API externa, iterando a trav\u00e9s de todas las p\u00e1ginas disponibles."""

    all_wizards = []
    current_url = API_URL
    params = {'page[size]': 500}

    sys.stderr.write("Iniciando recuperaci\u00f3n paginada de la API...\n")

    while current_url:
        try:
            response = requests.get(current_url, params=params)
            response.raise_for_status()
            data = response.json()

            if 'data' in data:
                current_wizards = [extract_wizard_data(record) for record in data['data']]
                all_wizards.extend(current_wizards)
                sys.stderr.write(f"  Recuperados {len(current_wizards)} magos. Total: {len(all_wizards)}\n")

            current_url = data.get('links', {}).get('next')
            params = {}

        except requests.exceptions.RequestException as e:
            sys.stderr.write(f"Error cr\u00edtico en la API: {e}\n")
            break

    sys.stderr.write(f"Recuperaci\u00f3n terminada. Total de magos obtenidos: {len(all_wizards)}\n")
    return all_wizards

def initialize_csv():
    """Asegura que la carpeta 'data' y el archivo CSV existan con encabezados."""
    if not os.path.exists('data'):
        os.makedirs('data')
        sys.stderr.write("Directorio 'data' creado.\n")

    if not os.path.exists(CSV_FILE):
        with open(CSV_FILE, 'w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=CSV_HEADERS)
            writer.writeheader()
        sys.stderr.write(f"Archivo CSV '{CSV_FILE}' creado.\n")

def obtener_todos_y_actualizar_csv():
    """Obtiene datos de la API y los guarda en CSV; luego devuelve el CSV en JSON."""
    initialize_csv()

    api_data = get_data_from_api()

    if not api_data:
        sys.stderr.write("No se obtuvieron datos de la API. Leyendo datos antiguos del CSV.\n")
        return obtener_datos_csv()

    # Reemplazar el contenido del CSV con los nuevos datos de la API
    try:
        with open(CSV_FILE, 'w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=CSV_HEADERS)
            writer.writeheader()
            writer.writerows(api_data)
        sys.stderr.write(f"CSV actualizado con {len(api_data)} registros.\n")
    except Exception as e:
        sys.stderr.write(f"Error escribiendo en CSV: {e}\n")
        return obtener_datos_csv()

    # Devolver los datos en formato JSON para que Java los lea
    print(json.dumps(api_data))

def obtener_datos_csv():
    """Lee todos los datos del CSV y los devuelve como JSON."""
    try:
        with open(CSV_FILE, 'r', newline='', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            data = list(reader)
        print(json.dumps(data))
        return data
    except FileNotFoundError:
        sys.stderr.write(f"Error: Archivo {CSV_FILE} no encontrado para lectura.\n")
        print(json.dumps([]))
        return []

if __name__ == '__main__':
    obtener_todos_y_actualizar_csv()