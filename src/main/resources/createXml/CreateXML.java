package createXml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import model.Guest;
import savingFile.FileSaver;

public class CreateXML {
	
	public static String generateXML(String name, String surname, String email, String type, String date, String time, String onlinePay, List<Guest> guestsList) {
		
		ClassLoader classLoader = new CreateXML().getClass().getClassLoader(); 
		StringWriter sw = new StringWriter();
		String strResult = new String();
		

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
            if (!guestsList.isEmpty()) {
                Element guests = doc.createElement("guests");
	            rootElement.appendChild(guests);

                //  guests
                for(Guest eguest: guestsList) {
                    Element guest = doc.createElement("guest");
                    guests.appendChild(guest);
                    
                    Element gName = doc.createElement("name");
                    gName.appendChild(doc.createTextNode(eguest.getName()));
                    guest.appendChild(gName);

                    Element gSurname = doc.createElement("surname");
                    gSurname.appendChild(doc.createTextNode(eguest.getSurname()));
                    guest.appendChild(gSurname);
                    
                    Attr attr = doc.createAttribute("type");
	        	    attr.setValue(eguest.getType());
	        	    guest.setAttributeNode(attr);
	        	    

                }
	        	
            }
	        

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
	        	
	        	Element onlinePayment = doc.createElement("onlinepayment");
	        	onlinePayment.appendChild(doc.createTextNode(onlinePay));
	        	course.appendChild(onlinePayment);
	        	
	        	
	        		// write the content into xml file
	        		TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        		Transformer transformer = transformerFactory.newTransformer();
	        		DOMSource source = new DOMSource(doc);
	        		//StreamResult result = new StreamResult(new File("C:\\Users\\zuzanab\\Documents\\registration.xml"));
	        		//transformer.transform(source, result);
	          
	        		// Output to console for testing
	        		StreamResult res = new StreamResult(System.out);
	        		transformer.transform(source, res);
	        		transformer.transform(source, new StreamResult(sw));
	        		
	        		
	        		
	        		ByteArrayOutputStream out = new ByteArrayOutputStream();
	        		res.setOutputStream(out);
	        		transformer.transform(source, res);
	        		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
	        		StreamSource streamSource = new StreamSource(in);
	        			        		
	        		
	        	    StringWriter writer = new StringWriter();
	        	    StreamResult output = new StreamResult(writer);
	        	    TransformerFactory tFactory = TransformerFactory.newInstance();
	        	    Transformer tf = tFactory.newTransformer();
	        	    tf.transform(source,output);
	        	    strResult = writer.toString();
	        		
                    FileSaver.saveFile(writer, new String("xml"));

	        	}
	        	
	        	catch (Exception e) {
	        		
	        		System.out.println(e.getStackTrace().toString());
	        	}
	        	
	          
			System.out.println("STRING:");
			System.out.println(strResult);
	        	
	        return strResult;
	        	

	}

}