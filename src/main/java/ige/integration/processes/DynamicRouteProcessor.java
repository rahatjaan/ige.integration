package ige.integration.processes;

import ige.integration.constants.Constants;
import ige.integration.transformer.BillDetailsTransformer;
import ige.integration.transformer.GuestCheckInTransformer;
import ige.integration.transformer.GuestPlaceOrderTransformer;
import ige.integration.transformer.GuestTransactionsTransformer;
import ige.integration.utils.XMLElementExtractor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		String flow = arg0.getIn().getHeader("flow").toString();
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
		req = req.replaceAll("<item>","");
		req = req.replaceAll("</item>","");
		req = req.replaceAll("<e>","<item>");
		req = req.replaceAll("</e>","</item>");
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
            SOAPMessage soapResponse = null;
            if(Constants.GUESTBILLINFO.equalsIgnoreCase(flow)){
            	soapResponse = soapConnection.call(createSOAPRequestForGuestBillInfo(req), url);
            }else if(Constants.GUESTCHECKIN.equalsIgnoreCase(flow)){
            	soapResponse = soapConnection.call(createSOAPRequestForGuestCheckIn(req), url);
            }else if(Constants.GUESTPLACEORDER.equalsIgnoreCase(flow)){
            	soapResponse = soapConnection.call(createSOAPRequestForPlaceOrder(req), url);
            }else if(Constants.GUESTCHECKOUT.equalsIgnoreCase(flow)){
            	soapResponse = soapConnection.call(createSOAPRequestForGuestCheckOut(req), url);
            }

            // Process the SOAP Response
            String message = printSOAPResponse(soapResponse);
            message = message.replaceAll("&lt;","<");
            message = message.replaceAll("&gt;",">");
            System.out.println("BEFORE TRANSFORMATION: "+message);
            boolean flag = false;
            if(message.contains("<faultcode>")){
            	flag = true;
            }
            String body = "";
            if(Constants.GUESTBILLINFO.equalsIgnoreCase(flow)){
            	body = BillDetailsTransformer.transform(message,flag);
            }else if(Constants.GUESTCHECKIN.equalsIgnoreCase(flow)){
            	body = GuestCheckInTransformer.transform(message,flag);
            }else if(Constants.GUESTPLACEORDER.equalsIgnoreCase(flow)){
            	body = GuestPlaceOrderTransformer.transform(message,flag);
            }else if(Constants.GUESTCHECKOUT.equalsIgnoreCase(flow)){
            	body = GuestTransactionsTransformer.transform(message,flag);
            }
            soapConnection.close();
            arg0.getOut().setBody(body);
        } catch (Exception e) {
            System.err.println("Error occurred while sending SOAP Request to Server");
            e.printStackTrace();
        }
		//*******************************************
		
		
	}
	
	
	private static SOAPMessage createSOAPRequestForGuestBillInfo(String value) throws Exception {
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
	
	
	private static SOAPMessage createSOAPRequestForGuestCheckOut(String value) throws Exception {
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
        String cardNum=XMLElementExtractor.extractXmlElementValue(value, "creditCardNumber");
        String roomNum=XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        //soapBodyElem3.addTextNode(room);
        
        System.out.println("****************");
        System.out.println(lastName+","+email+","+cardNum+","+roomNum);
        System.out.println("****************");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("guestCheckout", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("email");
        soapBodyElem2.addTextNode(email);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("creditCardNumber");
        soapBodyElem3.addTextNode(cardNum);
        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("roomNumber");
        soapBodyElem4.addTextNode(roomNum);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestCheckout");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	
	private static SOAPMessage createSOAPRequestForGuestCheckIn(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String[] guestInfo = {"firstName","lastName","fullAddress","mobileNumber","ratePlan","hhNumber","al","bonusAl","confirmationNumber","membershipNumber","bonusCode","groupName","email"};
        String[] guestStayInfo = {"roomNumber","floorNumber","arrivalDate","departureDate","folioNumber","totalBill","paymentType","creditAmount","cardType","cardNumber","balanceAmount","roomType","numberOfChildren","numberOfAdult","roomRate","creditCardExpirationDate","rateCode","reservationType"};
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("guestCheckIn", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("guestInfo");
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("guestStayInfo");
        Map<String,String> mapSoapElements = new HashMap<String,String>();
        for(int x=0;x<guestInfo.length;x++){
        	mapSoapElements.put(guestInfo[x],XMLElementExtractor.extractXmlElementValue(value, guestInfo[x]));
        }
        for(int y=0;y<guestStayInfo.length;y++){
        	mapSoapElements.put(guestStayInfo[y],XMLElementExtractor.extractXmlElementValue(value, guestStayInfo[y]));
        }
        
        SOAPElement [] soapElem = new SOAPElement[mapSoapElements.size()] ;
        //Iterate through the map to set SOAPElement and Body
        
        for(int i=0;i<guestInfo.length;i++){
        	String key = guestInfo[i];
        	System.out.println(key);
        	soapElem[i] = soapBodyElem1.addChildElement(key);
        	if(null == mapSoapElements.get(key))
              	soapElem[i].addTextNode("");
              else
              	soapElem[i].addTextNode(mapSoapElements.get(key));
        }
        for(int j=0;j<guestStayInfo.length;j++){
        	String key = guestStayInfo[j];
        	System.out.println(key);
        	soapElem[j] = soapBodyElem2.addChildElement(key);
        	if(null == mapSoapElements.get(key))
              	soapElem[j].addTextNode("");
              else
              	soapElem[j].addTextNode(mapSoapElements.get(key));
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestCheckIn");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	
	private static SOAPMessage createSOAPRequestForPlaceOrder(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        String originalVal = value;
        String serverURI = "http://webservice.integration.ige/";
        
        int ind1 = value.indexOf("<guestTransactions>");
        int ind2 = value.indexOf("</guestTransactions>");
        value = value.substring(ind1,ind2);
        System.out.println(value);
        List<String> text = new ArrayList<String>();
        while(-1 != value.indexOf("<item>")){
        	ind1 = value.indexOf("<item>");
        	ind2 = value.indexOf("</item>");
        	String v = value.substring(ind1,ind2+7);
        	System.out.println(v);
        	text.add(v);
        	value = value.substring(ind2+7);
        }

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String[] someInfo = {"lastName","email","roomNumber"};
        String[] guestTransactions = {"transactionDate","description","referenceNumber","transactionId","charges"};
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("placeOrder", "web");
        Map<String,String> mapSoapElements = new HashMap<String,String>();
        for(int x=0;x<someInfo.length;x++){
        	mapSoapElements.put(someInfo[x],XMLElementExtractor.extractXmlElementValue(originalVal, someInfo[x]));
        }
        int index = 0;
        while(text.size() >  index){
	        for(int j=0;j<guestTransactions.length;j++){
	        	String val = text.get(index);
	        	System.out.println("HERE: "+val);
	        	mapSoapElements.put(val,XMLElementExtractor.extractXmlElementValue(val, guestTransactions[j]));
	        }
	        index++;
        }
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("guestTransactions");
        SOAPElement soapBodyElem2 = null;
        
        SOAPElement [] soapElem = new SOAPElement[mapSoapElements.size()] ;
        //Iterate through the map to set SOAPElement and Body
        
        for(int i=0;i<someInfo.length;i++){
        	String key = someInfo[i];
        	System.out.println(key);
        	soapElem[i] = soapBodyElem.addChildElement(key);
        	if(null == mapSoapElements.get(key))
              	soapElem[i].addTextNode("");
              else
              	soapElem[i].addTextNode(mapSoapElements.get(key));
        }
        index = 0;
        while(text.size() >  index){
        	soapBodyElem2 = soapBodyElem1.addChildElement("item");
	        for(int j=0;j<guestTransactions.length;j++){
	        	String key = guestTransactions[j];
	        	System.out.println(key);
	        	soapElem[j] = soapBodyElem2.addChildElement(key);
	        	if(null == XMLElementExtractor.extractXmlElementValue(text.get(index), guestTransactions[j]))
	              	soapElem[j].addTextNode("");
	              else
	              	soapElem[j].addTextNode(XMLElementExtractor.extractXmlElementValue(text.get(index), guestTransactions[j]));
	        }
	        index++;
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestCheckIn");

        soapMessage.saveChanges();

        // Print the request message 
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
