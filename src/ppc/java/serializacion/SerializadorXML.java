package ppc.java.serializacion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ppc.java.modelo.Mensaje;
import ppc.java.modelo.MensajeControl;
import ppc.java.modelo.MensajeDistribucion;
import ppc.java.modelo.TipoControl;
import ppc.java.modelo.TipoMensaje;
import ppc.java.modelo.Variable;

/**
 * Serializador/deserializador XML basado en DOM. En la
 * deserializacion se realiza la validacion explicita del documento
 * contra el esquema XSD correspondiente.
 */
public class SerializadorXML implements ISerializador {

    private static final String XSD_DISTRIBUCION = "distribucion.xsd";
    private static final String XSD_CONTROL = "control.xsd";

    private final DocumentBuilderFactory dbf;
    private final Schema esquemaDistribucion;
    private final Schema esquemaControl;

    public SerializadorXML() throws SerializacionException {
        this.dbf = DocumentBuilderFactory.newInstance();
        this.dbf.setNamespaceAware(true);
        this.dbf.setIgnoringElementContentWhitespace(true);
        try {
            // Evita ataques XXE: no se procesan DTDs externas
            this.dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception ignore) {
            // No es critico
        }

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            this.esquemaDistribucion = sf.newSchema(
                    new javax.xml.transform.stream.StreamSource(cargarEsquema(XSD_DISTRIBUCION)));
            this.esquemaControl = sf.newSchema(
                    new javax.xml.transform.stream.StreamSource(cargarEsquema(XSD_CONTROL)));
        } catch (Exception e) {
            throw new SerializacionException("No se pudieron cargar los esquemas XSD", e);
        }
    }

    /**
     * Carga un esquema buscandolo primero en el classpath (/esquemas/...) y,
     * si no se encuentra, en el directorio "esquemas" del sistema de ficheros.
     */
    private InputStream cargarEsquema(String nombre) throws SerializacionException {
        // En el classpath
        InputStream is = getClass().getResourceAsStream("/esquemas/" + nombre);
        if (is != null) {
            return is;
        }
        // Como fichero, en las ubicaciones habituales
        String[] rutas = {
                "esquemas/" + nombre,
                "src/ppc/resources/esquemas/" + nombre
        };
        for (String r : rutas) {
            Path p = Paths.get(r);
            if (Files.exists(p)) {
                try {
                    return Files.newInputStream(p);
                } catch (Exception e) {
                    throw new SerializacionException("No se pudo abrir el esquema " + p, e);
                }
            }
        }
        throw new SerializacionException("No se encontro el esquema XSD: " + nombre);
    }

    @Override
    public Formato getFormato() {
        return Formato.XML;
    }

    // Serializacion (modelo -> XML)
    @Override
    public byte[] serializar(Mensaje mensaje) throws SerializacionException {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            if (mensaje instanceof MensajeDistribucion) {
                construirDistribucion(doc, (MensajeDistribucion) mensaje);
            } else if (mensaje instanceof MensajeControl) {
                construirControl(doc, (MensajeControl) mensaje);
            } else {
                throw new SerializacionException("Tipo de mensaje no soportado: " + mensaje);
            }

            return documentoABytes(doc);
        } catch (SerializacionException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializacionException("Error serializando a XML", e);
        }
    }

    private void construirDistribucion(Document doc, MensajeDistribucion m) {
        Element raiz = doc.createElement("distribucion");
        doc.appendChild(raiz);

        raiz.appendChild(elementoTexto(doc, "idServidor", m.getIdServidor()));
        raiz.appendChild(elementoTexto(doc, "marcaTiempo", Long.toString(m.getMarcaTiempo())));

        Element variables = doc.createElement("variables");
        raiz.appendChild(variables);

        for (Variable v : m.getVariables()) {
            Element var = doc.createElement("variable");
            var.appendChild(elementoTexto(doc, "nombre", v.getNombre()));
            var.appendChild(elementoTexto(doc, "valor", Double.toString(v.getValor())));
            var.appendChild(elementoTexto(doc, "unidad", v.getUnidad()));
            variables.appendChild(var);
        }
    }

    private void construirControl(Document doc, MensajeControl m) {
        Element raiz = doc.createElement("control");
        doc.appendChild(raiz);

        raiz.appendChild(elementoTexto(doc, "idCliente", m.getIdCliente()));
        raiz.appendChild(elementoTexto(doc, "idServidor", m.getIdServidor()));
        raiz.appendChild(elementoTexto(doc, "tipo", m.getTipo().name()));
        if (m.getVariable() != null) {
            raiz.appendChild(elementoTexto(doc, "variable", m.getVariable()));
        }
        if (m.getValor() != null) {
            raiz.appendChild(elementoTexto(doc, "valor", m.getValor()));
        }
        if (m.getRespuesta() != null) {
            raiz.appendChild(elementoTexto(doc, "respuesta", m.getRespuesta()));
        }
    }

    private Element elementoTexto(Document doc, String nombre, String texto) {
        Element e = doc.createElement(nombre);
        e.appendChild(doc.createTextNode(texto == null ? "" : texto));
        return e;
    }

    private byte[] documentoABytes(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.transform(new DOMSource(doc), new StreamResult(baos));
        return baos.toByteArray();
    }

    // Deserializacion (XML -> modelo) con validacion explicita
    @Override
    public Mensaje deserializar(byte[] datos, TipoMensaje tipo) throws SerializacionException {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(datos));
            doc.getDocumentElement().normalize();

            // Validacion explicita contra el esquema correspondiente
            Schema esquema = (tipo == TipoMensaje.DISTRIBUCION)
                    ? esquemaDistribucion : esquemaControl;
            Validator validador = esquema.newValidator();
            validador.validate(new DOMSource(doc));


            if (tipo == TipoMensaje.DISTRIBUCION) {
                return leerDistribucion(doc.getDocumentElement());
            } else {
                return leerControl(doc.getDocumentElement());
            }
        } catch (org.xml.sax.SAXException e) {
            throw new SerializacionException("XML no valido segun el esquema: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SerializacionException("Error deserializando XML", e);
        }
    }

    private MensajeDistribucion leerDistribucion(Element raiz) {
        MensajeDistribucion m = new MensajeDistribucion();
        m.setIdServidor(textoHijo(raiz, "idServidor"));
        m.setMarcaTiempo(Long.parseLong(textoHijo(raiz, "marcaTiempo")));

        Element variables = (Element) raiz.getElementsByTagName("variables").item(0);
        NodeList lista = variables.getElementsByTagName("variable");
        for (int i = 0; i < lista.getLength(); i++) {
            Element var = (Element) lista.item(i);
            Variable v = new Variable(
                    textoHijo(var, "nombre"),
                    Double.parseDouble(textoHijo(var, "valor")),
                    textoHijo(var, "unidad"));
            m.addVariable(v);
        }
        return m;
    }

    private MensajeControl leerControl(Element raiz) {
        MensajeControl m = new MensajeControl();
        m.setIdCliente(textoHijo(raiz, "idCliente"));
        m.setIdServidor(textoHijo(raiz, "idServidor"));
        m.setTipo(TipoControl.valueOf(textoHijo(raiz, "tipo")));
        m.setVariable(textoHijoOpcional(raiz, "variable"));
        m.setValor(textoHijoOpcional(raiz, "valor"));
        m.setRespuesta(textoHijoOpcional(raiz, "respuesta"));
        return m;
    }

    /** Devuelve el texto del primer hijo directo con ese nombre. */
    private String textoHijo(Element padre, String nombre) {
        NodeList hijos = padre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node n = hijos.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(nombre)) {
                return n.getTextContent();
            }
        }
        return null;
    }

    private String textoHijoOpcional(Element padre, String nombre) {
        String t = textoHijo(padre, nombre);
        return (t == null || t.isEmpty()) ? null : t;
    }
}
