package validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import util.Util;

public class ValidationXades {

	private static final String XMLNS_XZEP = "http://www.ditec.sk/ep/signature_formats/xades _zep/v1.0";
	private static final String XMLNS_DS = "http://www.w3.org/2000/09/xmldsig#";

	private static final String BOM = "\\uFEFF";

	public static class ValidationError {
		Element errorElement;
		String errorText;

		public ValidationError(Element errorElement, String errorText) {
			this.errorElement = errorElement;
			this.errorText = errorText;
		}

		public String toString() {
			return errorElement.getTagName() + ": " + errorText;
		}
	}

	public static List<ValidationError> validateXades() throws IOException, ParserConfigurationException, SAXException {
		File xadesFile = Util.getFile();
		xadesFile = removeBOM(xadesFile);

		InputStream inputStream = new FileInputStream(xadesFile);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);

		return validateXades(doc);
	}

	private static List<ValidationError> validateXades(Document doc) {
		List<ValidationError> validationErrors = new ArrayList<>();

		validationErrors.addAll(validateXadesEnvelope(doc));

		return validationErrors;
	}

	private static List<ValidationError> validateXadesEnvelope(Document doc) {
		List<ValidationError> validationErrors = new ArrayList<>();

		Element envelopeElement = doc.getDocumentElement();
		if (!XMLNS_XZEP.equals(envelopeElement.getAttribute("xmlns:xzep"))) {
			validationErrors.add(
					new ValidationError(envelopeElement, "Does not have attribute xmlns:xzep equal to: " + XMLNS_XZEP));
		}

		if (!XMLNS_DS.equals(envelopeElement.getAttribute("xmlns:ds"))) {
			validationErrors.add(
					new ValidationError(envelopeElement, "Does not have attribute xmlns:ds equal to: " + XMLNS_XZEP));
		}

		return validationErrors;
	}

	private static File removeBOM(File f) throws IOException {
		List<String> lines = Files.readAllLines(f.toPath(), Charset.forName("UTF-8"));
		String allLines = lines//
				.stream()//
				.map(line -> line.replaceAll(BOM, ""))//
				.reduce("", (u, v) -> u + "\n" + v);

		Files.write(f.toPath(), allLines.getBytes());

		return f;
	}
}
