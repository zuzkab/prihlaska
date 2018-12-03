package sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import savingFile.FileSaver;
import soap.SOAPClient;
import util.Util;

public class TimeStamp {
	public static void addTimeStamp()
			throws SAXException, IOException, ParserConfigurationException, DOMException, SOAPException, TSPException {
		File signedFile = Util.getFile();

		InputStream inputStream = new FileInputStream(signedFile);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);

		NodeList nList = doc.getElementsByTagName("ds:SignatureValue");
		String signedValue = null;

		if (nList.getLength() == 1) {
			Node nNode = nList.item(0);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				signedValue = nNode.getTextContent();
				System.out.println("Selected signed value: " + signedValue);
			}

			System.out.println(signedValue);
			SOAPMessage response = SOAPClient.getTimestamp(signedValue);

			nNode = doc.getElementsByTagName("xades:QualifyingProperties").item(0);

			Node newNode = nNode.appendChild(doc.createElement("xades:UnsignedProperties"));
			newNode = newNode.appendChild(doc.createElement("xades:UnsignedSignatureProperties"));

			Attr idAttribute = doc.createAttribute("Id");
			idAttribute.setValue("time_stamp_001");
			Element signatureTS = doc.createElement("xades:SignatureTimeStamp");
			signatureTS.setAttributeNode(idAttribute);

			newNode = newNode.appendChild(signatureTS);
			newNode = newNode.appendChild(doc.createElement("xades:EncapsulatedTimeStamp")); // base64 value,
																								// optionalatributy
			// (Id, Encoding)
			TimeStampResponse tsRes = new TimeStampResponse(
					Base64.getDecoder().decode(response.getSOAPBody().getTextContent().getBytes("UTF-8")));
			newNode.appendChild(
					doc.createTextNode(new String(Base64.getEncoder().encode(tsRes.getTimeStampToken().getEncoded()))));

			StringWriter outputWriter = new StringWriter();
			outputWriter.write(Util.xmlToString(doc));

			FileSaver.saveFile(outputWriter, new String("xml"));
		}
	}
}
