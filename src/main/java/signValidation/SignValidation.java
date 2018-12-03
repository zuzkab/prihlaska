package signValidation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import exception.SignException;
import exception.SignValidationException;

public class SignValidation {
	
	public static void validateSignedDoc() throws ParserConfigurationException, SAXException, IOException, CertificateException {
		File signedFile = getFile();

		InputStream inputStream= new FileInputStream(signedFile);
	    Reader reader = new InputStreamReader(inputStream,"UTF-8");
	    InputSource is = new InputSource(reader);
	    is.setEncoding("UTF-8");
	    
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(signedFile);
	    
	    String errors = "";
	   errors = errors.concat(checkRootElement(doc));
	    
	   errors = errors.concat(checkSignatureMethod(doc));
	   errors = errors.concat(checkCanonicalizationMethod(doc));
	    
	   errors = errors.concat(checkTransforms(doc));
	   errors = errors.concat(checkDigestMethod(doc));
	    
	   errors = errors.concat(checkOtherElements(doc));
	    
	   errors = errors.concat(checkKeyInfo(doc));
	    
	   errors = errors.concat(checkSignatureProperties(doc));
	    
	   errors = errors.concat(checkManifest(doc));
	    
	    if (!errors.equals("")) 
	    	throw new SignValidationException(errors);
	    else
	    	System.out.println("VALIDATION SUCCESSFULL");
	    
	}
	
	/*
	 * overenie ds:Manifest elementov:
	 * každý ds:Manifest element musí mať Id atribút,
	 * overenie hodnoty Type atribútu voči profilu XAdES_ZEP,
	 * každý ds:Manifest element musí obsahovať práve jednu referenciu na ds:Object
	 */
	public static String checkManifest(Document doc) {
		String errors = "";
		NodeList manifestList = doc.getElementsByTagName("ds:Manifest");
		
		for (int i = 0; i < manifestList.getLength(); i++) {
			Element manifest = (Element) manifestList.item(i);
			
			if (!manifest.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:Manifest! \n");
			}
			NodeList children = manifest.getChildNodes();
			
			if (children.getLength() == 1) {
				if (children.item(0).getNodeName().equals("ds:Reference")) {
					if (((Element)children.item(0)).hasAttribute("Type")) {
						if (!((Element)children.item(0)).getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#Object")) {
							errors = errors.concat("Invalid Type attribute value in ds:Manifest! \n");
						}
					}
					else {
						errors = errors.concat("Missing Type attribute in ds:Manifest! \n");
					}
					
					Element refObject = doc.getElementById(((Element) children.item(0)).getAttribute("URI"));
					if (refObject == null || !refObject.getNodeName().equals("ds:Object")) {
						errors = errors.concat("Reference to invalid element in ds:Manifest! \n");
					}
				}
				else {
					errors = errors.concat("Invalid element in ds:Manifest! \n");
				}
			}
			else {
				errors = errors.concat("Invalid elements in ds:Manifest! \n");
			}		
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * overenie obsahu ds:SignatureProperties:
	 * musí mať Id atribút,
	 * musí obsahovať dva elementy ds:SignatureProperty pre xzep:SignatureVersion a xzep:ProductInfos,
	 * obidva ds:SignatureProperty musia mať atribút Target nastavený na ds:Signature
	 */
	public static String checkSignatureProperties(Document doc) {
		String errors = "";
		Element sigProp = (Element) doc.getElementsByTagName("ds:SignatureProperties").item(0);
		if (!sigProp.hasAttribute("Id")) {
			errors = errors.concat("Missing Id attribute in ds:SignatureProperties! \n");
		}
		NodeList sigPropChild = sigProp.getChildNodes();
		
		if (sigPropChild.getLength() != 2) {
			errors = errors.concat("Invalid count of ds:SignatureProperty elements in ds:SignatureProperties! \n");
		}
		else {
			if (sigPropChild.item(0).getNodeName().equals("ds:SignatureProperty") && sigPropChild.item(1).getNodeName().equals("ds:SignatureProperty")) {
				Element sigProp1 = (Element) sigPropChild.item(0);
				Element sigProp2 = (Element) sigPropChild.item(1);
				boolean sv = false;
				boolean pi = false;
				
				if (sigProp1.hasAttribute("Target")) {
					if (!sigProp1.getAttribute("Target").equals(((Element)doc.getElementsByTagName("ds:Signature").item(0)).getAttribute("Id"))) {
						errors = errors.concat("Invalid Target attribute in ds:SignatureProperty! \n");
					}
				}
				else {
					errors = errors.concat("Missing Id attribute in ds:SignatureProperty! \n");
				}
				
				if (sigProp2.hasAttribute("Target")) {
					if (!sigProp2.getAttribute("Target").equals(((Element)doc.getElementsByTagName("ds:Signature").item(0)).getAttribute("Id"))) {
						errors = errors.concat("Invalid Target attribute in ds:SignatureProperty! \n");
					}
				}
				else {
					errors = errors.concat("Missing Id attribute in ds:SignatureProperty! \n");
				}
		
				if (sigProp1.getFirstChild().getNodeName().equals("xzep:ProductInfos") || sigProp2.getFirstChild().getNodeName().equals("xzep:ProductInfos")) {
					pi = true;
				}
				if (sigProp1.getFirstChild().getNodeName().equals("xzep:SignatureVersion") || sigProp2.getFirstChild().getNodeName().equals("xzep:SignatureVersion")) {
					sv = true;
				}
				
				if (!sv) {
					errors = errors.concat("Missing xzep:SignatureVersion element in ds:SignatureProperty! \n");
				}
				if (!pi) {
					errors = errors.concat("Missing xzep:ProductInfos element in ds:SignatureProperty! \n");
				}
				
			}
			else {
				errors = errors.concat("Invalid elements in ds:SignatureProperties! \n");
			}
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * overenie obsahu ds:KeyInfo:
	 * musí mať Id atribút,
	 * musí obsahovať ds:X509Data, ktorý obsahuje elementy: ds:X509Certificate, ds:X509IssuerSerial, ds:X509SubjectName,
	 * hodnoty elementov ds:X509IssuerSerial a ds:X509SubjectName súhlasia s príslušnými hodnatami v certifikáte, 
	 * ktorý sa nachádza v ds:X509Certificate
	 */
	public static String checkKeyInfo(Document doc) throws CertificateException {
		String errors = "";
		NodeList keyInfoList = doc.getElementsByTagName("ds:KeyInfo");
		if (keyInfoList.getLength() == 1) {
			Element keyInfo = (Element) keyInfoList.item(0);
			
			if (!keyInfo.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:KeyInfo! \n");
			}
			
			NodeList nl = keyInfo.getElementsByTagName("ds:X509Certificate");
			Element data = null;
			if (nl.getLength() > 0) {
				data = (Element) nl.item(0);
			}
			
			boolean certificate = false;
			boolean issuerSerial = false;
			boolean subjectName = false;
			
			if (data != null && data.getNodeName().equals("ds:X509Data")) {
				NodeList chNodes = data.getChildNodes();
				for (int i = 0; i < chNodes.getLength(); i++) {
					if (chNodes.item(i).getNodeName().equals("ds:X509Certificate")) {
						certificate = true;
					}
					else if (chNodes.item(i).getNodeName().equals("ds:X509IssuerSerial")) {
						issuerSerial = true;
					}
					else if (chNodes.item(i).getNodeName().equals("ds:X509SubjectName")) {
						subjectName = true;
					}
					else {
						errors = errors.concat("Invalid element in ds:X509Data! \n");
					}
				}
				
				if (!certificate) {
					errors = errors.concat("Missing ds:X509Certificate element in ds:X509Data! \n");
				}
				if (!issuerSerial) {
					errors = errors.concat("Missing ds:X509IssuerSerial element in ds:X509Data! \n");
				}
				if (!subjectName) {
					errors = errors.concat("Missing ds:X509SubjectName element in ds:X509Data! \n");
				}
				
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(data.getElementsByTagName("ds:X509Certificate").item(0).getTextContent().getBytes()));
				X509Certificate cert = (X509Certificate)cf.generateCertificate(stream);
				JcaX509CertificateHolder certHolder = new JcaX509CertificateHolder(cert);
				String issuer = certHolder.getIssuer().toString();
				if (!issuer.equals(data.getElementsByTagName("ds:X509IssuerName").item(0).getTextContent())) {
					errors = errors.concat("ds:X509IssuerName is not equal with issuer name in ds:X509Certificate! \n");
				}
		
				String serial = certHolder.getSerialNumber().toString();
				if (!serial.equals(data.getElementsByTagName("ds:X509SerialNumber").item(0).getTextContent())) {
					errors = errors.concat("ds:X509SerialNumber is not equal with serial number in ds:X509Certificate! \n");
				}
				
				String subject = certHolder.getSubject().toString();
				if (!subject.equals(data.getElementsByTagName("ds:X509SubjectName").item(0).getTextContent())) {
					errors = errors.concat("ds:X509SubjectName is not equal with subject name in ds:X509Certificate! \n");
				}
			}
			else {
				errors = errors.concat("Missing ds:X509Data element in ds:KeyInfo! \n");
			}
		}
		else {
			errors = errors.concat("Invalid count of ds:KeyInfo elements! \n");
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * overenie ostatných elementov profilu XAdES_ZEP, 
	 * ktoré prináležia do špecifikácie XML Signature
	 */
	public static String checkOtherElements(Document doc) {
		String errors = "";
		
		/*
		 * ds:Signature:
		 * musí mať Id atribút,
		 * musí mať špecifikovaný namespace xmlns:ds
		 */
		NodeList signatureList = doc.getElementsByTagName("ds:Signature");
		if (signatureList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:Signature elements! \n");
		}
		else {
			Element id = (Element) signatureList.item(0);
			if (!id.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:Signature! \n");
			}
			
			Element signature = (Element) signatureList.item(0);
			if (signature.hasAttribute("xmlns:ds")) {
				if (!signature.getAttribute("xmlns:ds").equals("http://www.w3.org/2000/09/xmldsig#")){
					errors = errors.concat("Invalid xmlns:ds attribute value in ds:Signature! \n");
				}
			}
			else {
				errors = errors.concat("Missing xmlns:ds attribute in ds:Signature! \n");
			}
		}
		
		/*
		 * ds:SignatureValue:
		 * musí mať Id atribút
		 */
		NodeList signatureValueList = doc.getElementsByTagName("ds:SignatureValue");
		if (signatureValueList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:SignatureValue elements! \n");
		}
		else {
			Element id = (Element) signatureValueList.item(0);
			if (!id.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:SignatureValue! \n");
			}
		}
		
		/*
		 * overenie existencie referencií v ds:SignedInfo a hodnôt atribútov Id a Type voči profilu XAdES_ZEP pre:
		 * ds:KeyInfo element,
		 * ds:SignatureProperties element,
		 * xades:SignedProperties element,
		 * všetky ostatné referencie v rámci ds:SignedInfo musia byť referenciami na ds:Manifest elementy
		 */
		NodeList signatureInfoList = doc.getElementsByTagName("ds:SignedInfo");
		Element signatureInfo = (Element) signatureInfoList.item(0);
		NodeList references = signatureInfo.getElementsByTagName("ds:Reference");
		boolean kiRef = false;
		boolean sgtrRef = false;
		boolean sgdRef = false;
		
		for (int i = 0; i < references.getLength(); i++) {
			Element reference = (Element) references.item(i);
			if (reference.hasAttribute("Id")) {
				if (reference.hasAttribute("Type")) {
					if (reference.hasAttribute("URI")) {
						if (reference.getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#Object")) {
							Element refElement = doc.getElementById(reference.getAttribute("URI"));
							if (refElement != null) {
								if (refElement.getTagName().equals("ds:KeyInfo")) {
									kiRef = true;
								}
								else {
									errors = errors.concat("Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							}
							else {
								errors = errors.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						}
						else if (reference.getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#SignatureProperties")) {
							Element refElement = doc.getElementById(reference.getAttribute("URI"));
							if (refElement != null) {
								if (refElement.getTagName().equals("ds:SignatureProperties")) {
									sgtrRef = true;
								}
								else {
									errors = errors.concat("Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							}
							else {
								errors = errors.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						}
						else if (reference.getAttribute("Type").equals("http://uri.etsi.org/01903#SignedProperties")) {
							Element refElement = doc.getElementById(reference.getAttribute("URI"));
							if (refElement != null) {
								if (refElement.getTagName().equals("xades:SignedProperties")) {
									sgdRef = true;
								}
								else {
									errors = errors.concat("Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							}
							else {
								errors = errors.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						}
						else if (reference.getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#Manifest")) {
							Element refElement = doc.getElementById(reference.getAttribute("URI"));
							if (refElement != null) {
								if (!refElement.getTagName().equals("ds:Manifest")) {
									errors = errors.concat("Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							}
							else {
								errors = errors.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						}
						else {
							errors = errors.concat("Invalid Type attribute value in ds:Reference in ds:SignedInfo! \n");
						}
					}
					else {
						errors = errors.concat("Missing URI attribute in ds:Reference in ds:SignedInfo! \n");
					}
				}
				else {
					errors = errors.concat("Missing Type attribute in ds:Reference in ds:SignedInfo! \n");
				}
			}
			else {
				errors = errors.concat("Missing Id attribute in ds:Reference in ds:SignedInfo! \n");
			}
		}	
		
		if (!kiRef) {
			errors = errors.concat("Missing ds:Reference to ds:KeyInfo in ds:SignedInfo! \n");
		}
		if (!sgtrRef) {
			errors = errors.concat("Missing ds:Reference to ds:SignatureProperties in ds:SignedInfo! \n");
		}
		if (!sgdRef) {
			errors = errors.concat("Missing ds:Reference to xades:SignedProperties in ds:SignedInfo! \n");
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * kontrola obsahu ds:DigestMethod 
	 * vo všetkých referenciách v ds:SignedInfo 
	 * musi obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP
	 */
	public static String checkDigestMethod(Document doc) {
		String errors = "";
		NodeList dmList = doc.getElementsByTagName("ds:DigestMethod");
		
		for (int i = 0; i < dmList.getLength(); i++) {
			Element dm = (Element) dmList.item(i);
			if (dm.hasAttribute("Algorithm")) {
				if (!(dm.getAttribute("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#sha1") || 
						dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig#sha224") ||
						dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmlenc#sha256") ||
						dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#sha384") ||
						dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmlenc#sha512") )){
					errors = errors.concat("Invalid Algorithm attribute value in ds:DigestMethod! \n");
				}
			}
			else {
				errors = errors.concat("Missing Algorithm attribute in ds:DigestMethod! \n");
			}
		}	
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * kontrola obsahu ds:Transforms
	 * vo všetkých referenciách v ds:SignedInfo 
	 * musi obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP
	 */
	public static String checkTransforms(Document doc) {
		String errors = "";
		NodeList tList = doc.getElementsByTagName("ds:Transform");
		
		for (int i = 0; i < tList.getLength(); i++) {
			Element t = (Element) tList.item(i);
			if (t.hasAttribute("Algorithm")) {
				if (!t.getAttribute("Algorithm").equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")){
					errors = errors.concat("Invalid Algorithm attribute value in ds:Transform! \n");
				}
			}
			else {
				errors = errors.concat("Missing Algorithm attribute in ds:Transform! \n");
			}
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * kontrola obsahu ds:SignatureMethod 
	 * musi obsahovať URI niektorého z podporovaných algoritmov 
	 * pre dané elementy podľa profilu XAdES_ZEP
	 */
	public static String checkSignatureMethod(Document doc) {
		String errors = "";
		NodeList smList = doc.getElementsByTagName("ds:SignatureMethod");
		
		if (smList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:SignatureMethod elements! \n");
		}
		else {
			Element cm = (Element) smList.item(0);
			if (cm.hasAttribute("Algorithm")) {
				if (!(cm.getAttribute("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1") || 
						cm.getAttribute("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1") ||
						cm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256") ||
						cm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384") ||
						cm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512") )){
					errors = errors.concat("Invalid Algorithm attribute value in ds:SignatureMethod! \n");
				}
			}
			else {
				errors = errors.concat("Missing Algorithm attribute in ds:SignatureMethod! \n");
			}
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * kontrola obsahu ds:CanonicalizationMethod 
	 * musi obsahovať URI niektorého z podporovaných algoritmov 
	 * pre dané elementy podľa profilu XAdES_ZEP
	 */
	public static String checkCanonicalizationMethod(Document doc) {
		String errors = "";
		NodeList cmList = doc.getElementsByTagName("ds:CanonicalizationMethod");
		
		if (cmList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:CanonicalizationMethod elements! \n");
		}
		else {
			Element cm = (Element) cmList.item(0);
			if (cm.hasAttribute("Algorithm")) {
				if (!cm.getAttribute("Algorithm").equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
					errors = errors.concat("Invalid Algorithm attribute value in ds:CanonicalizationMethod! \n");
				}
			}
			else {
				errors = errors.concat("Missing Algorithm attribute in ds:CanonicalizationMethod! \n");
			}
		}
		
		System.out.println(errors);
		return errors;
	}
	
	/*
	 * koreňový element musí obsahovať atribúty xmlns:xzep 
	 * a xmlns:ds podľa profilu XADES_ZEP
	 */
	public static String checkRootElement(Document doc) {
		String errors = "";
		
		Element root = doc.getDocumentElement();
		if (root.hasAttribute("xmlns:xzep")) {
			String xzep = root.getAttribute("xmlns:xzep");
			if (!xzep.equals("http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0")) {
				errors = errors.concat("Invalid xmlns:zep attribute value in root element! \n");
			}
		}
		else {
			errors = errors.concat("Missing xmlns:zep attribute in root element! \n");
		}
		
		if (root.hasAttribute("xmlns:ds")) {
			String ds = root.getAttribute("xmlns:ds");
			if (!ds.equals("http://www.w3.org/2000/09/xmldsig#")) {
				errors = errors.concat("Invalid xmlns:ds attribute value in root element! \n");
			}
		}
		else {
			errors = errors.concat("Missing xmlns:ds attribute in root element! \n");
		}		
		
		System.out.println(errors);
		return errors;
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