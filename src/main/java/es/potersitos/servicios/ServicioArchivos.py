import csv
import pickle
import xml.etree.ElementTree as ET
import logging
import json


# Configuración de logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    filename="servicio_archivos.log",
    filemode='a'
)

class ServicioArchivos:
    """
    Clase que proporciona métodos para guardar datos de personajes en distintos formatos.

    @author Erlantz
    @version 1.0
    """

    @staticmethod
    def guardar_csv(personajes: list, ruta: str) -> None:
        """
        Guarda una lista de objetos en un archivo CSV.

        Parámetros:
            personajes (list): Lista de objetos a guardar.
            ruta (str): Ruta del archivo CSV donde se guardarán los datos.

        Retorna:
            None
        """
        if not personajes:
            logging.warning("La lista de personajes está vacía. no se creará el archivo CSV.")
            return

        try:
            with open(ruta, "w", newline="", encoding="utf-8") as f:
                writer = csv.writer(f)

                # Cabeceras
                headers = personajes[0].__dict__.keys()
                writer.writerow(headers)
                logging.info(f"Cabeceras CSV escritas: {headers}")

                # Filas
                for p in personajes:
                    row = []
                    for v in p.__dict__.values():
                        if isinstance(v, list):
                            row.append(json.dumps(v, ensure_ascii=False))
                        else:
                            row.append("" if v is None else v)
                    writer.writerow(row)
                logging.info(f"CSV guardado correctamente en '{ruta}'")
        except Exception as e:
            logging.error(f"Error al guardar CSV en '{ruta}': {e}")


    @staticmethod
    def guardar_xml(personajes: list, ruta: str) -> None:
        """
        Guarda una lista de objetos en un archivo XML.

        Parámetros:
            personajes (list): Lista de objetos a guardar.
            ruta (str): Ruta del archivo XML donde se guardarán los datos.

        Retorna:
            None
        """
        if not personajes:
            logging.warning("La lista de personajes está vacía. No se creará el archivo XML.")
            return

        try:
            root = ET.Element("characters")

            for p in personajes:
                char_elem = ET.SubElement(root, "character")

                for field_name, value in p.__dict__.items():
                    if isinstance(value, list):
                        wrapper = ET.SubElement(char_elem, field_name)
                        for item in value:
                            item_elem = ET.SubElement(wrapper, "item")
                            item_elem.text = str(item)
                    else:
                        elem = ET.SubElement(char_elem, field_name)
                        elem.text = "" if value is None else str(value)

            tree = ET.ElementTree(root)
            tree.write(ruta, encoding="utf-8", xml_declaration=True)
            logging.info(f"XML guardado correctamente en '{ruta}'")
        except Exception as e:
            logging.error(f"Error al guardar XML en '{ruta}': {e}")


    @staticmethod
    def guardar_bin(personajes: list, ruta: str) -> None:
        """
        Guarda una lista de objetos en un archivo binario usando pickle.

        Parámetros:
            personajes (list): Lista de objetos a guardar.
            ruta (str): Ruta del archivo binario donde se guardarán los datos.

        Retorna:
            None
        """
        if not personajes:
            logging.warning("La lista de personajes está vacía. No se creará el archivo binario.")
            return

        try:
            with open(ruta, "wb") as f:
                pickle.dump(personajes, f)
            logging.info(f"Archivo binario guardado correctamente en '{ruta}'")
        except Exception as e:
            logging.error(f"Error al guardar archivo binario en '{ruta}': {e}")
