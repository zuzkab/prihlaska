package createXml;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.Normalizer.Form;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.JPanel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import savingFile.FileSaver;
import view.DateLabelFormatter;
import view.MainWindow;

public class CreateXML {
	
	public static String generateXML(String name, String surname, String email, String type, String date, String time, String onlinePay, String guestName, String guestSurname, String guestType) {
		
		ClassLoader classLoader = new CreateXML().getClass().getClassLoader(); 
		StringWriter sw = new StringWriter();
		sw = null;

		try {
			
		
		DocumentBuilderFactory dbFactory =  DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder;
		
		
		dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.newDocument();
	          
	    // root element
	    Element rootElement = doc.createElement("registration");
	    doc.appendChild(rootElement);

	    	// element buyer
	    	Element buyer = doc.createElement("buyer");
	    	rootElement.appendChild(buyer);
	          
	    	// element course
	    	Element course = doc.createElement("course");
	    	rootElement.appendChild(course);
	          
	        // parent element guests
	        Element guests = doc.createElement("guests");
	        rootElement.appendChild(guests);

	        	// setting attribute to element
	        	Attr attr = doc.createAttribute("type");
	        	attr.setValue(type);
	        	buyer.setAttributeNode(attr);

	        	// buyer
	        	Element buyerName = doc.createElement("name");
	        	buyerName.appendChild(doc.createTextNode(name));
	        	buyer.appendChild(buyerName);

	        	Element buyerSurname = doc.createElement("surname");
	        	buyerSurname.appendChild(doc.createTextNode(surname));
	        	buyer.appendChild(buyerSurname);
	        	
	        	Element buyerEmail = doc.createElement("email");
	        	buyerEmail.appendChild(doc.createTextNode(email));
	        	buyer.appendChild(buyerEmail);
	        	
	        	// course
	        	Element courseDate = doc.createElement("date");
	        	courseDate.appendChild(doc.createTextNode(date));
	        	course.appendChild(courseDate);

	        	Element courseTime = doc.createElement("time");
	        	courseTime.appendChild(doc.createTextNode(time));
	        	course.appendChild(courseTime);
	        	
	        	Element onlinePayment = doc.createElement("onlinePayment");
	        	onlinePayment.appendChild(doc.createTextNode(onlinePay));
	        	course.appendChild(onlinePayment);
	        	
	        	//  guests
	        	Element gName = doc.createElement("name");
	        	gName.appendChild(doc.createTextNode(guestName));
	        	guests.appendChild(gName);

	        	Element gSurname = doc.createElement("surname");
	        	gSurname.appendChild(doc.createTextNode(guestSurname));
	        	guests.appendChild(gSurname);
	        	
	        	Element gType = doc.createElement("type");
	        	gType.appendChild(doc.createTextNode(guestType));
	        	guests.appendChild(gType);

	        	
	        		// write the content into xml file
	        		TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        		Transformer transformer = transformerFactory.newTransformer();
	        		DOMSource source = new DOMSource(doc);
	        		StreamResult result = new StreamResult(new File("C:\\Users\\PC\\Documents\\registration.xml"));
	        		transformer.transform(source, result);
	          
	        		// Output to console for testing
	        		StreamResult res = new StreamResult(System.out);
	        		transformer.transform(source, res);
	        		transformer.transform(source, new StreamResult(sw));
	        		FileSaver.saveFile(sw, new String("xmlFile"));
	        		
	        		
	        	}
	        	
	        	catch (Exception e) {
	        		System.out.println();
	        		System.out.println(e.getStackTrace().toString());
	        	}
	        	
	          
	        	
	        return sw.toString();
	        	

	}

}
