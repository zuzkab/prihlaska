package sign;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Scanner;

import savingFile.FileSaver;
import sk.ditec.zep.dsigner.xades.XadesSig;
import sk.ditec.zep.dsigner.xades.plugin.DataObject;
import sk.ditec.zep.dsigner.xades.plugins.xmlplugin.XmlPlugin;

public class XMLSigner {

	public static void signDocument(String xmlFile) {
		final XadesSig dSigner = new XadesSig();
		dSigner.installLookAndFeel();
		dSigner.installSwingLocalization();
		dSigner.reset();
		
		String xsdFile = getFile("ReservationSchema.xsd");
		String xsltFile = getFile("ReservationToHTML.xslt");
		
		XmlPlugin xmlPlugin = new XmlPlugin();
		DataObject xmlObject = xmlPlugin.createObject2("XMLDoc", 
										"XMLRegistration",
										xmlFile,
										xsdFile,
										"", 
										"http://www.w3.org/2001/XMLSchema",
										xsltFile, 
										"http://www.w3.org/1999/XSL/Transform",
										"HTML");

		if (xmlObject == null) {
			System.out.println("XMLPlugin.createObject() errorMessage=" + xmlPlugin.getErrorMessage());
			return;
		}

		int rc = dSigner.addObject(xmlObject);
		if (rc != 0) {
			System.out.println("XadesSig.addObject() errorCode=" + rc + ", errorMessage=" + dSigner.getErrorMessage());
			return;
		}

		rc = dSigner.sign20("signatureId20", 
					"http://www.w3.org/2001/04/xmlenc#sha256", 
					"urn:oid:1.3.158.36061701.1.2.2", 
					"dataEnvelopeId",
					"dataEnvelopeURI", 
					"dataEnvelopeDescr");
		if (rc != 0) {
			System.out.println("XadesSig.sign20() errorCode=" + rc + ", errorMessage=" + dSigner.getErrorMessage());
			return;
		}	
		
		System.out.println(dSigner.getSignedXmlWithEnvelope());

		StringWriter outputWriter = new StringWriter();
		outputWriter.write(dSigner.getSignedXmlWithEnvelope());

		FileSaver.saveFile(outputWriter, new String("xml"));
	}
	
	  private static String getFile(String fileName) {

			StringBuilder result = new StringBuilder("");

			ClassLoader classLoader = new XMLSigner().getClass().getClassLoader();
			File file = new File(classLoader.getResource(fileName).getFile());

			try (Scanner scanner = new Scanner(file)) {

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}

				scanner.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
				
			return result.toString();

		  }
}
