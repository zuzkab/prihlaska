package validation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class ValidationXML {
	private static String xsdFileName = "ReservationSchema.xsd";

	public static StringWriter validateXML(String xmlFile) {
		ClassLoader classLoader = new ValidationXML().getClass().getClassLoader(); 
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try {
			schema = factory.newSchema(new StreamSource(new File(classLoader.getResource(xsdFileName).getFile())));
		} catch (SAXException e) {
			e.printStackTrace();
		}
		Validator validator = schema.newValidator();
	    LinkedList<SAXParseException> exceptions = new LinkedList<SAXParseException>();
		validator.setErrorHandler(new ErrorHandler() {
			 
			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				exceptions.add(exception);
			}
			 
			@Override
			public void error(SAXParseException exception) throws SAXException {
				exceptions.add(exception);
			}

			@Override
			public void warning(SAXParseException exception) throws SAXException {
				exceptions.add(exception);
			}
		});
		
		try {
			validator.validate(new StreamSource(new StringReader(xmlFile)));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		StringWriter sw = new StringWriter();
		if (!exceptions.isEmpty()) {
			for(SAXParseException ex: exceptions) {
				sw.write("Error at Line: " + ex.getLineNumber() + " Column: " + ex.getColumnNumber() + "\n" +
						 	"Message: " + ex.getMessage() + "\n");
			}
	    } 
		
		return sw;
	}
}