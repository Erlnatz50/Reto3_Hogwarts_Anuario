import csv
import pickle
import xml.etree.ElementTree as ET

class ServicioArchivos:

    def guardar_csv(self, personajes, path):
        """Guarda todos los datos en un CSV."""

        with open(path, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)

            # Cabeceras
            headers = personajes[0].__dict__.keys()
            writer.writerow(headers)

            # Filas
            for p in personajes:
                row = []
                for v in p.__dict__.values():
                    if isinstance(v, list):
                        row.append(";".join(v))
                    else:
                        row.append("" if v is None else v)
                writer.writerow(row)

    def guardar_xml(self, personajes, path):
        """Guarda todos los datos en un archivo XML."""
        
        root = ET.Element("characters")

        for p in personajes:
            char_elem = ET.SubElement(root, "character")

            for field_name, value in p.__dict__.items():
                if isinstance(value, list):
                    wrapper = ET.SubElement(char_elem, field_name)
                    for item in value:
                        item_elem = ET.SubElement(wrapper, "item")
                        item_elem.text = item
                else:
                    elem = ET.SubElement(char_elem, field_name)
                    elem.text = "" if value is None else str(value)

        tree = ET.ElementTree(root)
        tree.write(path, encoding="utf-8", xml_declaration=True)

    def guardar_bin(self, personajes, path):
        """Guarda todos los datos en un archivo binario (pickle)."""
        with open(path, "wb") as f:
            pickle.dump(personajes, f)
