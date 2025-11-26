package es.potersitos.services;

import es.potersitos.modelos.Personaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

/**
 * Servicio encargado de generar distintos tipos de archivos (CSV, XML y binario)
 * a partir de una lista de {@link Personaje}. Se utiliza para exportar y
 * persistir la información obtenida desde la API.
 *
 * @author Erlantz
 * @version 1.0
 */
public class ServicioArchivos {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(ServicioArchivos.class);

    /**
     * Guarda la lista de personajes en un archivo CSV.
     *
     * @param list Lista de personajes a exportar.
     * @param path Ruta completa del archivo CSV de salida.
     *
     * @author Erlantz
     */
    public void guardarComoCSV(List<Personaje> list, String path) {
        try (FileWriter writer = new FileWriter(path)) {

            // Cabeceras dinámicas
            writer.write(String.join(",", HEADERS) + "\n");

            for (Personaje c : list) {
                writer.write(String.join(",", createCSVRow(c)) + "\n");
            }

        } catch (Exception e) {
            logger.error("Error al guardar como CSV: {}", e.getMessage());
        }
    }

    /** Cabeceras del CSV, en el mismo orden que createCSVRow() */
    private static final String[] HEADERS = {
            "id","type","slug","aliasNames","animagus","bloodStatus","boggart","born","died",
            "eyeColor","familyMembers","gender","hairColor","height","house","image","jobs",
            "maritalStatus","name","nationality","patronus","romances","skinColor","species",
            "titles","wands","weight","wiki"
    };

    /** Crea una fila CSV con todos los campos */
    private String[] createCSVRow(Personaje p) {
        return new String[]{
                safe(p.getId()),
                safe(p.getType()),
                safe(p.getSlug()),
                safe(listToCSV(p.getAliasNames())),
                safe(p.getAnimagus()),
                safe(p.getBloodStatus()),
                safe(p.getBoggart()),
                safe(p.getBorn()),
                safe(p.getDied()),
                safe(p.getEyeColor()),
                safe(listToCSV(p.getFamilyMembers())),
                safe(p.getGender()),
                safe(p.getHairColor()),
                safe(p.getHeight()),
                safe(p.getHouse()),
                safe(p.getImage()),
                safe(listToCSV(p.getJobs())),
                safe(p.getMaritalStatus()),
                safe(p.getName()),
                safe(p.getNationality()),
                safe(p.getPatronus()),
                safe(listToCSV(p.getRomances())),
                safe(p.getSkinColor()),
                safe(p.getSpecies()),
                safe(listToCSV(p.getTitles())),
                safe(listToCSV(p.getWands())),
                safe(p.getWeight()),
                safe(p.getWiki())
        };
    }

    /** Convierte listas a texto CSV (separadas por ;) */
    private String listToCSV(List<String> list) {
        if (list == null) return "";
        return String.join(";", list);
    }

    /**
     * Guarda la lista de personajes como un archivo XML.
     *
     * @param list Lista de personajes a exportar.
     * @param path Ruta completa del archivo XML de salida.
     *
     * @author Erlantz
     */
    public void guardarComoXML(List<Personaje> list, String path) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element root = doc.createElement("characters");
            doc.appendChild(root);

            for (Personaje c : list) {
                Element charElem = doc.createElement("character");

                addNode(doc, charElem, "id", c.getId());
                addNode(doc, charElem, "type", c.getType());
                addNode(doc, charElem, "slug", c.getSlug());

                addListNode(doc, charElem, "aliasNames", "alias", c.getAliasNames());

                addNode(doc, charElem, "animagus", c.getAnimagus());
                addNode(doc, charElem, "bloodStatus", c.getBloodStatus());
                addNode(doc, charElem, "boggart", c.getBoggart());
                addNode(doc, charElem, "born", c.getBorn());
                addNode(doc, charElem, "died", c.getDied());
                addNode(doc, charElem, "EyeColor", c.getEyeColor());

                addListNode(doc, charElem, "familyMembers", "member", c.getFamilyMembers());

                addNode(doc, charElem, "gender", c.getGender());
                addNode(doc, charElem, "hairColor", c.getHairColor());
                addNode(doc, charElem, "height", c.getHeight());
                addNode(doc, charElem, "house", c.getHouse());
                addNode(doc, charElem, "image", c.getImage());

                addListNode(doc, charElem, "jobs", "job", c.getJobs());

                addNode(doc, charElem, "maritalStatus", c.getMaritalStatus());
                addNode(doc, charElem, "name", c.getName());
                addNode(doc, charElem, "nationality", c.getNationality());
                addNode(doc, charElem, "patronus", c.getPatronus());

                addListNode(doc, charElem, "romances", "romance", c.getRomances());

                addNode(doc, charElem, "skinColor", c.getSkinColor());
                addNode(doc, charElem, "species", c.getSpecies());

                addListNode(doc, charElem, "titles", "title", c.getTitles());
                addListNode(doc, charElem, "wands", "wand", c.getWands());

                addNode(doc, charElem, "weight", c.getWeight());
                addNode(doc, charElem, "wiki", c.getWiki());

                root.appendChild(charElem);
            }

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tf.transform(new DOMSource(doc), new StreamResult(new File(path)));

        } catch (Exception e) {
            logger.error("Error al guardar como XML: {}", e.getMessage());
        }
    }

    /**
     * Añade un nodo hijo a un elemento XML.
     *
     * @param doc Documento XML donde se está escribiendo.
     * @param parent Elemento padre donde se insertará el nodo.
     * @param tag Nombre del nodo a crear.
     * @param value Valor de texto del nodo.
     *
     * @author Erlantz
     */
    private void addNode(Document doc, Element parent, String tag, String value) {
        Element e = doc.createElement(tag);
        e.appendChild(doc.createTextNode(value == null ? "" : value));
        parent.appendChild(e);
    }

    /** Añade listas como nodos XML */
    private void addListNode(Document doc, Element parent, String wrapper, String tag, List<String> list) {
        Element wrapperNode = doc.createElement(wrapper);
        if (list != null) {
            for (String s : list) {
                Element child = doc.createElement(tag);
                child.appendChild(doc.createTextNode(s));
                wrapperNode.appendChild(child);
            }
        }
        parent.appendChild(wrapperNode);
    }

    /**
     * Sanitiza cadenas para que no rompan la estructura CSV.
     *
     * @param s Cadena a sanitizar.
     * @return Cadena segura para CSV.
     *
     * @author Erlantz
     */
    private String safe(String s) {
        return s == null ? "" : s.replace(",", " "); // evita romper CSV
    }

    /**
     * Guarda la lista de personajes como un archivo binario utilizando serialización Java.
     *
     * @param list Lista de personajes a exportar (deben implementar Serializable).
     * @param path Ruta completa del archivo binario de salida.
     *
     * @author Erlantz
     */
    public void guardarComoBinario(List<Personaje> list, String path) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(list);
        } catch (Exception e) {
            logger.error("Error al guardar como Binario: {}", e.getMessage());
        }
    }
}
