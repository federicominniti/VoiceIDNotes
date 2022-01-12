package it.unipi.dii.inginf.dmml.voiceidnotesapp.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.nio.file.Files;
import java.nio.file.Paths;



public class Config {
    private static volatile Config localConfig;
    private final String voiceExtractionServerIP;
    private final int voiceExtractionServerPort;
    private final String datasetPath;

    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 5001;
    private static final String DEFAULT_DATASET = "data.csv";

    /**
     * Constructor implementing the singleton pattern
     * @return a Config instance
     */
    public static Config getInstance() {
        if (localConfig == null) {
            synchronized (Config.class) {
                if (localConfig == null) {
                    localConfig = getParams();
                }
            }
        }
        return localConfig;
    }

    private Config() {
        this.voiceExtractionServerIP = DEFAULT_SERVER_IP;
        this.voiceExtractionServerPort = DEFAULT_SERVER_PORT;
        this.datasetPath = DEFAULT_DATASET;
    }

    /**
     * Used by the getInstance() to read the parameters contained in the config.xml file, after validating it against
     * the config.xsd XML schema
     * @return a Config instance
     */
    private static Config getParams() {
        if (validConfigParams()) {
            XStream xstream = new XStream();
            xstream.addPermission(AnyTypePermission.ANY);
            String text = null;

            try {
                text = new String(Files.readAllBytes(Paths.get("config.xml")));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return new Config();
            }

            return (Config) xstream.fromXML(text);

        } else {
            Utils.showAlert("Problem with the configuration file!");
            return new Config();
        }
    }

    /**
     * Validates the config.xml against the config.xsd XML schema
     */
    private static boolean validConfigParams()
    {
        Document document;
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            document = documentBuilder.parse("config.xml");
            Schema schema = schemaFactory.newSchema(new StreamSource("config.xsd"));
            schema.newValidator().validate(new DOMSource(document));
        }
        catch (Exception e) {
            if (e instanceof SAXException)
                System.err.println("Validation Error: " + e.getMessage());
            else
                System.err.println(e.getMessage());

            return false;
        }
        return true;
    }

    public String getVoiceExtractorServerIP() {
        return voiceExtractionServerIP;
    }

    public int getVoiceExtractorServerPort() {
        return voiceExtractionServerPort;
    }

    public String getDatasetPath() {
        return datasetPath;
    }
}