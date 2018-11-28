package soap;
import javax.xml.soap.*;

public class SOAPClient {
     public static SOAPMessage getTimestamp(String signedValue64) {
        String soapEndpointUrl = "http://test.ditec.sk/timestampws/TS.asmx";
        String soapAction = "http://www.ditec.sk/GetTimestamp";
        return callSoapWebService(soapEndpointUrl, soapAction, signedValue64);
    }
     private static void createSoapEnvelope(SOAPMessage soapMessage, String signedValue64) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();
        String myNamespace = "ns";
        String myNamespaceURI = "http://www.ditec.sk/";
         // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
         // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("GetTimestamp", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("dataB64", myNamespace);
        soapBodyElem1.addTextNode(signedValue64);
    }
     private static SOAPMessage callSoapWebService(String soapEndpointUrl, String soapAction, String signedValue64) {
    	SOAPMessage soapResponse = null;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
             // Send SOAP Message to SOAP Server
            soapResponse = soapConnection.call(createSOAPRequest(soapAction, signedValue64), soapEndpointUrl);
             // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();
             soapConnection.close();
        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
        
        return soapResponse;
    }
     private static SOAPMessage createSOAPRequest(String soapAction, String signedValue64) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        createSoapEnvelope(soapMessage, signedValue64);
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);
         soapMessage.saveChanges();
         /* Print the request message, just for debugging purposes */
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");
         return soapMessage;
    }
 }