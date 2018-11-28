package sign;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;
 import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import soap.SOAPClient;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Attr;


 public class TimeStamp {
	public static void addTimeStamp() throws SAXException, IOException, ParserConfigurationException {
		File signedFile = getFile();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(signedFile);
		
		NodeList nList = doc.getElementsByTagName("ds:SignatureValue");
		String signedValue = null;
		
		if (nList.getLength() == 1) {
			Node nNode = nList.item(0);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				signedValue = nNode.getTextContent();			
				System.out.println("Selected signed value: " + signedValue);
			}
			
			String signedValue64 = Base64.getEncoder().encodeToString(signedValue.getBytes("utf-8"));
			System.out.println(signedValue64); 
			SOAPMessage response = SOAPClient.getTimestamp(signedValue64);
		}
		
		
		Element QualifyingProperties = (Element)doc.getElementsByTagName("xades:QualifyingProperties");
	          
	    Element UnsignedProperties = doc.createElement("xades:UnsignedProperties");
	    QualifyingProperties.appendChild(UnsignedProperties);
	    
	    Element UnsignedSignatureProperties = doc.createElement("xades:UnsignedSignatureProperties");
	    UnsignedProperties.appendChild(UnsignedSignatureProperties);
	    
	    Element SignatureTimeStamp = doc.createElement("xades:SignatureTimeStamp");
	    UnsignedSignatureProperties.appendChild(SignatureTimeStamp);
	    
	    Attr attr = doc.createAttribute("Id");
	    attr.setValue("Id");
	    SignatureTimeStamp.setAttributeNode(attr);
	    
	    Element CanonicalizationMethod = doc.createElement("ds:CanonicalizationMethod");
	    SignatureTimeStamp.appendChild(CanonicalizationMethod);
	    
	    Attr attrAlgorithm = doc.createAttribute("Algorithm");
	    attr.setValue("Algorithm");
	    CanonicalizationMethod.setAttributeNode(attr);
	    
	    Element EncapsulatedTimeStamp = doc.createElement("xades:EncapsulatedTimeStamp");
	    SignatureTimeStamp.appendChild(EncapsulatedTimeStamp);
	    
	    Attr attrId = doc.createAttribute("Id");
	    attr.setValue("Id");
	    EncapsulatedTimeStamp.setAttributeNode(attr);
	
	}
	
	public static File getFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			System.out.println("Selected file: " + fileChooser.getSelectedFile().getAbsolutePath());
		    return fileChooser.getSelectedFile();
		}
		return null;
	}
 }