package signValidation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.Transforms;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;

import exception.SignValidationException;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.bouncycastle.asn1.DEREnumerated;
import static org.bouncycastle.asn1.cms.CMSObjectIdentifiers.data;
import static org.bouncycastle.asn1.isismtt.ocsp.RequestedCertificate.certificate;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import org.w3c.dom.Node;


public class SignValidation {

	private static final List<String> TRANSFORMATIONS = Arrays.asList(//
			"http://www.w3.org/TR/2001/REC-xml-c14n-20010315", //
			"http://www.w3.org/2001/04/xmlenc#sha256");

	private static final Map<String, String> DIGEST_ALGORITHMS = initializeDigestAlgorithms();

	public static void validateSignedDoc() throws ParserConfigurationException, SAXException, IOException,
			CertificateException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, TransformerException,
			TransformException, DOMException, XMLSignatureException, InvalidTransformException, XMLSecurityException,
			MarshalException, javax.xml.crypto.dsig.XMLSignatureException, XPathExpressionException, CMSException, TSPException, UnsupportedEncodingException, CRLException {
		File signedFile = getFile();

		InputStream inputStream = new FileInputStream(signedFile);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(signedFile);

		String errors = "";
                
                errors = errors.concat(check_timestamp(doc));
                errors = errors.concat(is_revoked(doc));
                
		errors = errors.concat(checkRootElement(doc));

		errors = errors.concat(checkSignatureMethod(doc));
		errors = errors.concat(checkCanonicalizationMethod(doc));

		errors = errors.concat(checkTransforms(doc));
		errors = errors.concat(checkDigestMethod(doc));

		errors = errors.concat(checkOtherElements(doc));

		errors = errors.concat(checkKeyInfo(doc));

		errors = errors.concat(checkSignatureProperties(doc));

		errors = errors.concat(checkManifest(doc));

		errors = errors.concat(checkCoreValidation(doc));

		if (!errors.equals(""))
			throw new SignValidationException(errors);
		else
			System.out.println("VALIDATION SUCCESSFULL");

	}
        
        /*
        Overenie časovej pečiatky:
	- overenie platnosti podpisového certifikátu časovej pečiatky 
        voči času UtcNow a voči platnému poslednému CRL.
	- overenie MessageImprint z časovej pečiatky voči podpisu ds:SignatureValue

        Katka
        */
        public static String check_timestamp(Document doc) throws CMSException, UnsupportedEncodingException, TSPException, IOException, CertificateException, CRLException {
            String errors="";
            
            String encapsulatedTimeStamp = doc.getElementsByTagName("xades:EncapsulatedTimeStamp").item(0).getChildNodes().item(0).getNodeValue();
            byte[] tspBinaries = Base64.getDecoder().decode(encapsulatedTimeStamp.getBytes("UTF-8"));
            TimeStampToken token = new TimeStampToken(new CMSSignedData(tspBinaries));       
            
            //timestamp time vs UtcNow
            Date timestamp_time = token.getTimeStampInfo().getGenTime();
            OffsetDateTime now1 = OffsetDateTime.now(ZoneOffset.UTC);
            //DateTime utc = new DateTime(DateTimeZone.UTC);

            
            //timestamp time vs CRL
            CMSSignedData cmssigned_data = token.toCMSSignedData();
            Collection<X509Certificate> result = new HashSet<X509Certificate>();
            Store<?> certStore = cmssigned_data.getCertificates();
            SignerInformationStore signers = cmssigned_data.getSignerInfos();
            Iterator<?> it = signers.getSigners().iterator();
            while (it.hasNext()) {
		SignerInformation signer = (SignerInformation) it.next();
		@SuppressWarnings("unchecked")
		Collection<?> certCollection = certStore.getMatches(signer.getSID());
		Iterator<?> certIt = certCollection.iterator();
		X509CertificateHolder certificateHolder = (X509CertificateHolder) certIt.next();
		try {
			result.add(new JcaX509CertificateConverter().getCertificate(certificateHolder));
		} catch (CertificateException error) {
		}
            }  
           
            X509CRLEntry revokedCertificate = null;
            X509CRL crl = null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for (X509Certificate cert : result) {
                byte[] crlDP = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());

                ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crlDP));
                ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
                DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
                oAsnInStream.close();

                byte[] crldpExtOctets = dosCrlDP.getOctets();
                ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
                ASN1Primitive derObj2 = oAsnInStream2.readObject();
                CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);

                oAsnInStream2.close();

                List<String> crlUrls = new ArrayList<String>();
                for (DistributionPoint dp : distPoint.getDistributionPoints())
                {
                    DistributionPointName dpn = dp.getDistributionPoint();
                    // Look for URIs in fullName
                    if (dpn != null)
                    {
                        if (dpn.getType() == DistributionPointName.FULL_NAME)
                        {
                            GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                            // Look for an URI
                            for (int j = 0; j < genNames.length; j++)
                            {
                                if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier)
                                {
                                    String url = DERIA5String.getInstance(genNames[j].getName()).getString();
                                    crlUrls.add(url);
                                }
                            }
                        }
                    }
                }

                for (String _url : crlUrls) {
                    System.out.println(_url);
                
                    URL url = new URL(_url);
                    URLConnection connection = url.openConnection();

                    try(DataInputStream inStream = new DataInputStream(connection.getInputStream())){

                        crl = (X509CRL)cf.generateCRL(inStream);
                    }

                    revokedCertificate = crl.getRevokedCertificate(cert.getSerialNumber());
                               

                    if(revokedCertificate !=null){
                        System.out.println("Revoked");
                    
                        //if revoked, get revocation date
                        Date revocationDate = revokedCertificate.getRevocationDate();
                    
                         //get TimeStampDate
                    
                        //compare it
                        if (revocationDate.compareTo(timestamp_time) < 0) {
                            errors = errors.concat("Certificate was revoked. ");
                        }
                        else {
                            System.out.println("DEBUG: Certificate valid at timestamping");
                        }
                    }
                
                    else {
                        System.out.println("DEBUG: Certificate valid, no CRL entry ");
                    }
            
                }
            
            }
            //message imprint vs ds:signatureValue
            
            //get message imprint digest      
            byte[] messageImprintDigest = token.getTimeStampInfo().getMessageImprintDigest();
            
            //Overenie message imprint z časovej pečiatky:
            //Porovnám digest value z message imprint so signatureValue a keď sa to nezhoduje tak pečiatka nepatrí k podpisu
                      
            //get signature value
            String signatureValue = doc.getElementsByTagName("ds:signatureValue").item(0).getChildNodes().item(0).getNodeValue();
            byte[] signatureValueBytes = signatureValue.getBytes("UTF-8");
            
            
            if (Arrays.equals(messageImprintDigest, signatureValueBytes))
            {
                System.out.println("OK");
            } else {
                errors = errors.concat("digest from message imprint != signatureValue "); 
            }
            
            return errors;
        }
        
        /*
        Overenie platnosti podpisového certifikátu:
	- Overenie platnosti podpisového certifikátu dokumentu voči 
        času T z časovej pečiatky a voči platnému poslednému CRL.

        Katka
        */        
        public static String is_revoked (Document doc) throws CMSException, TSPException, IOException {
                       
        String errors = "";
        
        String encapsulatedTimeStamp = doc.getElementsByTagName("xades:EncapsulatedTimeStamp").item(0).getChildNodes().item(0).getNodeValue();

        byte[] tspBinaries = Base64.getDecoder().decode(encapsulatedTimeStamp.getBytes("UTF-8"));
        TimeStampToken token = new TimeStampToken(new CMSSignedData(tspBinaries));
        
        Date timestamp_date = token.getTimeStampInfo().getGenTime();
        
               
        try  {
            
            X509CRLEntry revokedCertificate = null;
            X509CRL crl = null;
            
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(doc.getElementsByTagName("ds:X509Certificate").item(0).getTextContent().getBytes()));
            System.out.println(stream);
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
            System.out.println(certificate);
            JcaX509CertificateHolder certHolder = new JcaX509CertificateHolder(certificate);
            System.out.println(certHolder);
            
            byte[] crlDistributionPointDerEncodedArray = certificate.getExtensionValue(Extension.cRLDistributionPoints.getId());

            ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crlDistributionPointDerEncodedArray));
            ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
            DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;

            oAsnInStream.close();

            byte[] crldpExtOctets = dosCrlDP.getOctets();
            ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
            ASN1Primitive derObj2 = oAsnInStream2.readObject();
            CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);

            oAsnInStream2.close();

            List<String> crlUrls = new ArrayList<String>();
            for (DistributionPoint dp : distPoint.getDistributionPoints())
            {
                DistributionPointName dpn = dp.getDistributionPoint();
                // Look for URIs in fullName
                if (dpn != null)
                {
                    if (dpn.getType() == DistributionPointName.FULL_NAME)
                    {
                        GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                        // Look for an URI
                        for (int j = 0; j < genNames.length; j++)
                        {
                            if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier)
                            {
                                String url = DERIA5String.getInstance(genNames[j].getName()).getString();
                                crlUrls.add(url);
                            }
                        }
                    }
                }
            }

            for (String _url : crlUrls) {
                System.out.println(_url);
                
                URL url = new URL(_url);
                URLConnection connection = url.openConnection();

                try(DataInputStream inStream = new DataInputStream(connection.getInputStream())){

                    crl = (X509CRL)cf.generateCRL(inStream);
                }

                revokedCertificate = crl.getRevokedCertificate(certificate.getSerialNumber());
                               

                if(revokedCertificate !=null){
                    System.out.println("Revoked");
                    
                    //if revoked, get revocation date
                    Date revocationDate = revokedCertificate.getRevocationDate();
                    
                    //get TimeStampDate
                    
                    //compare it
                    if (revocationDate.compareTo(timestamp_date) < 0) {
                        errors = errors.concat("Certificate was revoked. ");
                    }
                    else {
                        System.out.println("DEBUG: Certificate valid at timestamping");
                    }
                }
                
                else {
                    System.out.println("DEBUG: Certificate valid, no CRL entry ");
                }
            
            }
                      
        
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        
        
        System.out.println("DEBUG: end of CRLcheck function"); 
        return errors;
        }

	/*
	 * • Core validation (podľa špecifikácie XML Signature) – overenie hodnoty
	 * podpisu ds:SignatureValue a referencií v ds:SignedInfo: • dereferencovanie
	 * URI, kanonikalizácia referencovaných ds:Manifest elementov a overenie hodnôt
	 * odtlačkov ds:DigestValue, • kanonikalizácia ds:SignedInfo a overenie hodnoty
	 * ds:SignatureValue pomocou pripojeného podpisového certifikátu v ds:KeyInfo,
	 * 
	 * Miro
	 */
	public static String checkCoreValidation(Document doc) throws CertificateException, XMLSecurityException,
			MarshalException, javax.xml.crypto.dsig.XMLSignatureException, XPathExpressionException {
		String errors = "";

		Element signedInfo = (Element) doc.getElementsByTagName("ds:SignedInfo").item(0);
		NodeList references = signedInfo.getElementsByTagName("ds:Reference");
		for (int i = 0; i < references.getLength(); i++) {
			Element reference = (Element) references.item(i);
			String referenceURI = reference.getAttribute("URI");
			Element referencedNode = getElementById(doc, referenceURI);

			if (referencedNode != null) {
				System.out.println(referencedNode.getAttribute("Id"));
				referencedNode.setIdAttributeNode(referencedNode.getAttributeNode("Id"), true);
			} else {
				errors = errors.concat("Object with id=\"" + referenceURI.substring(1) + "\" was not found.");
			}
		}

		if (errors.isEmpty()) {
			try {
				XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
				NodeList nl = doc.getElementsByTagName("ds:Signature");
				DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(), nl.item(0));
				XMLSignature signature = fac.unmarshalXMLSignature(valContext);
				boolean coreValidity = signature.validate(valContext);

				if (!coreValidity) {
					errors = errors.concat("Core validation failed: SignatureValue for SignedInfo does not match.");
				}
			} catch (Exception e) {
				errors = errors.concat("Core validation failed: " + e.getMessage());
				e.printStackTrace();
			}
		}

		return errors;
	}

	/*
	 * overenie referencií v elementoch ds:Manifest: dereferencovanie URI,
	 * aplikovanie príslušnej ds:Transforms transformácie (pri base64 decode),
	 * overenie hodnoty ds:DigestValue,
	 * 
	 * Miro
	 */
	public static String checkManifest(Element manifestRef, Element manifestRefObject) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, TransformerException, TransformException, IOException, DOMException,
			XMLSignatureException, InvalidTransformException, XMLSecurityException {
		String errors = "";

		byte[] data = new byte[0];
		NodeList transformsElements = manifestRef.getElementsByTagName("ds:Transforms");
		for (int i = 0; i < transformsElements.getLength(); i++) {
			Element transformsElement = (Element) transformsElements.item(i);

			NodeList transformElements = manifestRef.getElementsByTagName("ds:Transform");
			for (int j = 0; j < transformElements.getLength(); j++) {
				Element transformElement = (Element) transformElements.item(i);
				if (transformElement.hasAttribute("Algorithm")) {
					String transAlg = transformElement.getAttribute("Algorithm");
					if (TRANSFORMATIONS.contains(transAlg)) {
						data = applyTransforms(transformsElement, manifestRefObject);
						Element digestMethodElement = (Element) manifestRef.getElementsByTagName("ds:DigestMethod")
								.item(0);
						if (digestMethodElement.hasAttribute("Algorithm")) {
							String digestAlg = digestMethodElement.getAttribute("Algorithm");
							if (DIGEST_ALGORITHMS.keySet().contains(digestAlg)) {
								data = applyDigestMethod(data, digestMethodElement.getAttribute("Algorithm"));
								String computedDigest = Base64.getEncoder().encodeToString(data);

								Element digestValueElement = (Element) manifestRef
										.getElementsByTagName("ds:DigestValue").item(0);
								String digestValue = digestValueElement.getTextContent();

								System.out.println("Computed Digest: " + computedDigest);
								System.out.println("DigestValue: " + digestValue);

								if (!computedDigest.equals(digestValue)) {
									errors = errors.concat("DigestValue and computed digest are not equal.");
								}

							} else {
								errors = errors.concat(
										"Invalid digest algorithm! Is not one of: " + DIGEST_ALGORITHMS.keySet());
							}
						} else {
							errors = errors.concat("DigestMethod element does not have attribute \"Algorithm\".");
						}
						data = applyDigestMethod(data, digestMethodElement.getAttribute("Algorithm"));
					} else {
						errors = errors.concat("Invalid trasnform algorithm! Is not one of: " + TRANSFORMATIONS);
					}
				} else {
					errors = errors.concat("Transform element does not have attribute \"Algorithm\".");
				}
			}
		}

		return errors;
	}

	/*
	 * overenie ds:Manifest elementov: každý ds:Manifest element musí mať Id
	 * atribút, overenie hodnoty Type atribútu voči profilu XAdES_ZEP, každý
	 * ds:Manifest element musí obsahovať práve jednu referenciu na ds:Object
	 * 
	 * Zuzana, Miro - checked
	 */
	public static String checkManifest(Document doc) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, TransformerException, TransformException, IOException, DOMException,
			XMLSignatureException, InvalidTransformException, XMLSecurityException, XPathExpressionException {
		String errors = "";
		NodeList manifestList = doc.getElementsByTagName("ds:Manifest");

		for (int i = 0; i < manifestList.getLength(); i++) {
			Element manifest = (Element) manifestList.item(i);

			if (!manifest.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:Manifest! \n");
			}
			NodeList children = manifest.getChildNodes();
			List<Element> childrenElement = new LinkedList<Element>();

			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j) instanceof Element)
					childrenElement.add((Element) children.item(j));
			}

			if (childrenElement.size() == 1) {
				Element ref = childrenElement.get(0);
				if (ref.getNodeName().equals("ds:Reference")) {
					if (ref.hasAttribute("Type")) {
						if (!ref.getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#Object")) {
							errors = errors.concat("Invalid Type attribute value in ds:Manifest! \n");
						}
					} else {
						errors = errors.concat("Missing Type attribute in ds:Manifest! \n");
					}

					XPathFactory xpathFactory = XPathFactory.newInstance();
					XPath xpath = xpathFactory.newXPath();
					Element refObject = (Element) xpath.evaluate(
							"//*[@Id='" + ref.getAttribute("URI").substring(1) + "']", doc, XPathConstants.NODE);

					if (refObject == null || !refObject.getNodeName().equals("ds:Object")) {
						errors = errors.concat("Reference to invalid element in ds:Manifest! \n");
					} else {
						errors = errors.concat(checkManifest(ref, refObject));
					}
				} else {
					errors = errors.concat("Invalid element in ds:Manifest! \n");
				}
			} else {
				errors = errors.concat("Invalid elements in ds:Manifest! \n");
			}
		}

		System.out.println(errors);
		return errors;
	}

	/*
	 * overenie obsahu ds:SignatureProperties: musí mať Id atribút, musí obsahovať
	 * dva elementy ds:SignatureProperty pre xzep:SignatureVersion a
	 * xzep:ProductInfos, obidva ds:SignatureProperty musia mať atribút Target
	 * nastavený na ds:Signature
	 * 
	 * Zuzana - checked
	 */
	public static String checkSignatureProperties(Document doc) {
		String errors = "";
		Element sigProp = (Element) doc.getElementsByTagName("ds:SignatureProperties").item(0);
		if (!sigProp.hasAttribute("Id")) {
			errors = errors.concat("Missing Id attribute in ds:SignatureProperties! \n");
		}
		NodeList sigPropChild = sigProp.getChildNodes();
		List<Element> children = new LinkedList<Element>();

		for (int i = 0; i < sigPropChild.getLength(); i++) {
			if (sigPropChild.item(i) instanceof Element)
				children.add((Element) sigPropChild.item(i));
		}

		if (children.size() != 2) {
			errors = errors.concat("Invalid count of ds:SignatureProperty elements in ds:SignatureProperties! \n");
		} else {
			if (children.get(0).getNodeName().equals("ds:SignatureProperty")
					&& children.get(1).getNodeName().equals("ds:SignatureProperty")) {
				Element sigProp1 = children.get(0);
				Element sigProp2 = children.get(1);
				boolean sv = false;
				boolean pi = false;

				if (sigProp1.hasAttribute("Target")) {
					if (!sigProp1.getAttribute("Target").substring(1)
							.equals(((Element) doc.getElementsByTagName("ds:Signature").item(0)).getAttribute("Id"))) {
						errors = errors.concat("Invalid Target attribute in ds:SignatureProperty! \n");
					}
				} else {
					errors = errors.concat("Missing Id attribute in ds:SignatureProperty! \n");
				}

				if (sigProp2.hasAttribute("Target")) {
					if (!sigProp2.getAttribute("Target").substring(1)
							.equals(((Element) doc.getElementsByTagName("ds:Signature").item(0)).getAttribute("Id"))) {
						errors = errors.concat("Invalid Target attribute in ds:SignatureProperty! \n");
					}
				} else {
					errors = errors.concat("Missing Id attribute in ds:SignatureProperty! \n");
				}

				if (sigProp1.getElementsByTagName("xzep:ProductInfos").item(0) != null
						|| sigProp2.getElementsByTagName("xzep:ProductInfos").item(0) != null) {
					pi = true;
				}
				if (sigProp1.getElementsByTagName("xzep:SignatureVersion").item(0) != null
						|| sigProp2.getElementsByTagName("xzep:SignatureVersion").item(0) != null) {
					sv = true;
				}

				if (!sv) {
					errors = errors.concat("Missing xzep:SignatureVersion element in ds:SignatureProperty! \n");
				}
				if (!pi) {
					errors = errors.concat("Missing xzep:ProductInfos element in ds:SignatureProperty! \n");
				}

			} else {
				errors = errors.concat("Invalid elements in ds:SignatureProperties! \n");
			}
		}

		System.out.println(errors);
		return errors;
	}

	/*
	 * overenie obsahu ds:KeyInfo: musí mať Id atribút, musí obsahovať ds:X509Data,
	 * ktorý obsahuje elementy: ds:X509Certificate, ds:X509IssuerSerial,
	 * ds:X509SubjectName, hodnoty elementov ds:X509IssuerSerial a
	 * ds:X509SubjectName súhlasia s príslušnými hodnatami v certifikáte, ktorý sa
	 * nachádza v ds:X509Certificate
	 * 
	 * Zuzana - checked ds:X509IssuerName?
	 */
	public static String checkKeyInfo(Document doc) throws CertificateException {
		String errors = "";
		NodeList keyInfoList = doc.getElementsByTagName("ds:KeyInfo");
		if (keyInfoList.getLength() == 1) {
			Element keyInfo = (Element) keyInfoList.item(0);

			if (!keyInfo.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:KeyInfo! \n");
			}

			NodeList nl = keyInfo.getElementsByTagName("ds:X509Data");
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
					if (chNodes.item(i) instanceof Element == false)
						continue;
					if (chNodes.item(i).getNodeName().equals("ds:X509Certificate")) {
						certificate = true;
					} else if (chNodes.item(i).getNodeName().equals("ds:X509IssuerSerial")) {
						issuerSerial = true;
					} else if (chNodes.item(i).getNodeName().equals("ds:X509SubjectName")) {
						subjectName = true;
					} else {
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
				InputStream stream = new ByteArrayInputStream(Base64.getDecoder()
						.decode(data.getElementsByTagName("ds:X509Certificate").item(0).getTextContent().getBytes()));
				X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
				JcaX509CertificateHolder certHolder = new JcaX509CertificateHolder(cert);
				String issuer = certHolder.getIssuer().toString();
				if (!issuer.equals(data.getElementsByTagName("ds:X509IssuerName").item(0).getTextContent())) {
					errors = errors.concat("ds:X509IssuerName is not equal with issuer name in ds:X509Certificate! \n");
				}

				String serial = certHolder.getSerialNumber().toString();
				if (!serial.equals(data.getElementsByTagName("ds:X509SerialNumber").item(0).getTextContent())) {
					errors = errors
							.concat("ds:X509SerialNumber is not equal with serial number in ds:X509Certificate! \n");
				}

				String subject = certHolder.getSubject().toString();
				if (!subject.equals(data.getElementsByTagName("ds:X509SubjectName").item(0).getTextContent())) {
					errors = errors
							.concat("ds:X509SubjectName is not equal with subject name in ds:X509Certificate! \n");
				}
			} else {
				errors = errors.concat("Missing ds:X509Data element in ds:KeyInfo! \n");
			}
		} else {
			errors = errors.concat("Invalid count of ds:KeyInfo elements! \n");
		}

		System.out.println(errors);
		return errors;
	}

	/*
	 * overenie ostatných elementov profilu XAdES_ZEP, ktoré prináležia do
	 * špecifikácie XML Signature
	 * 
	 * Zuzana - checked
	 */
	public static String checkOtherElements(Document doc) throws XPathExpressionException {
		String errors = "";

		/*
		 * ds:Signature: musí mať Id atribút, musí mať špecifikovaný namespace xmlns:ds
		 * 
		 * checked
		 */
		NodeList signatureList = doc.getElementsByTagName("ds:Signature");
		if (signatureList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:Signature elements! \n");
		} else {
			Element id = (Element) signatureList.item(0);
			if (!id.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:Signature! \n");
			}

			Element signature = (Element) signatureList.item(0);
			if (signature.hasAttribute("xmlns:ds")) {
				if (!signature.getAttribute("xmlns:ds").equals("http://www.w3.org/2000/09/xmldsig#")) {
					errors = errors.concat("Invalid xmlns:ds attribute value in ds:Signature! \n");
				}
			} else {
				errors = errors.concat("Missing xmlns:ds attribute in ds:Signature! \n");
			}
		}

		/*
		 * ds:SignatureValue: musí mať Id atribút
		 * 
		 * checked
		 */
		NodeList signatureValueList = doc.getElementsByTagName("ds:SignatureValue");
		if (signatureValueList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:SignatureValue elements! \n");
		} else {
			Element id = (Element) signatureValueList.item(0);
			if (!id.hasAttribute("Id")) {
				errors = errors.concat("Missing Id attribute in ds:SignatureValue! \n");
			}
		}

		/*
		 * overenie existencie referencií v ds:SignedInfo a hodnôt atribútov Id a Type
		 * voči profilu XAdES_ZEP pre: ds:KeyInfo element, ds:SignatureProperties
		 * element, xades:SignedProperties element, všetky ostatné referencie v rámci
		 * ds:SignedInfo musia byť referenciami na ds:Manifest elementy
		 * 
		 * checked
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
							XPathFactory xpathFactory = XPathFactory.newInstance();
							XPath xpath = xpathFactory.newXPath();
							Element refElement = (Element) xpath.evaluate(
									"//*[@Id='" + reference.getAttribute("URI").substring(1) + "']", doc,
									XPathConstants.NODE);
							if (refElement != null) {
								if (refElement.getNodeName().equals("ds:KeyInfo")) {
									kiRef = true;
								} else {
									errors = errors.concat(
											"Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							} else {
								errors = errors
										.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						} else if (reference.getAttribute("Type")
								.equals("http://www.w3.org/2000/09/xmldsig#SignatureProperties")) {
							XPathFactory xpathFactory = XPathFactory.newInstance();
							XPath xpath = xpathFactory.newXPath();
							Element refElement = (Element) xpath.evaluate(
									"//*[@Id='" + reference.getAttribute("URI").substring(1) + "']", doc,
									XPathConstants.NODE);
							if (refElement != null) {
								if (refElement.getNodeName().equals("ds:SignatureProperties")) {
									sgtrRef = true;
								} else {
									errors = errors.concat(
											"Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							} else {
								errors = errors
										.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						} else if (reference.getAttribute("Type")
								.equals("http://uri.etsi.org/01903#SignedProperties")) {
							XPathFactory xpathFactory = XPathFactory.newInstance();
							XPath xpath = xpathFactory.newXPath();
							Element refElement = (Element) xpath.evaluate(
									"//*[@Id='" + reference.getAttribute("URI").substring(1) + "']", doc,
									XPathConstants.NODE);
							if (refElement != null) {
								if (refElement.getNodeName().equals("xades:SignedProperties")) {
									sgdRef = true;
								} else {
									errors = errors.concat(
											"Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							} else {
								errors = errors
										.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						} else if (reference.getAttribute("Type")
								.equals("http://www.w3.org/2000/09/xmldsig#Manifest")) {
							XPathFactory xpathFactory = XPathFactory.newInstance();
							XPath xpath = xpathFactory.newXPath();
							Element refElement = (Element) xpath.evaluate(
									"//*[@Id='" + reference.getAttribute("URI").substring(1) + "']", doc,
									XPathConstants.NODE);
							if (refElement != null) {
								if (!refElement.getNodeName().equals("ds:Manifest")) {
									errors = errors.concat(
											"Invalid element name for Type attribute in ds:Reference in ds:SignedInfo! \n");
								}
							} else {
								errors = errors
										.concat("Invalid URI attribute value in ds:Reference in ds:SignedInfo! \n");
							}
						} else {
							errors = errors.concat("Invalid Type attribute value in ds:Reference in ds:SignedInfo! \n");
						}
					} else {
						errors = errors.concat("Missing URI attribute in ds:Reference in ds:SignedInfo! \n");
					}
				} else {
					errors = errors.concat("Missing Type attribute in ds:Reference in ds:SignedInfo! \n");
				}
			} else {
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
	 * kontrola obsahu ds:DigestMethod vo všetkých referenciách v ds:SignedInfo musi
	 * obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP
	 * 
	 * Zuzana - checked
	 */
	public static String checkDigestMethod(Document doc) {
		String errors = "";
		NodeList dmList = doc.getElementsByTagName("ds:DigestMethod");

		for (int i = 0; i < dmList.getLength(); i++) {
			Element dm = (Element) dmList.item(i);
			if (dm.hasAttribute("Algorithm")) {
				if (!(dm.getAttribute("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#sha1")
						|| dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig#sha224")
						|| dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmlenc#sha256")
						|| dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#sha384")
						|| dm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmlenc#sha512"))) {
					errors = errors.concat("Invalid Algorithm attribute value in ds:DigestMethod! \n");
				}
			} else {
				errors = errors.concat("Missing Algorithm attribute in ds:DigestMethod! \n");
			}
		}
		System.out.println(errors);
		return errors;
	}

	/*
	 * kontrola obsahu ds:Transforms vo všetkých referenciách v ds:SignedInfo musi
	 * obsahovať URI niektorého z podporovaných algoritmov podľa profilu XAdES_ZEP
	 * 
	 * Zuzana - checked
	 */
	public static String checkTransforms(Document doc) {
		String errors = "";
		NodeList tList = doc.getElementsByTagName("ds:Transform");

		for (int i = 0; i < tList.getLength(); i++) {
			Element t = (Element) tList.item(i);
			if (t.hasAttribute("Algorithm")) {
				if (!t.getAttribute("Algorithm").equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
					errors = errors.concat("Invalid Algorithm attribute value in ds:Transform! \n");
				}
			} else {
				errors = errors.concat("Missing Algorithm attribute in ds:Transform! \n");
			}
		}

		System.out.println(errors);
		return errors;
	}

	/*
	 * kontrola obsahu ds:SignatureMethod musi obsahovať URI niektorého z
	 * podporovaných algoritmov pre dané elementy podľa profilu XAdES_ZEP
	 * 
	 * Zuzana - checked
	 */
	public static String checkSignatureMethod(Document doc) {
		String errors = "";
		NodeList smList = doc.getElementsByTagName("ds:SignatureMethod");

		if (smList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:SignatureMethod elements! \n");
		} else {
			Element cm = (Element) smList.item(0);
			if (cm.hasAttribute("Algorithm")) {
				if (!(cm.getAttribute("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1")
						|| cm.getAttribute("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1")
						|| cm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256")
						|| cm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384")
						|| cm.getAttribute("Algorithm").equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"))) {
					errors = errors.concat("Invalid Algorithm attribute value in ds:SignatureMethod! \n");
				}
			} else {
				errors = errors.concat("Missing Algorithm attribute in ds:SignatureMethod! \n");
			}
		}

		System.out.println(errors);
		return errors;
	}

	/*
	 * kontrola obsahu ds:CanonicalizationMethod musi obsahovať URI niektorého z
	 * podporovaných algoritmov pre dané elementy podľa profilu XAdES_ZEP
	 * 
	 * Zuzana - checked
	 */
	public static String checkCanonicalizationMethod(Document doc) {
		String errors = "";
		NodeList cmList = doc.getElementsByTagName("ds:CanonicalizationMethod");

		if (cmList.getLength() != 1) {
			errors = errors.concat("Invalid count of ds:CanonicalizationMethod elements! \n");
		} else {
			Element cm = (Element) cmList.item(0);
			if (cm.hasAttribute("Algorithm")) {
				if (!cm.getAttribute("Algorithm").equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
					errors = errors.concat("Invalid Algorithm attribute value in ds:CanonicalizationMethod! \n");
				}
			} else {
				errors = errors.concat("Missing Algorithm attribute in ds:CanonicalizationMethod! \n");
			}
		}

		System.out.println(errors);
		return errors;
	}

	/*
	 * koreňový element musí obsahovať atribúty xmlns:xzep a xmlns:ds podľa profilu
	 * XADES_ZEP
	 * 
	 * Zuzana - checked
	 */
	public static String checkRootElement(Document doc) {
		String errors = "";

		Element root = doc.getDocumentElement();
		if (root.hasAttribute("xmlns:xzep")) {
			String xzep = root.getAttribute("xmlns:xzep");
			if (!xzep.equals("http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0")) {
				errors = errors.concat("Invalid xmlns:zep attribute value in root element! \n");
			}
		} else {
			errors = errors.concat("Missing xmlns:zep attribute in root element! \n");
		}

		if (root.hasAttribute("xmlns:ds")) {
			String ds = root.getAttribute("xmlns:ds");
			if (!ds.equals("http://www.w3.org/2000/09/xmldsig#")) {
				errors = errors.concat("Invalid xmlns:ds attribute value in root element! \n");
			}
		} else {
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

	private static Element getElementById(Document doc, String id) throws XPathExpressionException {
//		NodeList nl = doc.getElementsByTagName("ds:Object");
//
//		for (int i = 0; i < nl.getLength(); i++) {
//			Element element = (Element) nl.item(i);
//			if (id.equals(element.getAttribute("Id"))) {
//				return element;
//			}
//		}
//
//		return null;

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		Element refObject = (Element) xpath.evaluate("//*[@Id='" + id.substring(1) + "']", doc, XPathConstants.NODE);

		return refObject;
	}

	private static byte[] applyTransforms(Element transforms, Element object)
			throws DOMException, XMLSignatureException, InvalidTransformException, XMLSecurityException {
		Init.init();
		Transforms transformsObj = new Transforms(transforms, "");
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		transformsObj.performTransforms(new XMLSignatureInput(object), os);

//		System.out.println("after transformation: " + new String(os.toByteArray()));

		return os.toByteArray();
	}

	private static byte[] applyDigestMethod(byte[] data, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHMS.get(algorithm));
		return digest.digest(data);
	}

	private static Map<String, String> initializeDigestAlgorithms() {
		Map<String, String> digestAlgorithms = new HashMap<>();

		digestAlgorithms.put("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
		digestAlgorithms.put("http://www.w3.org/2001/04/xmldsig-more#sha224", "SHA-224");
		digestAlgorithms.put("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
		digestAlgorithms.put("http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384");
		digestAlgorithms.put("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");

		return digestAlgorithms;
	}
}
