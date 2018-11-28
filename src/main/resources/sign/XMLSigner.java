package sign;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Scanner;

import exception.SignException;
import savingFile.FileSaver;
import sk.ditec.zep.dsigner.xades.XadesSig;
import sk.ditec.zep.dsigner.xades.plugin.DataObject;
import sk.ditec.zep.dsigner.xades.plugins.xmlplugin.XmlPlugin;

public class XMLSigner {

	public static void signDocument(String xmlFile) throws SignException {
		final XadesSig dSigner = new XadesSig();
		dSigner.installLookAndFeel();
		dSigner.installSwingLocalization();
		dSigner.reset();

		String xsdFile = getFile("ReservationSchema.xsd");
		String xsltFile = getFile("ReservationToHTML.xslt");

		XmlPlugin xmlPlugin = new XmlPlugin();
		DataObject xmlObject = xmlPlugin.createObject2("XMLDoc", //
				"XMLRegistration", //
				xmlFile, //
				xsdFile, //
				"", //
				"src/main/resources/ReservationSchema.xsd", //
				xsltFile, //
				"src/main/resources/ReservationToHTML.xslt", //
				"HTML");

		if (xmlObject == null) {
			throw new SignException("Error when creating xml object<br>"//
					+ "XMLPlugin.createObject() errorMessage=" + xmlPlugin.getErrorMessage());
		}

		int rc = dSigner.addObject(xmlObject);
		if (rc != 0) {
			throw new SignException("Error when adding xml object to DSigner<br>"//
					+ "XadesSig.addObject() errorCode=" + rc//
					+ ", errorMessage=" + dSigner.getErrorMessage());
		}

		rc = dSigner.sign20("signatureId20", //
				"http://www.w3.org/2001/04/xmlenc#sha256", //
				"urn:oid:1.3.158.36061701.1.2.2", //
				"registrationEnvelopeId", //
				"dataEnvelopeURI", //
				"dataEnvelopeDescr");
		
		//dzico
		System.out.println(rc);

		if (rc != 0) {
			throw new SignException("Error when signing using sign20<br>"//
					+ "XadesSig.sign20() errorCode=" + rc//
					+ ", errorMessage=" + dSigner.getErrorMessage());
		}

		System.out.println(dSigner.getSignedXmlWithEnvelope());

		StringWriter outputWriter = new StringWriter();
		outputWriter.write(dSigner.getSignedXmlWithEnvelope());
		
		System.out.println(outputWriter.toString());

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