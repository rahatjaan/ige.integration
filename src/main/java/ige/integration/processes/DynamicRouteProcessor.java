package ige.integration.processes;

import ige.integration.utils.XMLElementExtractor;

import java.io.StringWriter;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DynamicRouteProcessor implements Processor{

	public void process(Exchange arg0) throws Exception {
		// TODO Auto-generated method stub
		String url = arg0.getIn().getHeader("OutboundUrl").toString();
		System.out.println("URL IS: "+url);
		String req = arg0.getIn().getBody().toString();
		//url = "spring-ws:http://localhost:8080/spring-webservices-pms/endpoints?rootqname:{http://com/spring/pms/webservices/billservice}/BillDetailsRequest";
		/*String req = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bil=\"http://com/spring/pms/webservices/billservice\"><soapenv:Header/><soapenv:Body><bil:BillDetailsRequest><bil:lastName>Ali</bil:lastName><bil:memberShipNumber>23</bil:memberShipNumber><bil:roomNumber>1</bil:roomNumber></bil:BillDetailsRequest></soapenv:Body></soapenv:Envelope>";
		//arg0.getContext().createProducerTemplate().send((String) arg0.getIn().getHeader("OutboundUrl"),arg0);
		arg0.getOut().setBody(req);
		arg0.getContext().createProducerTemplate().requestBody(url,arg0);*/
		//System.out.println("here");
		//*******************************************
		System.out.println(req);
		int ind1 = req.indexOf("<");
		req = req.substring(ind1);
		req = req.replaceAll("&gt;",">");
		req = req.replaceAll("&lt;","<");
		req = req.substring(req.indexOf("<o>"));
		req = req.replaceAll("<o>","");
		req = req.replaceAll("</o>","");
		req = req.substring(0,req.indexOf("</payload>"));
		
		System.out.println("REQUEST : "+req);
		try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            //String url = "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx";
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(req), url);

            // Process the SOAP Response
            String message = printSOAPResponse(soapResponse);
            /*JSONObject xmlJSONObj = XML.toJSONObject(message);
            String jsonString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            System.out.println(jsonString);
            arg0.getOut().setBody(jsonString);*/
            //String message=XMLElementExtractor.extractXmlElementValue(req, "lastName");
            arg0.getOut().setBody(message);
            soapConnection.close();
        } catch (Exception e) {
            System.err.println("Error occurred while sending SOAP Request to Server");
            e.printStackTrace();
        }
		//*******************************************
		
		
	}
	
	
	private static SOAPMessage createSOAPRequest(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://com/spring/pms/webservices/billservice";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("bil", serverURI);

        /*
        Constructed SOAP Request Message:
        <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
            <SOAP-ENV:Header/>
            <SOAP-ENV:Body>
                <example:VerifyEmail>
                    <example:email>mutantninja@gmail.com</example:email>
                    <example:LicenseKey>123</example:LicenseKey>
                </example:VerifyEmail>
            </SOAP-ENV:Body>
        </SOAP-ENV:Envelope>
         */

        // SOAP Body
        //SOAPBody soapBody = envelope.getBody();
        //SOAPElement soapBodyElem = soapBody.addChildElement("BillDetailsRequest", "bil");
        //SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName", "bil");
        String lastName=XMLElementExtractor.extractXmlElementValue(value, "lastName");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String membership=XMLElementExtractor.extractXmlElementValue(value, "memberShipNumber");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String room=XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        //soapBodyElem3.addTextNode(room);
        
        System.out.println("****************");
        System.out.println(lastName+","+membership+","+room);
        System.out.println("****************");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("BillDetailsRequest", "bil");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName", "bil");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        soapBodyElem2.addTextNode(membership);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        soapBodyElem3.addTextNode(room);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "BillDetailsRequest");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }

    /**
     * Method used to print the SOAP Response
     */
    private static String printSOAPResponse(SOAPMessage soapResponse) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source sourceContent = soapResponse.getSOAPPart().getContent();
        System.out.print("\nResponse SOAP Message = ");
        StreamResult result = new StreamResult(System.out);
        transformer.transform(sourceContent, result);
        
        System.out.println();
        StringWriter writer = new StringWriter();
        transformer.transform(sourceContent, new StreamResult(writer));
        String output = writer.toString();
        int ind = output.indexOf("<SOAP");
        System.out.println("NOW IS: "+output.substring(ind));
        return output;
    }

}
