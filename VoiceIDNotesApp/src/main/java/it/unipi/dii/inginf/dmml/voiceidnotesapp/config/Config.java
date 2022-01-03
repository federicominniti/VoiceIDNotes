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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Thread.sleep;


public class Config {
    //ROBA DATABASE
    private static volatile Config localConfig;
    private String voiceExtractionServerIP;
    private int voiceExtractionServerPort;
    private String datasetPath;

    public static Config getInstance() throws IOException {
        if (localConfig == null) {
            synchronized (Config.class) {
                if (localConfig == null) {
                    localConfig = getParams();
                }
            }
        }
        return localConfig;
    }

    private static Config getParams() {
        if (validConfigParams()) {
            XStream xstream = new XStream();
            xstream.addPermission(AnyTypePermission.ANY);
            String text = null;

            try {
                text = new String(Files.readAllBytes(Paths.get("config.xml")));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            return (Config) xstream.fromXML(text);

        } else {
            Utils.showAlert("Problem with the configuration file!");
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        return null;
    }
/*
    private static Config getParams() throws IOException {
        if (validConfigParams()) {
            File configFile = new File("config.xml");
            XmlMapper xmlMapper = new XmlMapper();
            String xmlString = inputStreamToString(new FileInputStream(configFile));
            Config instance = xmlMapper.readValue(xmlString, Config.class);
            return instance;
        } else {
            Utils.showAlert("XML Schema file could not be validated: wrong configuration parameters");
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        return null;
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
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