package ige.integration.processes;

import ige.integration.transformer.BillDetailsTransformer;
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
		//*******************************************
		System.out.println(req);
		int ind1 = req.indexOf("<");
		req = req.substring(ind1);
		if(req.contains("&gt;")){
			req = req.replaceAll("&gt;",">");
			req = req.replaceAll("&lt;","<");
		}
		System.out.println("1"+req);
		req = req.substring(req.indexOf("<o>"));
		System.out.println("2"+req);
		req = req.replaceAll("<o>","");
		req = req.replaceAll("</o>","");
		System.out.println("3"+req);
		if(req.contains("</payload>"))
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
            String body = BillDetailsTransformer.transform(message);
            soapConnection.close();
            arg0.getOut().setBody(body);
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

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String lastName=XMLElementExtractor.extractXmlElementValue(value, "lastName");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String email=XMLElementExtractor.extractXmlElementValue(value, "email");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String room=XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        //soapBodyElem3.addTextNode(room);
        
        System.out.println("****************");
        System.out.println(lastName+","+email+","+room);
        System.out.println("****************");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("getBillInfo", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("email");
        soapBodyElem2.addTextNode(email);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber");
        soapBodyElem3.addTextNode(room);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "getBillInfo");

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
        int ind = output.indexOf("<S");
        System.out.println("NOW IS: "+output.substring(ind));
        return output;
    }

}
