package transformation;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import savingFile.FileSaver;

public class TransformationXMLToHTML {
	private static String xsltFileName = "ReservationToHTML.xslt";

	public static void transformXMLToHTML(String xmlFile) {
		Source xml = new StreamSource(new StringReader(xmlFile));
		ClassLoader classLoader = new TransformationXMLToHTML().getClass().getClassLoader(); 
		Source xslt = new StreamSource(new File(classLoader.getResource(xsltFileName).getFile()));
		StringWriter sw = new StringWriter();

		try {
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer trasform = tFactory.newTransformer(xslt);
			trasform.transform(xml, new StreamResult(sw));
			
			FileSaver.saveFile(sw, new String("html"));
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	

}
